(ns com.jakemccrary.test-refresh
  (:require clojure.java.shell
            clojure.pprint
            [clojure.string :as str]
            clojure.test
            clojure.tools.namespace.dir
            clojure.tools.namespace.find
            clojure.tools.namespace.reload
            clojure.tools.namespace.repl
            clojure.tools.namespace.track
            jakemcc.clojure-gntp.gntp)
  (:import java.text.SimpleDateFormat))

(defn- make-change-tracker []
  (clojure.tools.namespace.track/tracker))

(defn- scan-for-changes [tracker]
  (clojure.tools.namespace.dir/scan tracker))

(defn- namespaces-in-directories [dirs]
  (let [as-files (map clojure.java.io/file dirs)]
    (flatten (for [file as-files]
               (clojure.tools.namespace.find/find-namespaces-in-dir file)))))

(defn- refresh-environment []
  ;; (clojure.tools.namespace.reload/track-reload tracker)
  (clojure.tools.namespace.repl/refresh))

(def ^:private top-stars (apply str (repeat 45 "*")))
(def ^:private side-stars (apply str (repeat 15 "*")))

(defn- print-banner []
  (println top-stars)
  (println side-stars "Running tests" side-stars))

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
  (let [{:keys [pass test error fail]} results]
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

(def capture-report clojure.test/report)
(let [fail (get-method clojure.test/report :fail)]
  (defmethod capture-report :fail [x]
    (swap! failed-tests update-tracked-failing-tests clojure.test/*testing-vars*)
    (fail x)))

(defn suppress-unselected-tests
  "A function that figures out which vars need to be suppressed based on the
  given selectors, moves their :test metadata to :leiningen/skipped-test (so
  that clojure.test won't think they are tests), runs the given function, and
  then sets the metadata back."
  [namespaces selectors func]
  (let [move-meta! (fn [var from-key to-key]
                     (if-let [x (get (meta var) from-key)]
                       (alter-meta! var #(-> % (assoc to-key x) (dissoc from-key)))))
        vars (if (seq selectors)
               (->> namespaces
                    (mapcat (comp vals ns-interns))
                    (remove (fn [var]
                              (some (fn [[selector args]]
                                      (let [sfn (if (vector? selector)
                                                  (second selector)
                                                  selector)]
                                        (apply (eval sfn)
                                               (merge (-> var meta :ns meta)
                                                      (assoc (meta var) :leiningen.test/var var))
                                               args)))
                                    selectors)))))
        move-all-meta! (fn [from-key to-key]
                         (doseq [v vars]
                           (move-meta! v from-key to-key)))]
    (move-all-meta! :test :test-refresh/skipped)
    (try (func)
         (finally
           (move-all-meta! :test-refresh/skipped :test)))))

(defn- nses-selectors-match [selectors ns-sym]
  (distinct
   (for [ns ns-sym
         [_ var] (ns-publics ns)
         :when (some (fn [[selector args]]
                       (apply (eval (if (vector? selector)
                                      (second selector)
                                      selector))
                              (merge (-> var meta :ns meta)
                                     (assoc (meta var) :leiningen.test/var var))
                              args))
                     selectors)]
     ns)))


(defn- select-reporting-fn
  "Selects the reporting function based on user specified configuration"
  [report]
  (when report (require (symbol (namespace (symbol report)))))
  (let [resolved-report (when report (let [rr (resolve (symbol report))]
                                       (if rr rr (println "Unable to locate report method:" report))))]
    (if resolved-report resolved-report capture-report)))

(defn run-selected-tests [test-paths selectors report namespaces-to-run]
  (let [test-namespaces (namespaces-in-directories test-paths)
        selected-test-namespaces (nses-selectors-match selectors test-namespaces)
        filtered-test-namespaces (if (seq namespaces-to-run)
                                   (filter namespaces-to-run selected-test-namespaces)
                                   selected-test-namespaces)]
    (binding [clojure.test/report (select-reporting-fn report)]
      (reset! failed-tests #{})
      (summary
       (suppress-unselected-tests filtered-test-namespaces
                                  selectors
                                  #(apply clojure.test/run-tests filtered-test-namespaces))))))

(defn- run-tests
  "Refreshes project and runs tests. Tests can be restricted by
  test-selectors or namespaces in namespaces-to-run. When
  namespaces-to-run is empty then it does not cause any namespaces to
  be filtered out."
  ([test-paths selectors report]
   (run-tests test-paths selectors report #{}))
  ([test-paths selectors report namespaces-to-run]
   (let [started (System/currentTimeMillis)
         refresh (refresh-environment)
         result (if (= :ok refresh)
                  (run-selected-tests test-paths selectors report namespaces-to-run)
                  {:status "Error"
                   :message (str "Error refreshing environment: " clojure.core/*e)
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
        (loop [line (read-line)]
          (if-not line
            (System/exit 0)
            (run-test-refresh!))
          (recur (read-line)))))))

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

(defn monitor-project [test-paths options]
  (let [should-growl (:growl options)
        users-notifier (create-user-notifier (:notify-command options))
        should-notify? (partial should-notify? (:notify-on-success options))
        keystroke-pressed (atom nil)
        selectors (second (:nses-and-selectors options))
        report (:report options)
        run-once? (:run-once options)
        with-repl? (:with-repl options)
        monitoring? (atom false)]

    (when report
      (println "Using reporter:" report))
    (when (:quiet options)
      (defmethod capture-report :begin-test-ns [m]))

    (loop [tracker (make-change-tracker)]
      (let [new-tracker (scan-for-changes tracker)
            something-changed? (something-changed? new-tracker tracker)]
        (try
          (when (or @keystroke-pressed something-changed?)
            (reset! keystroke-pressed nil)
            (reset! printing true)

            (when (and with-repl? @monitoring? something-changed?) (println))
            (print-banner)

            (let [was-failed (tracking-failed-tests?)
                  changed-namespaces (if (:changes-only options)
                                       (set (:clojure.tools.namespace.track/load new-tracker))
                                       #{})
                  result (run-tests test-paths selectors report changed-namespaces)
                  ;; tests need to be run once a failed test is resolved
                  result (if (and was-failed (passed? result))
                           (run-tests test-paths selectors report)
                           result)]
              (print-to-console result)
              (when (should-notify? result)
                (when should-growl
                  (growl (:status result) (:message result)))
                (users-notifier (:message result))))
            (reset! printing false)

            (when (and with-repl? @monitoring? something-changed?)
              (clojure.main/repl-prompt)
              (flush))

            (when (and (not run-once?)
                       (not @monitoring?))
              (monitor-keystrokes keystroke-pressed with-repl?)
              (reset! monitoring? true)))
          (catch Exception ex (.printStackTrace ex)))
        (Thread/sleep 200)
        (if-not run-once?
          (recur (dissoc new-tracker
                         :clojure.tools.namespace.track/load
                         :clojure.tools.namespace.track/unload))
          (do
            (println "K THX BYE")
            (System/exit 0)))))))
