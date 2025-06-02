(ns jutuke-tests.hello-test
  (:require
    [jutuke-tests.setup]
    [app.lightning :as jl]
    [cljs.test :refer-macros [deftest is testing async]]
    [lambdaisland.glogi :as log]
    [cljs.core.async :refer [go] :as async]))

(deftest jutuke-tests
  (testing "whether logic still works!!!!"
    (is (= (+ 1 1) 2))))

(def test-node "http://127.0.0.1:8184")
(def test-rune "")

(deftest querying-balance
  (testing "successfully getting balance"
    (async done
      (go
        (let [_ (jl/init-client test-node test-rune)
              res-chan (jl/call :listfunds)
              balance (async/<! res-chan)]

          (log/debug ">>> balance: " balance)
          (is (= balance 42))
          (done))))))
