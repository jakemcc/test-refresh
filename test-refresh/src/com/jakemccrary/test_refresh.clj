(ns com.jakemccrary.test-refresh
  (:require clojure.java.shell
            [clojure.stacktrace :as stacktrace]
            [clojure.string :as string]
            [clojure.pprint :as pp]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            clojure.test
            clojure.tools.namespace.dir
            clojure.tools.namespace.find
            clojure.tools.namespace.reload
            clojure.tools.namespace.repl
            clojure.tools.namespace.track
            [clojure.tools.cli :as cli]
            jakemcc.clojure-gntp.gntp
            [clojure.set :as set]))


(try
  (require 'circleci.test.report)
  (require 'circleci.test)
  (def capture-report (var-get (find-var 'circleci.test.report/report)))
  (def test-runner (var-get (find-var 'circleci.test/run-tests)))
  (catch Exception _
    (def capture-report (var-get (find-var 'clojure.test/report)))
    (def test-runner (var-get (find-var 'clojure.test/run-tests)))))

(defmacro dbg [& body]
  `(let [x# ~@body]
     (println (str "dbg: " (quote ~@body) "="))
     (pp/pprint x#)
     x#))

(def focus-flag (volatile! :test-refresh/focus))

(defn focused? [meta]
  (boolean (get meta @focus-flag)))

(defn- make-change-tracker []
  (clojure.tools.namespace.track/tracker))

(let [prev-failed (atom nil)]
  (defn- scan-for-changes [tracker watch-dirs]
    (try (let [new-tracker (apply clojure.tools.namespace.dir/scan tracker watch-dirs)]
           (reset! prev-failed false)
           new-tracker)
         (catch Exception e
           (when-not @prev-failed
             (println e))
           (reset! prev-failed true)
           ;; return the same tracker so we dont try to run tests
           tracker))))

(defn- namespaces-in-directories [dirs]
  (let [as-files (map clojure.java.io/file dirs)]
    (flatten (for [file as-files]
               (clojure.tools.namespace.find/find-namespaces-in-dir file)))))

(defn- refresh-environment []
  (clojure.tools.namespace.repl/refresh))

(defn- print-banner [banner]
  (when (and banner (not (string/blank? banner)))
    (println banner)))

(defn- print-end-message [run-time]
  (let [date-str (.format (java.text.SimpleDateFormat. "HH:mm:ss.SSS")
                          (java.util.Date.))]
    (println (format "Finished at %s (run time: %.3fs)" date-str
                     (float (/ run-time 1000))))))

(defn- print-to-console [report]
  (println)
  (println (:message report))
  (print-end-message (:run-time report)))

(defn- growl [title-postfix message]
  (try
    (jakemcc.clojure-gntp.gntp/message
     (str "TestRefresh - " title-postfix)
     message)
    (catch Exception ex
      (println "Problem communicating with growl, exception:" (.getMessage ex)))))

(defn- summary [results]
  (let [{:keys [pass error fail]} results]
    (if (pos? (+ fail error))
      {:status "Failed" :message (format "Failed %s of %s assertions"
                                         (+ fail error)
                                         (+ fail error pass))}
      {:status "Passed" :message (format "Passed all tests")})))

(def failed-tests (atom #{}))

(defn tracking-failed-tests? []
  (seq @failed-tests))

(defn previously-failing-test? [test-var]
  (@failed-tests (str test-var)))

(defn update-tracked-failing-tests [tracked-tests new-tests]
  (reduce conj tracked-tests (map str new-tests)))

(let [fail (get-method capture-report :fail)]
  (defmethod capture-report :fail [x]
    (swap! failed-tests update-tracked-failing-tests clojure.test/*testing-vars*)
    (fail x)))

(defn suppress-unselected-tests
  "A function that figures out which vars need to be suppressed based on
  the given selectors, moves their :test metadata
  to :leiningen/skipped-test (so that clojure.test won't think they
  are tests), runs the given function, and then sets the metadata
  back. Tests marked with :test-refresh/focus metadata are given
  priority over other selectors."
  [namespaces selectors func]
  (let [move-meta! (fn [var from-key to-key]
                     (when-let [x (get (meta var) from-key)]
                       (alter-meta! var #(-> % (assoc to-key x) (dissoc from-key)))))
        all-vars (mapcat (comp vals ns-interns) namespaces)
        tests-to-skip (when (seq selectors)
                        (remove (fn [var]
                                  (some (fn [[selector args]]
                                          (let [sfn (if (vector? selector)
                                                      (second selector)
                                                      selector)]
                                            (apply (eval sfn)
                                                   (merge (-> var meta :ns meta)
                                                          (assoc (meta var) :leiningen.test/var var))
                                                   args)))
                                        selectors))
                                all-vars))
        test-refresh-focused (filter (fn [var]
                                       (let [meta (merge (-> var meta :ns meta)
                                                         (assoc (meta var) :leiningen.test/var var))]
                                         (focused? meta)))
                                     all-vars)
        move-all-meta! (fn [from-key to-key]
                         (if (seq test-refresh-focused)
                           (doseq [v (set/difference (set all-vars) (set test-refresh-focused))]
                             (move-meta! v from-key to-key))
                           (doseq [v tests-to-skip]
                             (move-meta! v from-key to-key))))]
    (move-all-meta! :test :test-refresh/skipped)
    (try
      (func)
      (finally
        (move-all-meta! :test-refresh/skipped :test)))))

(defn- nses-selectors-match [selectors ns-sym]
  (distinct
   (for [ns ns-sym
         [_ var] (ns-publics ns)
         :when (some (fn [[selector args]]
                       (let [meta (merge (-> var meta :ns meta)
                                         (assoc (meta var) :leiningen.test/var var))]
                         (or (apply (eval (if (vector? selector)
                                            (second selector)
                                            selector))
                                    meta
                                    args)
                             (focused? meta))))
                     selectors)]
     ns)))

(defn- select-reporting-fn
  "Selects the reporting function based on user specified configuration"
  [report]
  (if (= ::no-report report)
    (fn silent-report [report])
    (do (when report (require (symbol (namespace (symbol report)))))
        (let [resolved-report (when report (let [rr (resolve (symbol report))]
                                             (if rr rr (println "Unable to locate report method:" report))))]
          (if resolved-report resolved-report capture-report)))))

(defn run-selected-tests [stack-depth test-paths selectors report namespaces-to-run]
  (let [test-namespaces (namespaces-in-directories test-paths)
        selected-test-namespaces (nses-selectors-match selectors test-namespaces)
        filtered-test-namespaces (if (seq namespaces-to-run)
                                   (filter namespaces-to-run selected-test-namespaces)
                                   selected-test-namespaces)]
    (binding [clojure.test/*stack-trace-depth* stack-depth
              clojure.test/report (select-reporting-fn report)]
      (reset! failed-tests #{})
      (summary
       (suppress-unselected-tests filtered-test-namespaces
                                  selectors
                                  #(apply test-runner filtered-test-namespaces))))))

(defn- run-tests
  "Refreshes project and runs tests. Tests can be restricted by
  test-selectors or namespaces in namespaces-to-run. When
  namespaces-to-run is empty then it does not cause any namespaces to
  be filtered out."
  ([stack-depth test-paths selectors report]
   (run-tests stack-depth test-paths selectors report #{}))
  ([stack-depth test-paths selectors report namespaces-to-run]
   (let [started (System/currentTimeMillis)
         refresh (refresh-environment)
         result (if (= :ok refresh)
                  (run-selected-tests stack-depth test-paths selectors report namespaces-to-run)
                  {:status "Error"
                   :message (str "Error refreshing environment: "
                                 clojure.core/*e " "
                                 (stacktrace/root-cause clojure.core/*e))
                   :exception clojure.core/*e})]
     (assoc result :run-time (- (System/currentTimeMillis) started)))))

(defn- something-changed? [x y]
  (not= x y))

(def printing (atom false))

(defn- monitor-keystrokes [keystroke-pressed with-repl?]
  (future
    (let [run-test-refresh! #(do (reset! keystroke-pressed true)
                                 (reset! printing true))]
      (if with-repl?
        (clojure.main/repl
         :prompt #(do (while @printing (Thread/sleep 100))
                      (clojure.main/repl-prompt))
         :read (fn [request-prompt request-exit]
                 (if-let [line (read-line)]
                   (if-not (empty? (clojure.string/trim line))
                     (read-string line)
                     (do (run-test-refresh!) :run-tests))
                   (System/exit 0))))
        (loop [c (.read System/in)]
          (if (= c -1)
            (do
              (println "")
              (println "*****************************************")
              (println "test-refresh's stdin stream has been closed, stopping monitoring for keystrokes to run tests")
              (println "*****************************************"))
            (do
              (run-test-refresh!)
              (recur (.read System/in)))))))))

(defn- create-user-notifier [notify-command]
  (let [notify-command (if (string? notify-command)
                         [notify-command]
                         notify-command)]
    (fn [message]
      (when (seq notify-command)
        (let [command (concat notify-command [message])]
          (try
            (apply clojure.java.shell/sh command)
            (catch Exception e
              (println (str "Problem running shell command `" (clojure.string/join " " command) "`"))
              (println "Exception:" (.getMessage e)))))))))

(defn passed? [test-run-result]
  (= "Passed" (:status test-run-result)))

(defn should-notify? [notify-on-success result]
  (not (and (not notify-on-success)
            (passed? result))))

(def ^:private top-stars (apply str (repeat 45 "*")))
(def ^:private side-stars (apply str (repeat 15 "*")))
(def ^:private default-banner (str top-stars
                                   "\n"
                                   side-stars
                                   "Running tests"
                                   side-stars))

(defn- resolve-banner [banner clear debug]
  (let [banner (or banner default-banner)]
    (if (or (true? clear)
            (and debug
                 (not (false? clear))))
      (str "\033[H" ;; Send caret to home position in console
           "\033[2J" ;; Clears console (preserving scrollback)
           (when-not (string/blank? banner) (str "\n" banner)))
      banner)))

(defn monitor-project [test-paths options]
  (let [watch-dirs (:watch-dirs options [])
        refresh-dirs (:refresh-dirs options [])
        growl? (:growl options)
        users-notifier (create-user-notifier (:notify-command options))
        should-notify? (partial should-notify? (:notify-on-success options))
        report (:report options)
        run-once? (:run-once options)
        with-repl? (:with-repl options)
        selectors (second (:nses-and-selectors options))

        run-once-exit-code (atom 0)
        monitoring? (atom false)

        do-not-monitor-keystrokes? (:do-not-monitor-keystrokes options false)
        keystroke-pressed (atom nil)
        debug-mode? (:debug options)
        test-paths (if debug-mode? [] test-paths)
        banner (resolve-banner (:banner options)
                               (:clear options)
                               debug-mode?)]

    (vreset! focus-flag (or (:focus-flag options) :test-refresh/focus))

    (when (seq refresh-dirs)
      (println "Only refreshing dirs:" (pr-str refresh-dirs))
      (apply clojure.tools.namespace.repl/set-refresh-dirs refresh-dirs))

    (when report
      (println "Using reporter:" report))

    (when (or (:quiet options) debug-mode?)
      (defmethod capture-report :begin-test-ns [m]))

    (loop [tracker (make-change-tracker)]
      (let [new-tracker (scan-for-changes tracker watch-dirs)
            something-changed? (something-changed? new-tracker tracker)]
        (try
          (when (or @keystroke-pressed something-changed?)
            (reset! keystroke-pressed nil)
            (reset! printing true)

            (when (and with-repl? @monitoring? something-changed?)
              (println))

            (print-banner banner)

            (let [stack-depth (:stack-trace-depth options)
                  was-failed (tracking-failed-tests?)
                  changed-namespaces (if (:changes-only options)
                                       (set (:clojure.tools.namespace.track/load new-tracker))
                                       #{})
                  report (if debug-mode? ::no-report report)
                  result (run-tests stack-depth test-paths selectors report changed-namespaces)
                  ;; tests need to be run once a failed test is resolved
                  result (if (and was-failed (passed? result))
                           (run-tests stack-depth test-paths selectors report)
                           result)]

              (when-not (and debug-mode?
                             (-> result :status (not= "Error")))
                (print-to-console result))
              
              (reset! run-once-exit-code (if (passed? result) 0 1))
              (when (should-notify? result)
                (when growl?
                  (growl (:status result) (:message result)))
                (users-notifier (:message result))))
            (reset! printing false)

            (when (and with-repl? @monitoring? something-changed?)
              (clojure.main/repl-prompt)
              (flush))

            (when (and (not do-not-monitor-keystrokes?)
                       (not run-once?)
                       (not @monitoring?))
              (monitor-keystrokes keystroke-pressed with-repl?) ;; TODO: this seems wrong? So many futures?
              (reset! monitoring? true)))
          (catch Exception ex (.printStackTrace ex)))
        (Thread/sleep 200)
        (if-not run-once?
          (recur (dissoc new-tracker
                         :clojure.tools.namespace.track/load
                         :clojure.tools.namespace.track/unload))
          (System/exit @run-once-exit-code))))))

(defn- parse-kw
  [^String s]
  (if (.startsWith s ":") (read-string s) (keyword s)))

(defn- accumulate [m k v]
  (update-in m [k] (fnil conj []) v))

(def cli-options
  [["-d" "--dir DIRNAME" "Name of the directory containing tests. Specify flag multiple times for specifying multiple test directories. Defaults to \"test\"."
    :parse-fn str
    :assoc-fn accumulate]
   [nil "--no-config" "Run and ignore all .test-refresh.edn files"]
   ["-c" "--config FILENAME" "Use specified file as a configuration file. Specify flag multiple times to specify multiple files. Later files override values found in earlier files. Defaults to [\"~/.test-refresh.edn\", \".test-refresh.edn\"]"
    :parse-fn str
    :assoc-fn accumulate]
   ["-h" "--help" "Display this help message"]])

(defn help
  ([args] (help args nil))
  ([args message]
   (when message
     (println message))
   (println "Below are the arguments supported by test-refresh:")
   (println (:summary args))))

(defn load-config [files]
  (let [error-on-missing? (seq files)
        configs (if (seq files)
                  (mapv io/file files)
                  [(io/file (System/getProperty "user.home") ".test-refresh.edn") (io/file ".test-refresh.edn")])]
    (apply merge
           {:nses-and-selectors [:ignore [[(constantly true)]]]}
           (for [config configs]
             (if-not (.exists config)
               (when error-on-missing?
                 (throw (ex-info (str "Couldn't load config file " (.getAbsoluteFile config))
                                 {:file (.getAbsoluteFile config)})))
               (edn/read-string (slurp config))))))) 

(defn -main
  [& args]
  (let [args (cli/parse-opts args cli-options)
        options (:options args)]
    (cond
      (:errors args)
      (do
        (run! println (:errors args))
        (help args)
        (System/exit 1))

      (:help options)
      (help args)

      (and (:no-config options)
           (seq (:config options)))
      (help args "Cannot specify --no-config and --config at same time")
      :else
      (let [test-paths (set (:dir options ["test"]))]
        (monitor-project test-paths (load-config (:config options)))))))

(defn run-in-repl
  "Helper function for running test-refresh from the repl. This ignores
  any project.clj or profiles.clj settings and blocks the repl. Hit
  ctrl-c to stop.

  This is intended for running in Cursive's repl so stacktraces become
  clickable.

  Pass in strings that are the path (relative is fine) to the
  directories containing your tests."
  [test-path & test-paths]
  (monitor-project (cons test-path test-paths)
                   {:nses-and-selectors [:ignore [[(constantly true)]]]
                    :do-not-monitor-keystrokes true}))

(comment
  java.io.Fileq
  (require 'clojure.java.classpath)
  (mapv (fn [f] (clojure.java.io/file f))))
