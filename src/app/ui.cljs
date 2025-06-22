(ns app.ui
  (:require
    [mount.core :refer [defstate]]
    [reagent.dom.client :as rdc]
    [re-frame.core :as rf]
    [superstructor.re-frame.fetch-fx]
    [lambdaisland.glogi :as log]
    [app.ui.pages.clightning-operations :as pages.clightning-operations]
    [app.ui.pages.home :as pages.home]
    [app.ui.pages.lnurl-login :as pages.lnurl-login]
    [app.ui.pages.profile :as pages.profile]
    [app.ui.pages.configuration :as pages.configuration]
    [app.ui.routing :as routing]
    [app.ui.layout :as layout]))


(declare start)
(declare stop)

(defstate state
  :start (start)
  :stop (stop))

(rf/reg-event-db ::initialize-db
  (fn [db [_ initial-db-state]]
    (merge db initial-db-state)))

(defonce root-container
  (rdc/create-root (js/document.getElementById "app")))

(def routes
  [""
   [["/" {:name :home
          :link-text "Home"
          :view pages.home/show}]
    ["/core-lightning" {:name ::core-lightning
                        :link-text "Core Lightning"
                        :view pages.clightning-operations/show}]
    ["/lnurl-login" {:name ::lnurl-login
                     :link-text "LNURL Login"
                     :view pages.lnurl-login/show}]
    ["/profile" {:name :login
                 :link-text "Profile"
                 :view pages.profile/show}]
    ["/configuration" {:name ::configuration
                       :link-text "Configuration"
                       :view pages.configuration/show}]]])

(defn start
  []
  (let [router (routing/make-router routes)]
    (log/info :ui/start "Starting UI module")
    (rf/clear-subscription-cache!)
    (rf/dispatch-sync [::initialize-db {:router router}])
    (routing/start router)
    (rdc/render root-container [layout/show router])
    root-container))

(defn stop
  []
  ; (rf/clear-subscription-cache!)
  (log/info :ui/stop "Stopping UI module"))
