(ns ^:slow lein2.bad-namespace-test
  (:require [clojure.test :refer :all]))

(deftest a-fake-slow-test
  (is (= 1 2)))
