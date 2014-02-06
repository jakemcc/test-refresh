(ns com.jakemccrary.test-refresh
  (:require clojure.test
            clojure.tools.namespace.dir
            clojure.tools.namespace.find
            clojure.tools.namespace.track
            clojure.tools.namespace.repl
            clojure.java.shell
            clojure.string
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

(defn- print-end-message []
  (let [date-str (.format (java.text.SimpleDateFormat. "HH:mm:ss.SSS")
                          (java.util.Date.))]
    (println "Finished at" date-str)))

(defn- print-to-console [report]
  (println)
  (println (:message report))
  (print-end-message))

(defn- growl [title-postfix message]
  (try
    (jakemcc.clojure-gntp.gntp/message
     (str "AutoTest - " title-postfix)
     message)
    (catch Exception ex
      (println "Problem communicating with growl, exception:" (.getMessage ex)))))

(defn- report [results]
  (let [{:keys [pass test error fail]} results]
    (if (pos? (+ fail error))
      {:status "Failed" :message (format "Failed %s of %s assertions"
                                         (+ fail error)
                                         (+ fail error pass))}
      {:status "Passed" :message (format "Passed %s assertions in %s tests" pass test)})))

(defn- run-tests [test-paths]
  (let [result (suppress-stdout (refresh-environment))]
    (if (= :ok result)
      (report (apply clojure.test/run-tests (namespaces-in-directories test-paths)))
      {:status "Error" :message (str "Error refreshing environment: " clojure.core/*e)})))

(defn- something-changed? [x y]
  (not= x y))

(defn- monitor-keystrokes [keystroke-pressed]
  (future
    (while true
      (.read System/in)
      (reset! keystroke-pressed true))))

(defn- create-user-notifier [notify-command]
  (let [notify-command (if (string? notify-command) [notify-command] notify-command)]
    (fn [message]
      (when (seq notify-command)
        (let [command (concat notify-command [message])]
          (try
            (apply clojure.java.shell/sh command)
            (catch Exception e
              (println (str "Problem running shell command `" (clojure.string/join " " command) "`"))
              (println "Exception:" (.getMessage e)))))))))

(defn monitor-project [test-paths should-growl notify-command]
  (let [users-notifier (create-user-notifier notify-command)
        keystroke-pressed (atom nil)]
    (monitor-keystrokes keystroke-pressed)
    (loop [tracker (make-change-tracker)]
      (let [new-tracker (scan-for-changes tracker)]
        (try
          (when (or @keystroke-pressed
                    (something-changed? new-tracker tracker))
            (reset! keystroke-pressed nil)
            (print-banner)
            (let [result (run-tests test-paths)]
              (print-to-console result)
              (when should-growl
                (growl (:status result) (:message result)))
              (users-notifier (:message result))))
          (Thread/sleep 200)
          (catch Exception ex (.printStackTrace ex)))
        (recur new-tracker)))))
