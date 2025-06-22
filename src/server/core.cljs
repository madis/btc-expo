(ns server.core
  (:require ["express" :as express]
            ["path" :as path]
            ["morgan" :as morgan]
            ["multer" :as multer]
            [oops.core :refer [oget]]
            [server.middleware :as middleware]
            [server.api :as api]))

;; Initialize Express app
;; Docs: https://expressjs.com/en/5x/api.html
(def app (express))
(.set app "query parser" "simple")


(.use app middleware/cors)
;; Middleware to parse JSON bodies
(.use app (.json express))
(.use app (.urlencoded express #js {"extended" true}))

;; Serve static files from the 'public' directory
(.use app (.static express (.resolve path "public")))
(.use app (morgan "dev"))

;; API Endpoints
(.get app "/api/data" (middleware/wrap-clojure-handler api/get-data))
(.post app "/api/data" (middleware/wrap-clojure-handler api/post-data))
(.get app "/api/lnurl" (middleware/wrap-clojure-handler api/lnurl))
(.get app "/api/login" (middleware/wrap-clojure-handler api/login))
(.get app "/api/login-status" (middleware/wrap-clojure-handler api/login-status))
(.get app "/api/config" (middleware/wrap-clojure-handler api/get-config))


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

(.post app "/api/profile"
       (create-upload-middleware "avatar-file" "public/uploads")
       (middleware/wrap-clojure-handler api/profile))

;; Start the server
(defn main []
  (.listen app 3000 #(js/console.log "Server running on http://localhost:3000")))

(comment
  (main))
