(ns server.login-helpers
  (:require
    ["lnurl" :as lnurl]
    [lambdaisland.uri :as li-uri]
    ["node:crypto" :as crypto]))

(defn validate-signature
  [sig k1 key]
  (try
    [(lnurl/verifyAuthorizationSignature sig k1 key)]
    (catch js/Error e
      [false e])))

(defn generate-random-hex-id
  []
  (.toString (crypto/randomBytes 32) "hex"))

(defn generate-lnurl
  [base-url query-params]
  (let [base-url base-url]
    (-> base-url
        (li-uri/parse ,,,)
        (li-uri/assoc-query ,,, query-params)
        (li-uri/uri-str ,,,))))

(defn handle-login
  [sig k1 key]
  (let [[valid? maybe-error] (validate-signature sig k1 key)]
    (if valid?
      {:status "OK"}
      {:status "ERROR" :reason (.-message maybe-error)})))
