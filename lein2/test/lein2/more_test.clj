(ns lein2.more-test
  (:require [clojure.test :refer :all]
            [lein2.core :as core]))

(deftest
  ;; ^:test-refresh/focus
  ;; ^:f
  cows
  (is (= :moo (:sound {:sound :moo}))))

