(ns app.core
  (:require
    [reagent.dom.client :as rdc]
    [lambdaisland.glogi :as log]
    [lambdaisland.glogi.console :as glogi-console]
    [app.pages :as pages]))

(glogi-console/install!)

(log/set-levels
  {:glogi/root :info
   'app.lightning :trace})

(defonce root-container
  (rdc/create-root (js/document.getElementById "app")))

(defn ^:export init []
  (rdc/render root-container [pages/page]))

(defn clear-cache-and-render!
  []
  (init))
