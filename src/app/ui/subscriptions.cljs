(ns app.ui.subscriptions
  (:require
    [re-frame.core :as re]))

(re/reg-sub
  :navigation-menu-open?
  (fn [db _]
    (get-in db [:navigation-menu :open?])))

