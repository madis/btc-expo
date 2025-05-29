(ns jutuke.core
  (:require
    [reagent.core :as r]
    [reagent.dom.client :as rdc]
    [clojure.string :as str]
    [components.button :as button]))

(defonce timer (r/atom (js/Date.)))

(defonce time-color (r/atom "#f34"))

(defonce root-container
  (rdc/create-root (js/document.getElementById "app")))

(defonce time-updater (js/setInterval
                       #(reset! timer (js/Date.)) 1000))

(defn greeting [message]
  [:h1 message])

(defn clock []
  (let [time-str (-> @timer .toTimeString (str/split " ") first)]
    [:div.example-clock
     {:style {:color @time-color}}
     time-str]))

(defn color-input []
  [:div.color-input
   "Time color: "
   [:input {:type "text"
            :value @time-color
            :on-change #(reset! time-color (-> % .-target .-value))}]])

(defn simple-example []
  [:div
   [greeting "Hello world, it is now"]
   [clock]
   [color-input]
   [button/button 
    {:label "Hello" 
     :size "medium" 
     :background-color "is-primary" 
     :on-click (fn [] (println "Button clicked!"))}]])

(defn ^:export init []
  (rdc/render root-container [simple-example]))
