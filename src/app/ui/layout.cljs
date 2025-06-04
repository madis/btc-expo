(ns app.ui.layout
  (:require
    [app.ui.pages.operations]))

(defn show
  [content & args]
  ; Example design: https://bulmatemplates.github.io/bulma-templates/templates/cheatsheet.html
  ; Another example: https://bulmatemplates.github.io/bulma-templates/templates/admin.html
  [:section.section
   [:div.container
    [:div.columns
     [:div.column.is-3
      [:aside.is-medium.menu
       [:p.menu-label "Categories"]]]
     [:div.column.is-9
      [app.ui.pages.operations/show]]]]])
