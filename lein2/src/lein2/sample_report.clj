(ns lein2.sample-report
    (:require [clojure.test :as t]))


(defmulti ^:dynamic my-report :type)

(defmethod my-report :default [m])

(defmethod my-report :pass [m]
           (t/with-test-out
             (t/inc-report-counter :pass)))

(defmethod my-report :error [m]
           (t/with-test-out
             (t/inc-report-counter :error)
             (println "\nERROR in" (t/testing-vars-str m))
             (when-let [message (:message m)] (println message))
             (println "expected:" (pr-str (:expected m)))
             (println "  actual:" (pr-str (:actual m)))
             (println)))

(defmethod my-report :fail [m]
           (t/with-test-out
             (t/inc-report-counter :fail)
             (println "\nFAIL in" (t/testing-vars-str m))
             (when-let [message (:message m)] (println message))
             (println "expected:" (pr-str (:expected m)))
             (println "  actual:" (pr-str (:actual m)))
             (println)))

(defmethod my-report :begin-test-ns [m]
           (t/with-test-out
             (println "\nTesting" (ns-name (:ns m)))))

(defmethod my-report :end-test-ns [m])

(defmethod my-report :summary [m]
           (t/with-test-out
             (println "\nRan" (:test m) "tests containing"
                      (+ (:pass m) (:fail m) (:error m)) "assertions.")
             (println (:fail m) "failures," (:error m) "errors.")))

