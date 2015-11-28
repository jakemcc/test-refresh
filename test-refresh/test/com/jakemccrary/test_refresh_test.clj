(ns com.jakemccrary.test-refresh-test
  (:require [com.jakemccrary.test-refresh :refer :all]
            [clojure.test :refer :all]))

(deftest test-should-notify?
  (testing "if notify-on-success is true then all results should notify"
    (is (should-notify? true {:status "Passed" :message "Testing"}))
    (is (should-notify? true {:status "Failed" :message "Testing"}))
    (is (should-notify? true {:status "Error" :message "Testing"})))

  (testing "when notify-on-success false then Passed results do not notify"
    (is (not (should-notify? false {:status "Passed" :message "Testing"})))
    (is (should-notify? false {:status "Failed" :message "Testing"}))
    (is (should-notify? false {:status "Error" :message "Testing"}))))

