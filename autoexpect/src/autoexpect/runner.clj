(ns autoexpect.runner
  (:require clojure.tools.namespace.track
            clojure.tools.namespace.repl
            expectations
            jakemcc.clojure-gntp.gntp))

(def ^:private ^:dynamic *growl* nil)

(defn- turn-off-testing-at-shutdown []
  (reset! expectations/run-tests-on-shutdown false))

(defn- make-change-tracker []
  (clojure.tools.namespace.track/tracker))

(defn- scan-for-changes [tracker]
  (clojure.tools.namespace.dir/scan tracker))

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

(defn- growl [title-postfix message]
  (try
    (when *growl*
      (jakemcc.clojure-gntp.gntp/message
       (str "AutoExpect - " title-postfix)
       message))
    (catch Exception ex
      (println "Problem communicating with growl, exception:" (.getMessage ex)))))

(defn- report [results]
  (let [{:keys [fail error test run-time]} results]
    (if (< 0 (+ fail error))
      (growl "Failed" (format "Failed %s of %s tests."
                              (+ fail error)
                              test))
      (growl "Passed" (format "Passed %s tests" test)))))

(defn- run-tests []
  (let [result (suppress-stdout (refresh-environment))]
    (if (= :ok result)
      (do
        (print-banner)
        (report (expectations/run-all-tests)))
      (let [message (str "Error refreshing environment: " clojure.core/*e)]
        (println message)
        (growl "Error" message )))))

(defn- something-changed? [x y]
  (not= x y))

(defn monitor-project [should-growl]
  (turn-off-testing-at-shutdown)
  (loop [tracker (make-change-tracker)]
    (let [new-tracker (scan-for-changes tracker)]
      (try
        (when (something-changed? new-tracker tracker)
          (binding [*growl* should-growl]
            (run-tests)))
        (Thread/sleep 500)
        (catch Exception ex (.printStackTrace ex)))
      (recur new-tracker))))
