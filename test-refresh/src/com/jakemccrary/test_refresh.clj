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
(add-watch failed-tests :failed-tests
           (fn [key ref old-state new-state]
             (println "old:" old-state)
             (println "new:" new-state)))

(def capture-report clojure.test/report)
(let [fail (get-method clojure.test/report :fail)]
  (defmethod capture-report :fail [x]
    (swap! failed-tests
           (fn [all-failed just-failed]
             (apply conj all-failed just-failed))
           clojure.test/*testing-vars*)
    (fail x)))

(defn match [test-var [selector args]]
  (let [form (if (vector? selector)
               (second selector)
               selector)
        selector-fn (eval form)]
    (apply selector-fn
      (merge (-> test-var meta :ns meta)
             (assoc (meta test-var) :leiningen.test/var test-var))
      args)))

(defn selected? [selectors test-var]
  (some #(match test-var %) selectors))

(defn run-selected-tests [test-paths selectors]
  (let [test-namespaces (namespaces-in-directories test-paths)
        tests-in-namespaces (select-vars :test (vars-in-namespaces test-namespaces))
        disabled-tests (if (seq @failed-tests)
                          (remove @failed-tests tests-in-namespaces)
                          (remove #(selected? selectors %) tests-in-namespaces))]
    (println "Previously failed tests:" (str/join " "  @failed-tests))
    (println "SELECTORS" selectors)
    (move-metadata! disabled-tests :test :test-refresh/skipped)
    (binding [clojure.test/report capture-report]
      (reset! failed-tests #{})
      (let [result (summary (apply clojure.test/run-tests test-namespaces))]
        (move-metadata! disabled-tests :test-refresh/skipped :test)
        (println "Just failed tests:" (str/join " "  @failed-tests))        
        result))))

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
    (while true
      (.read System/in)
      (reset! keystroke-pressed true))))

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

            (let [was-failed @failed-tests
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
