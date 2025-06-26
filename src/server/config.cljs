(ns server.config
  (:require
    [mount.core :refer [defstate]]
    [oops.core :refer [oget]]
    [lambdaisland.glogi :as log]))

(goog-define base-url "http://localhost:3000")
(goog-define uploads-root-url "http://localhost:3000/uploads")

(defn start!
  []
  (log/info :config "Starting config")
  {:base-url base-url
   :uploads-root-url uploads-root-url
   :database-url (oget js/process [:env "DATABASE_URL"])})

(defstate config
  :start (start!)
  :stop (log/info :config "Stopping config"))

