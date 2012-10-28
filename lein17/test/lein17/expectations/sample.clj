(ns lein17.expectations.sample
  (:use expectations))

(expect 1 1)

;; uncomment to test failures
;(expect 3 4)

;; uncomment to test failures to reload code
;(expect 3 (what))