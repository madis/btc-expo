(ns browser-tests.ui.pages.hodl-invoice.db-schema-test
  (:require
    [malli.core :as m]
    [malli.dev.cljs]
    [malli.dev.pretty]
    [cljs.test :refer-macros [deftest is testing]]
    [app.ui.pages.hodl-invoice.db :as hi-db]))

(deftest schema-tests
  (testing "ConnectionCheck"
    (let [good-data {:rune "abcde" :api-base-url "https://wut.com"}
          valid? (m/validator hi-db/ConnectionCheck)]
      (is (valid? good-data))
      (is (not (valid? (select-keys good-data [:rune])))))))
