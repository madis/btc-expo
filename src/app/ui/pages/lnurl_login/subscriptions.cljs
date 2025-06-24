(ns app.ui.pages.lnurl-login.subscriptions
  (:require
    [re-frame.core :as rf]))

(rf/reg-sub
  ::login-lnurl-qr-data
  (fn [db _]
    (get-in db [:operations :lnurl :qr-data])))

(rf/reg-sub
  ::login-lnurl
  (fn [db _]
    (get-in db [:operations :lnurl :lnurl])))

(rf/reg-sub
  ::login-url
  (fn [db _]
    (get-in db [:operations :lnurl :url])))

(rf/reg-sub
  ::login-status
  (fn [db _]
    (get-in db [:login :status])))
