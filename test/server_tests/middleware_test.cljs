(ns server-tests.middleware-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [server.middleware :as middleware]
    [oops.core :refer [ocall]]
    ["node-mocks-http" :as http-mocks]
    ["express" :as express]))

(deftest middleware-tests
  (testing "wrap-clojure-handler GET"
    (let [req (.createRequest http-mocks #js {:method "GET"
                                              :url "/endpoint?query-counter=5"})
          res (.createResponse http-mocks)
          next-called? (atom false)
          handler (fn [input]
                    {:status 201 :json {:status "OK"
                                        :counter (+ 1 (-> input :query :query-counter))}})
          wrapped-handler (middleware/wrap-clojure-handler handler)]
      (wrapped-handler req res #(reset! next-called? true))
      (is (= 201 (.-statusCode res)))
      (is (= {"status" "OK" "counter" 6} (js->clj (ocall res "_getJSONData"))))
      (is (= @next-called? true))))

  (testing "wrap-clojure-handler POST"
    (let [req (.createRequest http-mocks #js {:method "POST"
                                              :url "/endpoint"
                                              :headers {"Content-Type" "application/json"}
                                              :body {:body-counter 1}})
          res (.createResponse http-mocks)
          next-called? (atom false)
          handler (fn [input]
                    {:status 201 :json {:status "OK" :counter (+ 1 (-> input :json :body-counter))}})
          wrapped-handler (middleware/wrap-clojure-handler handler)]
      ((.json express) req res identity) ; Manually apply express's JSON middleware
      (wrapped-handler req res (fn [_] (reset! next-called? true)))
      (is (= 201 (.-statusCode res)))
      (is (= {"status" "OK" "counter" 2} (js->clj (ocall res "_getJSONData"))))
      (is (= @next-called? true)))))
