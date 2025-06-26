(ns server.core
  (:require
    ["express" :as express]
    ["path" :as path]
    ["morgan" :as morgan]
    [lambdaisland.glogi :as log]
    [oops.core :refer [oget]]
    ; Mount modules import
    [server.config]
    [server.storage.postgres]
    [mount.core :as mount]
    [server.middleware :as middleware]
    [server.api :as api]))

;; Initialize Express app
;; Docs: https://expressjs.com/en/5x/api.html
(def app (express))
(.set app "query parser" "simple")

(log/set-levels
  {:glogi/root :info})

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
(.get app "/api/profiles" (middleware/wrap-clojure-handler api/profiles))
(.post app
       "/api/profile"
       (middleware/create-upload-middleware "avatar-file" "public/uploads")
       (middleware/wrap-clojure-handler api/profile))

;; Start the server
(defn main []
  (mount/in-cljc-mode)
  (mount/start
    #'server.config/config
    #'server.storage.postgres/storage)
  (.listen app 3000 #(js/console.log "Server running on http://localhost:3000")))

(comment
  (main))
