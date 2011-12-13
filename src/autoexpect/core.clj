(ns autoexpect.core
  (:use [fresh.core :only [freshener clj-files-in]]
        [expectations :only [run-all-tests]]
        [clojure.java.io :only [file]]))

(defn -main []
  (let [stars (apply str (repeat 15 "*"))
        check (freshener #(clj-files-in (file "test") (file "src")))]
    (loop [_ nil]
      (try
        (let [report (check)]
          (when-let [reloaded (seq (:reloaded report))]
            (println stars "Running tests" stars)
            (run-all-tests))
          (Thread/sleep 500))
        (catch Exception ex (println ex)))
      (recur nil))))
