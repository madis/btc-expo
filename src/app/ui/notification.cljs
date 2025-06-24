(ns app.ui.notification
  (:require
    [reagent.core]
    [re-frame.core :as rf]))

(rf/reg-event-fx
  :notification/show
  (fn [{:keys [db]} [_ content]]
    {:fx [[:dispatch-later [{:ms 3000 :dispatch [:notification/hide]}]]]
     :db (assoc-in db [:notification :content] content)}))

(rf/reg-event-db
  :notification/hide
  (fn [db [_ content]]
    (assoc-in db [:notification :content] nil)))

(rf/reg-sub
  :notification/content
  (fn [db _]
    (get-in db [:notification :content])))


(defn hide-notification
  []
  (rf/dispatch [:notification/hide]))

(defn show-notification
  [s]
  (rf/dispatch [:notification/show s]))

(defn notification-container
  []
  (let [notification (rf/subscribe [:notification/content])]
    [:div.notification-container
     [:div {:class [:notification :is-success (when @notification :is-visible)]}
      [:button.delete {:on-click #(rf/dispatch :notification/hide)}]
      @notification]]))
