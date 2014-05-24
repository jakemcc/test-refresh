(ns ^:slow lein2.metadata-on-namespace-test
  (:require [clojure.test :refer :all]))

(deftest a-fake-slow-test
  (is (= 32 2)))
