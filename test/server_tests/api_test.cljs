(ns server-tests.api-test
  (:require
    [server-tests.setup]
    [server.api :as api]
    [cljs.test :refer-macros [deftest is testing]]))

(def valid-login-params
  {:k1 "6736034edea80996cd23a1187d3dacf333ae23f6e7f955f80771ba3e44ee4c75",
   :tag "login",
   :sig "3045022100bed36485ba3ca965efc319bd9a8d9449cc5f2d92af024fdfb69b6fdfa1760fe60220755c496a5a19a4a027e0028f6ff2b7f57b684eea981c03ce690c54b290f59c82",
   :key "022d4f5c4b963283c04ee5599a667e15dec202546d7728a2ba10ec5cba914086c2"})

(deftest login-flow
  (testing "/login-status after a successful login"
    (let [_ (api/login {:query valid-login-params})
          login-status (api/login-status {:query {:sid (:k1 valid-login-params)}})
          non-login-status (api/login-status {:query {:sid "does not exist"}})]
      (is (= "OK" (-> login-status :json :status)))
      (is (= "ERROR" (-> non-login-status :json :status)))))

  (testing "/login-status after failed login"
    (let [invalid-login-params (assoc valid-login-params :sig (str "X" (subs (:sig valid-login-params) 1)))
          login-result (api/login {:query invalid-login-params})
          login-status (api/login-status {:query {:sid (:k1 invalid-login-params)}})]
      (is (= "ERROR" (-> login-result :json :status)))
      (is (= "ERROR" (-> login-status :json :status))))))
