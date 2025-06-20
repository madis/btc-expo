(ns server.middleware
  (:require
    [server.js-helpers :refer [js-obj->clj-map]]
    [oops.core :refer [oget]]))

(defn cors
  [_ res next]
    (.header ^js res "Access-Control-Allow-Headers" "Origin, X-Requested-With Content-Type Accept")
    (.header ^js res "Access-Control-Allow-Headers" "X-Requested-With")
    (.header ^js res "Access-Control-Allow-Headers" "Access-Control-Allow-Origin")
    (.header ^js res "Access-Control-Allow-Origin" "*")
    (next))

(defn wrap-clojure-handler
  "Calls the handler with a map consisting of query params map and parsed JSON
  body Expects handler to return a map consisting of :status and :json
  (handler {:json <parsed json map> :query <parsed query params})
   => {:status <HTTP status code> :json <JSON map>}"
  [handler]
  (fn [req res next]
    (let [query (oget req :query)
          clj-query (js-obj->clj-map query)
          json-body (js->clj (oget req :body) :keywordize-keys true)
          {:keys [json status]} (handler {:query clj-query :json json-body})]
      (when json (.json res (clj->js json)))
      (.status res (or status 200))
      (next))))
