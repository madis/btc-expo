(ns server.middleware
  (:require
    [server.js-helpers :refer [js-obj->clj-map]]
    [oops.core :refer [oget]]
    ["multer" :as multer]
    [clojure.core.async :refer [take!]]
    [cljs.core.async.impl.channels :refer [ManyToManyChannel]]))

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
          ; {:keys [json status]} (handler handler-params)
          result (handler handler-params)
          promise? (= (type result) js/Promise)
          channel? (= (type result) ManyToManyChannel)
          respond-with-results (fn [{:keys [json status]}]
                                 (when json (.json res (clj->js json)))
                                 (.status res (or status 200)))]
      (if channel?
        (take! result respond-with-results)
        (if promise?
         (-> result
             (.then ,,, respond-with-results)
             (.catch ,,, next))
         (do
           (respond-with-results result)
           (next)))))))

(defn create-upload-middleware
  [form-field upload-folder]
  (let [config {:destination (fn [_req _file callback] (callback nil upload-folder))
               :filename
               (fn [_req file callback]
                 (let [unique-suffix (str form-field "-" (js/Date.now))
                       orig-filename (oget file "originalname")
                       new-filename (str unique-suffix "-" orig-filename)]
                   (callback nil new-filename)))}
        storage (.diskStorage multer (clj->js config))
        upload (multer (clj->js {:storage storage}))]
    (.single upload form-field)))
