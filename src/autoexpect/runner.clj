(ns autoexpect.runner
  (:require clojure.tools.namespace.track
            clojure.tools.namespace.repl
            expectations
            clj-gntp.core))

(defn- turn-off-testing-at-shutdown []
  (reset! expectations/run-tests-on-shutdown false))

(defn- make-tracker []
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
  (clj-gntp.core/growl-notify (str "AutoExpect - " title-postfix)
                              message
                              nil nil))
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
      (println "Error refreshing environment:" clojure.core/*e))))

(defn- something-changed? [x y]
  (not= x y))

(defn monitor-project []
  (turn-off-testing-at-shutdown)
  (loop [tracker (make-tracker)]
    (let [new-tracker (scan-for-changes tracker)]
      (try
        (when (something-changed? new-tracker tracker)
          (run-tests))
        (Thread/sleep 500)
        (catch Exception ex (.printStackTrace ex)))
      (recur new-tracker))))