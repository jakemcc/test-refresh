(ns lein2.more-test
  (:require [clojure.test :refer :all]
            [lein2.core :as core]))

(deftest cows
  (is (= :moo (:sound {:sound :moo}))))

