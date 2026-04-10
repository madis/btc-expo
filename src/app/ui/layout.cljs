(ns app.ui.layout
  (:require
    [re-frame.core :as rf]
    [reitit.frontend.easy :as rfe]
    [reitit.core :as r]))

(defn navbar-items
  [routes current-route]
  (map (fn [route]
         [:a.navbar-item
          {:key (-> route :data :name)
           :class (if (= (-> route :data :name) (-> current-route :data :name))
                    [:is-selected]
                    [])
           :href (rfe/href (-> route :data :name))}
          (-> route :data :link-text)])
       routes))

(defn show
  [router]
  (let [current-route @(rf/subscribe [:app.ui.routing/current-route])
        route-names (r/route-names router)
        routes (map #(r/match-by-name router %) route-names)
        routed-view (-> current-route :data :view)]
    [:div
     [:nav.navbar {:role "navigation" :aria-label "main navigation"}
      [:div.navbar-menu
       [:div.navbar-start (navbar-items routes current-route)]]]
     [:div.section [routed-view]]]))
