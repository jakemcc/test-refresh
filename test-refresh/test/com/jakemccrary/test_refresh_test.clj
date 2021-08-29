(ns com.jakemccrary.test-refresh-test
  (:require [com.jakemccrary.test-refresh :as tr]
            [clojure.test :refer [deftest is testing]]))

(deftest test-should-notify?
  (testing "if notify-on-success is true then all results should notify"
    (is (tr/should-notify? true {:status "Passed" :message "Testing"}))
    (is (tr/should-notify? true {:status "Failed" :message "Testing"}))
    (is (tr/should-notify? true {:status "Error" :message "Testing"})))

  (testing "when notify-on-success false then Passed results do not notify"
    (is (not (tr/should-notify? false {:status "Passed" :message "Testing"})))
    (is (tr/should-notify? false {:status "Failed" :message "Testing"}))
    (is (tr/should-notify? false {:status "Error" :message "Testing"}))))

