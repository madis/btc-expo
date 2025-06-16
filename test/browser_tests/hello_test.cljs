(ns browser-tests.hello-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]))

(deftest sanity-checks
  (testing "sanity"
    (is (= 4 (+ 2 2)))))
