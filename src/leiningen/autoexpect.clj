(ns leiningen.autoexpect
  (:use [leiningen.compile :only [eval-in-project]]))

(defn autoexpect
  "Autoruns expecations on source change"
  [project & args]
  (let [test-path (get :test-path project "test")
        source-path (get :source-path project "src")]
    (eval-in-project
     project
     `(do
        (reset! expectations/run-tests-on-shutdown false)
        (let [top-stars#  (apply str (repeat 45 "*"))
              side-stars# (apply str (repeat 15 "*"))
              check# (fresh.core/freshener
                      #(fresh.core/clj-files-in
                        (clojure.java.io/file ~test-path)
                        (clojure.java.io/file ~source-path)))]
          (loop [_# nil]
            (try
              (let [report# (check#)]
                (when-let [reloaded# (seq (:reloaded report#))]
                  (println top-stars#)
                  (println side-stars# "Running tests" side-stars#)
                  (expectations/run-all-tests))
                (Thread/sleep 500))
              (catch Exception ex# (.printStackTrace ex#)))
            (recur nil))))
     nil nil `(do (require 'fresh.core
                           'expectations
                           'clojure.java.io)))))