(ns com.jakemccrary.test-refresh
  (:require clojure.java.shell
            [clojure.string :as str]
            clojure.test
            clojure.tools.namespace.dir
            clojure.tools.namespace.find
            clojure.tools.namespace.repl
            clojure.tools.namespace.track
            jakemcc.clojure-gntp.gntp)
  (:import [java.text SimpleDateFormat]))

(defn- make-change-tracker []
  (clojure.tools.namespace.track/tracker))

(defn- scan-for-changes [tracker]
  (clojure.tools.namespace.dir/scan tracker))

(defn- namespaces-in-directories [dirs]
  (let [as-files (map clojure.java.io/file dirs)]
    (flatten (for [file as-files]
               (clojure.tools.namespace.find/find-namespaces-in-dir file)))))

(defn vars-in-namespaces [nses]
  (mapcat (comp vals ns-interns) nses))

(defn select-vars [selector-fn vars]
  (filter (comp selector-fn meta) vars))

(defn move-metadata! [vars from-key to-key]
  (doseq [var vars]
    (if-let [x (get (meta var) from-key)]
      (alter-meta! var #(-> % (assoc to-key x) (dissoc from-key))))))

(defmacro suppress-stdout [& forms]
  `(binding [*out* (java.io.StringWriter.)]
     ~@forms))

(defn- refresh-environment []
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
     (str "AutoTest - " title-postfix)
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

(def suppress-unselected-tests
  "A function that figures out which vars need to be suppressed based on the
  given selectors, moves their :test metadata to :leiningen/skipped-test (so
  that clojure.test won't think they are tests), runs the given function, and
  then sets the metadata back."
  (fn [namespaces selectors func]
    (let [copy-meta (fn [var from-key to-key]
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
          copy #(doseq [v vars] (copy-meta v %1 %2))]
      (copy :test :test-refresh/skipped)
      (try (func)
           (finally
             (copy :test-refresh/skipped :test))))))

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

(defn run-selected-tests [test-paths selectors]
  (let [test-namespaces (namespaces-in-directories test-paths)
        selected-test-namespaces (nses-selectors-match selectors test-namespaces)]
    (binding [clojure.test/report capture-report]
      (reset! failed-tests #{})
      (summary 
        (suppress-unselected-tests selected-test-namespaces 
                                   selectors
                                   #(apply clojure.test/run-tests selected-test-namespaces))))))

(defn- run-tests [test-paths selectors]
  (let [started (System/currentTimeMillis)
        refresh #_(suppress-stdout) (refresh-environment)
        result (if (= :ok refresh)
                 (run-selected-tests test-paths selectors)
                 {:status "Error"
                  :message (str "Error refreshing environment: " clojure.core/*e)
                  :exception clojure.core/*e})]
    (assoc result :run-time (- (System/currentTimeMillis) started))))

(defn- something-changed? [x y]
  (not= x y))

(defn- monitor-keystrokes [keystroke-pressed]
  (future
    (loop [c (.read System/in)]
      (if (= c -1)
        (System/exit 0)
        (do (reset! keystroke-pressed true)
            (recur (.read System/in)))))))

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

(defn monitor-project [test-paths should-growl notify-command notify-on-success nses-and-selectors]
  (let [users-notifier (create-user-notifier notify-command)
        should-notify? (partial should-notify? notify-on-success)
        keystroke-pressed (atom nil)
        selectors (second nses-and-selectors)]
    (monitor-keystrokes keystroke-pressed)
    (loop [tracker (make-change-tracker)]
      (let [new-tracker (scan-for-changes tracker)]
        (try
          (when (or @keystroke-pressed
                    (something-changed? new-tracker tracker))
            (reset! keystroke-pressed nil)
            (print-banner)

            (let [was-failed (tracking-failed-tests?)
                  result (run-tests test-paths selectors)
                  ; tests need to be run once a failed test is resolved
                  result (if (and was-failed (passed? result))
                           (run-tests test-paths selectors)
                           result)]
              (print-to-console result)
              (when (should-notify? result)
                (when should-growl
                  (growl (:status result) (:message result)))
                (users-notifier (:message result)))))
          (catch Exception ex (.printStackTrace ex)))
        (Thread/sleep 200)
        (recur new-tracker)))))
