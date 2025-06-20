(ns app.ui.routing
  (:require
    [lambdaisland.glogi :as log]
    [re-frame.core :as re-frame]
    [reitit.frontend]
    [reitit.frontend.controllers :as rfc]
    [reitit.frontend.easy :as rfe]
    [reitit.coercion.spec :as rss]))

;;; Effects ;;;

;; Triggering navigation from events.

(re-frame/reg-fx :push-state
  (fn [route]
    (apply rfe/push-state route)))

;;; Events ;;;

(re-frame/reg-event-fx ::push-state
  (fn [_ [_ & route]]
    {:push-state route}))

(re-frame/reg-event-db ::navigated
  (fn [db [_ new-match]]
    (let [old-match   (:current-route db)
          controllers (rfc/apply-controllers (:controllers old-match) new-match)]
      (assoc db :current-route (assoc new-match :controllers controllers)))))

;;; Subscriptions ;;;

(re-frame/reg-sub
  ::current-route
  (fn [db _]
    (:current-route db)))

(defn on-navigate [new-match]
  (when new-match
    (re-frame/dispatch [::navigated new-match])))

(defn make-router
  [routes]
  (reitit.frontend/router routes {:data {:coercion rss/coercion}}))

(defn start
  [router]
  (log/info :routes "Starting routes")
  (rfe/start! router on-navigate {:use-fragment false}))
