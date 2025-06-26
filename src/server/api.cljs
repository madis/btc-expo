(ns server.api
  (:require
    ["lnurl" :as lnurl]
    [server.login-helpers :as login-helpers]
    [server.config :refer [config storage]]
    [server.storage :refer [get-from-store add-to-store! in-store?]]
    [server.storage.atom-storage]
    [clojure.core.async :refer [go chan <! >! put! sliding-buffer]]))


(defn post-data
  [{:keys [json query]}]
  {:json {:original-json json :original-query query} :status 200})

(def info-channel (chan (sliding-buffer 3)))
(put! info-channel "First info")

(defn get-data
  [{:keys [query]}]
  ; (new js/Promise
  ;      (fn [resolve reject]
  ;        (js/setTimeout #(resolve {:json {:original-query query} :status 200}) 5000)))
  (go
    (let [new-info (<! info-channel)]
      (when (:info query) (>! info-channel (:info query)))

      {:json {:new-info new-info}})))

(defn login-status
  [{:keys [query]}]
  (let [session-id (:sid query)
        session (get-from-store @storage session-id)]
    (if (= (:status session) "OK")
      {:json {:status "OK"}}
      {:json {:status "ERROR" :reason "No session found"}})))

(defn lnurl
  [_]
  (let [hex-id (login-helpers/generate-random-hex-id)
        url-params {:k1 hex-id :tag "login"}
        api-url (str (@config :base-url) "/api/login")
        url (login-helpers/generate-lnurl api-url url-params)]
    (add-to-store! @storage hex-id nil)
    {:json {:lnurl (lnurl/encode url) :url url}}))

(defn login
  [{:keys [query]}]
  (let [{:keys [sig k1 key]} query
        login-result (login-helpers/handle-login sig k1 key)]
    (add-to-store! @storage k1 login-result)
    {:json login-result}))

(defn get-config
  [_]
  {:json (select-keys @config [:base-url])})

(def ul (atom nil))

(defn profile
  [{:keys [json uploads]}]
  (reset! ul uploads)
  (let [file-name (-> uploads first :filename)
        upload-url (str (@config :uploads-root-url) "/" file-name)]
    {:json {:upload-url upload-url}}))
