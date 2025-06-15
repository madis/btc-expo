(ns app.ui.pages.configuration)

(defn show
  []
  [:div.box
   [:h4.title.is-4 "Configuration"]
   [:div.field
    [:label.label "Server base URL"]
    [:div.control
     [:input.input {:type "text" :placeholder "http://localhost:3000"}]]]
   [:div.field
    [:label.label "Core Lightning URL"]
    [:div.control
     [:input.input {:type "text" :placeholder "http://localhost:3000"}]]]
   [:div.field
    [:label.label "Core Lightning Rune"]
    [:div.control
     [:input.input {:type "text" :placeholder "9bGL9nKUV9P0Va1iWGniUWoET2rOZ3tvN0WXdsLvXR49MA=="}]]]])
