(ns server.core
  (:require [oops.core :refer [oget oset! ocall]]
            ["express" :as express]
            ["path" :as path]
            ["node:crypto" :as crypto]
            ["lnurl" :as lnurl]
            ["morgan" :as morgan]))

;; Initialize Express app
;; Docs: https://expressjs.com/en/5x/api.html
(def app (express))
(.set app "query parser" "simple")

(defn cors-middleware
  [_req res next]
    (.header ^js res "Access-Control-Allow-Headers" "Origin, X-Requested-With Content-Type Accept")
    (.header ^js res "Access-Control-Allow-Headers" "X-Requested-With")
    (.header ^js res "Access-Control-Allow-Headers" "Access-Control-Allow-Origin")
    (.header ^js res "Access-Control-Allow-Origin" "*")
    (next))

(.use app cors-middleware)
;; Middleware to parse JSON bodies
(.use app (.json express))
(.use app (.urlencoded express #js {"extended" true}))

;; Serve static files from the 'public' directory
(.use app (.static express (.resolve path "public")))
(.use app (morgan "dev"))

;; API Endpoints
(.get app "/api/data"
      (fn [req res]
        (.json res (clj->js {:message "Hello from GET endpoint!"
                             :timestamp (.getTime (js/Date.))}))))
(.post app "/api/data"
       (fn [req res]
         (let [body (js->clj (oget req :body) :keywordize-keys true)]
           (.json res (clj->js {:received (:message body)
                                :response "Processed by POST endpoint"
                                :timestamp (.getTime (js/Date.))})))))

(defn js-obj->clj-map [js-obj]
  (into {}
        (map (fn [k] [(keyword k) (aget js-obj k)]))
        (js/Object.keys js-obj)))

(defn signature-valid?
  [sig k1 key]
  (lnurl/verifyAuthorizationSignature sig k1 key))

(defn generate-lnurl
  []
  (let [base-url "https://apps.mad.is/api/login"]
    (str base-url "?tag=login&k1=" (.toString (crypto/randomBytes 32) "hex"))))

(defn handle-login
  [sig k1 key]
  (if (signature-valid? sig k1 key)
    {:status "OK"}
    {:status "ERROR" :reason "Login unsuccessful due to failure in verifying signature"}))

(def login-req (atom nil))

(.get app "/api/lnurl"
      (fn [_req res]
        (let [url (generate-lnurl)]
          (js/console.log "Requested /api/lnurl")
          (.json res (clj->js {:lnurl (lnurl/encode url)
                               :url url})))))

(.get app "/api/login"
       (fn [req res]
         (let [query-params (js-obj->clj-map (oget req :query))
               {:keys [sig k1 key]} query-params]
           (reset! login-req req)
           (.json res (clj->js (handle-login sig k1 key))))))

;; Start the server
(defn main []
  (.listen app 3000
           (fn []
             (js/console.log "Server running on http://localhost:3000"))))
