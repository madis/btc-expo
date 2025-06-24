(ns app.ui.pages.profile.subscriptions
  (:require
    [re-frame.core :as rf]))

(rf/reg-sub
  :profile/user-name
  (fn [db _]
    (get-in db [:profile :name])))

