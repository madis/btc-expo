(ns app.ui.pages.profile.events
  (:require
    [app.queries :as queries]
    [re-frame.core :as rf]))

(rf/reg-event-db
  :profile/set-name
  (fn [db [_ value]]
    (assoc-in db [:profile :name] value)))

(rf/reg-event-db
  :profile/set-avatar
  (fn [db [_ value]]
    (assoc-in db [:profile :avatar] value)))

(rf/reg-event-fx
  :profile/save-success
  (fn [{:keys [db]} [_ result]]
    (let [parsed-body (js->clj (.parse js/JSON (get result :body)) :keywordize-keys true)
          avatar-url (:upload-url parsed-body)]
      {:fx [[:dispatch [:notification/show "Profile successfully saved"]]]
       :db (assoc-in db [:profile :avatar-url] avatar-url)})))

(rf/reg-event-db
  :profile/save-failure
  (fn [_ _]
    (js/console.log ">>> :profile/save-failure")))

(rf/reg-event-fx
  :profile/save
  (fn [{:keys [db]} _]
    (let [profile-fields {:name (get-in db [:profile :name])}
          avatar (get-in db [:profile :avatar])
          form-data (new js/FormData)
          api-endpoint (queries/api-url db "/api/profile")]
      (.append form-data "avatar-file" avatar)
      (.append form-data "json" (.stringify js/JSON (clj->js profile-fields)))
      {:fx [[:fetch {:method :post
                     :url api-endpoint
                     :mode :cors
                     :body form-data
                     :response-content-type :json
                     :credentials :omit
                     :on-success [:profile/save-success]
                     :on-failure [:profile/save-failure]}]]})))
