(ns com.jakemccrary.test-refresh-test
  (:require [com.jakemccrary.test-refresh :refer :all]
            [clojure.test :refer :all]))

;(deftest test-should-notify?
;  (testing "if notify-on-success is true then all results should notify"
;    (is (should-notify? true {:status "Passed" :message "Testing"}))
;    (is (should-notify? true {:status "Failed" :message "Testing"}))
;    (is (should-notify? true {:status "Error" :message "Testing"})))
;
;  (testing "when notify-on-success false then Passed results do not notify"
;    (is (not (should-notify? false {:status "Passed" :message "Testing"})))
;    (is (should-notify? false {:status "Failed" :message "Testing"}))
;    (is (should-notify? false {:status "Error" :message "Testing"}))))
;
;(defn a-fn [& _] nil)
;(defn another-fn [& _] nil)
;
;(deftest test-selecting-vars
;  (testing "Selects vars matching selector function"
;    (with-redefs [a-fn (vary-meta another-fn merge {:integration true})
;                  another-fn (vary-meta another-fn merge {:fast true})]
;      (let [vs [#'a-fn #'another-fn]]
;        (= [#'a-fn] (select-vars :integration vs))
;        (= [] (select-vars :no-match vs))))))
;
;(deftest test-notification
;  (testing "lololol"))
