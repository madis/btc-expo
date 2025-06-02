(ns app.lightning
  (:require
    [lambdaisland.fetch :as fetch]
    [lambdaisland.glogi :as log]
    [cljs.core.async :as async]))

(defonce client (atom {}))

(defn p->ch
  [promise]
  (let [out (async/chan)]
    (.then promise #(async/put! out %) #(async/put! out %))
    out))

(defn init-client
  [node-url rune]
  (reset! client {:node-url node-url :rune rune}))

(defn get-node-url [] (get @client :node-url))

(defn get-endpoint-url [method]
  (str (get-node-url) "/v1/" (name method)))

(def api-methods
  {:listmethods {:method :get}
   :getinfo {:method :post}
   :listfunds {:method :post}})

(defn call [method & args]
  (p->ch (fetch/request
           (get-endpoint-url method)
           {:method (get-in api-methods [method :method])
            :as :json
            :headers {"Rune" (:rune @client)}})))

(defn get-balance
  []
  (call :getinfo))
