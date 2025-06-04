(ns app.ui.events
  (:require
    [re-frame.core :as re]
    [superstructor.re-frame.fetch-fx]))

(def initial-db-state
  {})

(re/reg-event-db
  :initialize-db
  (fn [db _]
    (merge db initial-db-state)))
