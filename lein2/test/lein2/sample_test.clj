(ns lein2.sample-test
  (:use clojure.test))

(deftest passes
  (is (= 1 1)))

(deftest passes-again
  (is (= 2 2)))

(deftest ^:integration integration
  (is (= 4 4)))

;;uncomment to test failures
;; (deftest fails
;;   (is (= 3 4)))

;; uncomment to test failures to reload code
;; (deftest bad-code
;;   (is (= 3 (what))))
