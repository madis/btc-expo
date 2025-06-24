(ns app.subscriptions
  (:require [re-frame.core] :as rf))

(rf/reg-sub
  :config
  (fn [db [_ config-key]]
    (get-in db (if config-key [:config config-key] [:config]))))
