(ns app.core
  (:require
    [lambdaisland.glogi :as log]
    [lambdaisland.glogi.console :as glogi-console]
    [mount.core :as mount]
    [app.ui]))

(glogi-console/install!)

(log/set-levels
  {:glogi/root :info
   'app.lightning :trace})

(defn start!
  []
  (log/info :starting "Starting the application")
  (mount/start))

(defn stop!
  []
  (log/info :stopping "Stopping the application")
  (mount/stop))

(defn ^:export init []
  (log/info :init "init called")
  (start!))
