(ns app.ui
  (:require
    [mount.core :refer [defstate]]
    [reagent.dom.client :as rdc]
    [superstructor.re-frame.fetch-fx]
    [lambdaisland.glogi :as log]
    [app.ui.layout :as layout]
    ))


(declare start)
(declare stop)

(defstate state
  :start (start)
  :stop (stop))

(defonce root-container
  (rdc/create-root (js/document.getElementById "app")))

(defn start
  []
  (log/info :ui/start "Starting UI module")
  (rdc/render root-container [layout/show])
  root-container)

(defn stop
  []
  (log/info :ui/stop "Stopping UI module"))
