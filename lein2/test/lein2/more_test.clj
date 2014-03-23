(ns lein2.more-test
  (:require [clojure.test :refer :all]))

(deftest cows
  (is (= :moo (:sound {:sound :moo}))))

