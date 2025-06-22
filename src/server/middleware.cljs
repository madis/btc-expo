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

(defn safe-parse-json [s]
  (try
    (js->clj (.parse js/JSON s) :keywordize-keys true)
    (catch js/Error _error
      ; Return the original object (possibly already parsed)
      s)))

(defn extract-json-from-req
  [req]
  (if (= "GET" (oget req "method"))
    nil ; No body in GET
    (if (re-find #"multipart/form-data" (.get req "Content-Type"))
      (.-json (oget req "body"))
      (oget req :body))))

(defn wrap-clojure-handler
  "Calls the handler with a map consisting of query params map and parsed JSON
  body.

  If the request is multipart/form-data then the JSON is expected to be in form
  field called 'json'

  Expects handler to return a map consisting of :status and :json
  (handler {:json <parsed json map> :query <parsed query params})
   => {:status <HTTP status code> :json <JSON map>}"
  [handler]
  (fn [req res next]
    (let [query (oget req :query)
          uploaded-file (oget req "?file")
          clj-query (js-obj->clj-map query)
          raw-json-from-req (extract-json-from-req req)
          json-from-req (safe-parse-json raw-json-from-req)
          uploads (when uploaded-file {:uploads [(js->clj uploaded-file :keywordize-keys true)]})
          handler-params (merge {:query clj-query :json json-from-req} uploads)
          {:keys [json status]} (handler handler-params)]
      (when json (.json res (clj->js json)))
      (.status res (or status 200))
      (next))))
