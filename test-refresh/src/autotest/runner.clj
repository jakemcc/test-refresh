(ns autotest.runner
  (:require clojure.test
            clojure.tools.namespace.dir
            clojure.tools.namespace.find
            clojure.tools.namespace.track
            clojure.tools.namespace.repl
            jakemcc.clojure-gntp.gntp))

(def ^:private ^:dynamic *growl* nil)

(defn- make-change-tracker []
  (clojure.tools.namespace.track/tracker))

(defn- scan-for-changes [tracker]
  (clojure.tools.namespace.dir/scan tracker))

(defn- namespaces-in-directories [dirs]
  (let [as-files (map clojure.java.io/file dirs)]
    (flatten (for [file as-files]
               (clojure.tools.namespace.find/find-namespaces-in-dir file)))))

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
       (str "AutoTest - " title-postfix)
       message))
    (catch Exception ex
      (println "Problem communicating with growl, exception:" (.getMessage ex)))))

(defn- report [results]
  (let [{:keys [pass test error fail]} results]
    (if (< 0 (+ fail error))
      (growl "Failed" (format "Failed %s of %s tests."
                              (+ fail error)
                              test))
      (growl "Passed" (format "Passed %s tests" test)))))

(defn- run-tests [source-paths]
  (println source-paths) 
  (let [result (suppress-stdout (refresh-environment))]
    (if (= :ok result)
      (do
        (print-banner)
        (report (apply clojure.test/run-tests (namespaces-in-directories source-paths))))
      (let [message (str "Error refreshing environment: " clojure.core/*e)]
        (println message)
        (growl "Error" message )))))

(defn- something-changed? [x y]
  (not= x y))

(defn monitor-project [should-growl source-paths]
  (loop [tracker (make-change-tracker)]
    (let [new-tracker (scan-for-changes tracker)]
      (try
        (when (something-changed? new-tracker tracker)
          (binding [*growl* should-growl]
            (run-tests source-paths)))
        (Thread/sleep 500)
        (catch Exception ex (.printStackTrace ex)))
      (recur new-tracker))))
