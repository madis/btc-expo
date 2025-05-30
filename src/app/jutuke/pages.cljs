(ns jutuke.pages
  (:require
    [components.button :as button]))

(defn page
  []
  ; Example design: https://bulmatemplates.github.io/bulma-templates/templates/cheatsheet.html
  ; Another example: https://bulmatemplates.github.io/bulma-templates/templates/admin.html
  [:section.section
   [:div.container
    [:div.columns
     [:div.column.is-3
      [:aside.is-medium.menu
       [:p.menu-label "Categories"]]]
     [:div.column.is-9
      [:div.content.is-medium
       [:h2.title.is-2 "Operations"]
       [:div.box
        [:h4.title.is-4 "Query balance"]
        [button/button {:label "Query"
                        :size "medium"
                        :background-color "is-primary"
                        :on-click (fn [] (println "Clicked!"))}]]]]]]])
