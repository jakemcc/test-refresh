(ns example.hello-test
  (:require [example.hello :as hello]
            [clojure.test :refer [deftest is]]))

(deftest it-greets-you
  (is (= "Hello Jake" (hello/greet "Jake"))))

(deftest yolo
  (is (= 1 2)))
