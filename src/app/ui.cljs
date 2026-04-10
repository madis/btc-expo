(ns app.ui
  (:require
    [mount.core :refer [defstate]]
    [reagent.dom.client :as rdc]
    [re-frame.core :as rf]
    [superstructor.re-frame.fetch-fx]
    [lambdaisland.glogi :as log]
    [app.ui.events]
    [app.ui.pages.clightning-operations :as pages.clightning-operations]
    [app.ui.pages.home :as pages.home]
    [app.ui.pages.lnurl-login :as pages.lnurl-login]
    [app.ui.pages.hodl-invoice :as pages.hodl-invoice]
    [app.ui.pages.profile :as pages.profile]
    [app.ui.pages.configuration :as pages.configuration]
    [app.ui.routing :as routing]
    [app.ui.layout :as layout]
    [app.ui.notification :as notification]
    [akiroz.re-frame.storage :refer [reg-co-fx!]]
    [app.ui.pages.hodl-invoice.db :as hodl-invoice.db]))


(declare start)
(declare stop)

(defstate state
  :start (start)
  :stop (stop))

(reg-co-fx! :my-app         ;; local storage key
            {:fx :store     ;; re-frame fx ID
             :cofx :store}) ;; re-frame cofx ID

(rf/reg-event-fx
  :initialize-db
  [(rf/inject-cofx :store)]
  (fn [{:keys [db store]} [_ initial-db-state keys-from-store]]
    (let []
      {:db (merge db initial-db-state)})))

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
    ["/hodl-invoice" {:name ::hodl-invoice
                     :link-text "HODL invoice"
                     :view pages.hodl-invoice/show}]
    ["/profile" {:name :login
                 :link-text "Profile"
                 :view pages.profile/show}]
    ["/configuration" {:name ::configuration
                       :link-text "Configuration"
                       :view pages.configuration/show}]]])

; Overwritten in compile time in shadow-cljs via closure-defines from API_URL env variable
(goog-define api-url "http://localhost:3000")
(def config {:api-url api-url})

(defn start
  []
  (let [router (routing/make-router routes)]
    (log/info :ui/start "Starting UI module")
    (rf/clear-subscription-cache!)
    (rf/dispatch-sync [:initialize-db {:router router :config config} [:hodl-invoice]])
    (routing/start router)
    (rdc/render root-container [:<>
                                [layout/show router]
                                [notification/notification-container]])
    root-container))

(defn stop
  []
  ; (rf/clear-subscription-cache!)
  (log/info :ui/stop "Stopping UI module"))
