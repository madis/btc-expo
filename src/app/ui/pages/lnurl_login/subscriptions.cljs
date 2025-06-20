(ns app.ui.pages.lnurl-login.subscriptions
  (:require
    [re-frame.core :as rf]))

(rf/reg-sub
  ::login-lnurl
  (fn [db _]
    (get-in db [:operations :lnurl :data-url])))

(rf/reg-sub
  ::login-url
  (fn [db _]
    (get-in db [:operations :lnurl :url])))

(rf/reg-sub
  ::login-status
  (fn [db _]
    (get-in db [:login :status])))
