(ns jutuke-tests.hello-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]))

(deftest jutuke-tests
  (testing "whether logic still works"
    (is (= (+ 1 1) 2))))
