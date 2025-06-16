(ns server.api
  (:require
    ["lnurl" :as lnurl]
    ["node:crypto" :as crypto]
    [lambdaisland.uri :as li-uri]))

(def config
  {:base-url "https://apps.mad.is/api/login"})

(defn- signature-valid?
  [sig k1 key]
  (lnurl/verifyAuthorizationSignature sig k1 key))

(defn- generate-random-hex-id
  []
  (.toString (crypto/randomBytes 32) "hex"))

(defn- generate-lnurl
  [query-params]
  (let [base-url (config :base-url)]
    (-> base-url
        (li-uri/parse ,,,)
        (li-uri/assoc-query ,,, query-params)
        (li-uri/uri-str ,,,))))

(defn- handle-login
  [sig k1 key]
  (if (signature-valid? sig k1 key)
    {:status "OK"}
    {:status "ERROR" :reason "Login unsuccessful due to failure in verifying signature"}))

(defn post-data
  [{:keys [json query]}]
  {:json {:original-json json :original-query query :status 200}})

(defn get-data
  [{:keys [query]}]
  {:json {:nothing "Here" :original-query query} :status 200})

(defn login-status
  [_]
  {:json {:status "OK"}})

(defn lnurl
  [_]
  (let [hex-id (generate-random-hex-id)
        url-params {:k1 hex-id :tag "login"}
        url (generate-lnurl url-params)]
    {:json {:lnurl (lnurl/encode url) :url url}}))

(defn login
  [{:keys [query]}]
  (let [{:keys [sig k1 key]} query]
    {:json (handle-login sig k1 key)}))

(defn get-config
  [_]
  {:json {:api-base-url "http://example.com"}})
