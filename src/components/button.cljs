(ns components.button)

(defn button [{:keys [label _primary size background-color on-click]}]
  [:button
   {:type "button"
    :class ["button" size background-color]
    :on-click on-click}
   label])
