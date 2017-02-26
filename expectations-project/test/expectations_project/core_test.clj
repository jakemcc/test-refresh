(ns expectations-project.core-test
  (:require [expectations :refer :all]
            [expectations.clojure.test :refer [defexpect]]))

(defexpect two
  2 (+ 1 1))

(defexpect three
  3 (+ 1 1))

(defexpect group
  (expect [1 2] (conj [] 1 5))
  (expect #{1 2} (conj #{} 1 2))
  (expect {1 2} (assoc {} 1 3)))

