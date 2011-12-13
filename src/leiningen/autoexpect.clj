(ns leiningen.autoexpect
  (:use [leiningen.compile :only [eval-in-project]]))

(defn autoexpect
  [project & args]
  (eval-in-project
   project
   `(do
      (let [stars# (apply str (repeat 15 "*"))
            check# (fresh.core/freshener
                    #(fresh.core/clj-files-in
                      (clojure.java.io/file "test")
                      (clojure.java.io/file "src")))]
        (loop [_# nil]
          (try
            (let [report# (check#)]
              (when-let [reloaded# (seq (:reloaded report#))]
                (println stars# "Running tests" stars#)
                (expectations/run-all-tests))
              (Thread/sleep 500))
            (catch Exception ex (println ex)))
          (recur nil))))
   nil nil `(do (require 'fresh.core
                         'expectations
                         'clojure.java.io))))