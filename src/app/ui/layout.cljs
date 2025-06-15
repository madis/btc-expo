(ns app.ui.layout
  (:require
    [re-frame.core :as rf]
    [reitit.frontend.easy :as rfe]
    [reitit.core :as r]))

(defn menu-item-links
  [routes current-route]
  (map (fn [route]
    [:li.is-right
     [:a {:class (if
                   (= (-> route :data :name) (-> current-route :data :name))
                   [:is-active]
                   [])
          :href (rfe/href (-> route :data :name))}
      (-> route :data :link-text)]])
  routes))

(defn show
  []
  ; Example design: https://bulmatemplates.github.io/bulma-templates/templates/cheatsheet.html
  ; Another example: https://bulmatemplates.github.io/bulma-templates/templates/admin.html
  (let [[router current-route] @(rf/subscribe [:app.ui.routing/route-info])
        route-names (r/route-names router)
        routes (map #(r/match-by-name router %) route-names)
        menu-items (menu-item-links routes current-route)]
    [:section.section
     [:div.container
      [:div.columns
       [:div.column.is-3
        [:aside.is-medium.menu
         [:p.menu-label "Actions"]
         (into [:ul.menu-list] menu-items)]]
       [:div.column.is-9
        [(-> current-route :data :view)]]]]]))
