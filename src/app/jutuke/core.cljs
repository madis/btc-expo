(ns jutuke.core
  (:require
    [reagent.dom.client :as rdc]
    [jutuke.pages :as pages]))

(defonce root-container
  (rdc/create-root (js/document.getElementById "app")))


(defn ^:export init []
  (rdc/render root-container [pages/page]))

(defn clear-cache-and-render!
  []
  (init))
