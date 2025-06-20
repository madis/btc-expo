(ns server.api
  (:require
    ["lnurl" :as lnurl]
    [server.login-helpers :as login-helpers]
    [server.config :refer [config storage]]
    [server.storage :refer [get-from-store add-to-store! in-store?]]
    [server.storage.atom-storage]))


(defn post-data
  [{:keys [json query]}]
  {:json {:original-json json :original-query query} :status 200})

(defn get-data
  [{:keys [query]}]
  {:json {:original-query query} :status 200})

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
        url (login-helpers/generate-lnurl (@config :base-url) url-params)]
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
