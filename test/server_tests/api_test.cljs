(ns server-tests.api-test
  (:require
    [server-tests.setup]
    [server.api :as api]
    [app.lightning :as jl]
    [cljs.test :refer-macros [deftest is testing async]]
    [lambdaisland.glogi :as log]
    [cljs.core.async :refer [go] :as async]))

(def test-node "http://127.0.0.1:8184")
(def test-rune "")

(deftest login-flow
  (testing "/login-status"
    (is (= 1 1))))
