(ns app.ui.pages.hodl-invoice
  (:require
    [app.ui.pages.hodl-invoice.events]
    [app.ui.pages.hodl-invoice.subscriptions]
    [app.ui.pages.hodl-invoice.views :as views]
    [lambdaisland.glogi :as log]
    [re-frame.core :as rf]))

(defmulti step-view (fn [[role step]] [role step]))

(defn- node-config-for-role
  [[role step :as role-step]]
  (let [api-base-url (rf/subscribe [:hodl-invoice/api-base-url role-step])
        rune (rf/subscribe [:hodl-invoice/rune role-step])
        status (rf/subscribe [:hodl-invoice/step-status role-step])
        error (rf/subscribe [:hodl-invoice/step-error role-step])
        validations (rf/subscribe [:hodl-invoice/step-validations role-step])
        on-submit #(rf/dispatch [:hodl-invoice/check-connection role-step])
        on-use-defaults #(rf/dispatch [:hodl-invoice/use-connection-defaults role-step])
        set-url #(rf/dispatch [:hodl-invoice/set-api-base-url role-step (-> % .-target .-value)])
        set-rune #(rf/dispatch [:hodl-invoice/set-rune role-step (-> % .-target .-value)])]
    (log/info :node-config-for-role @validations)
    [views/node-config
     {:on-submit on-submit :on-use-defaults on-use-defaults :set-url set-url :set-rune set-rune}
     {:url api-base-url :rune rune :status status :error error :validations validations}]))

(defmethod step-view [:sender :check-connection] [role-step]
  (node-config-for-role role-step))

(defmethod step-view [:receiver :check-connection] [role-step]
  (node-config-for-role role-step))

(defmethod step-view [:receiver :generate-invoice] [role-step]
  (let [amount (rf/subscribe [:hodl-invoice/new-invoice-amount])
        status (rf/subscribe [:hodl-invoice/step-status role-step])]
    [views/invoice-form
     {:amount amount
      :description "Hello"
      :on-change #(rf/dispatch [:hodl-invoice/set-new-invoice-amount
                                (js/parseInt (-> % .-target .-value))])
      :disabled? (= @status :ok)
      :on-submit #(rf/dispatch [:hodl-invoice/generate-invoice])}]))

(defmethod step-view [:receiver :show-invoice-info] [_ _]
  (let [invoice (rf/subscribe [:hodl-invoice/invoice])]
    [views/invoice-display (:bolt11 @invoice) (:qr-code @invoice)]))

(defmethod step-view [:sender :pay-invoice] [role-step]
  (let [invoices @(rf/subscribe [:hodl-invoice/payable-invoices])
        error (rf/subscribe [:hodl-invoice/step-error role-step])
        status (rf/subscribe [:hodl-invoice/step-status role-step])
        on-pay (fn [invoice] (rf/dispatch [:hodl-invoice/pay-invoice (:bolt11 invoice)]))]
    (views/pay-invoice {:invoices invoices
                         :disabled? (#{:ok :waiting-next} @status)
                         :error @error
                         :actions [{:title "Pay" :on-click on-pay}]})))

(defmethod step-view [:receiver :settle-invoice] [role-step]
  (let [invoice @(rf/subscribe [:hodl-invoice/invoice-to-settle])
        status (rf/subscribe [:hodl-invoice/step-status role-step])
        on-settle (fn [invoice] (rf/dispatch [:hodl-invoice/settle-invoice (:preimage invoice)]))]
    (views/settle-invoice {:invoice invoice
                           :disabled? (#{:ok :waiting-next} @status)
                           :actions [{:title "Settle" :on-click on-settle}]})))

(defmethod step-view [:sender :show-confirmation] [_ _]
  (let [invoice @(rf/subscribe [:hodl-invoice/invoice-to-settle])]
    [:div.content
     [:h1.is-2 "HOLD invoice paid"]]))

(defn- columns-slice
  [[role step] view]
  (let [sender-class (if (= :sender role) ["hold-panel-sender"] ["hold-panel-empty"])
        receiver-class (if (= :receiver role) ["hold-panel-receiver"] ["hold-panel-empty"])
        element-id (str (name role) "-" (name step))]
    [:div.columns {:key element-id :id element-id}
     [:div.column.is-half {:class sender-class} (when (= :sender role) view)]
     [:div.column.is-half {:class receiver-class} (when (= :receiver role) view)]]))

(defn show
  []
  (let [steps @(rf/subscribe [:hodl-invoice/steps])]
    [:div
     [:div.columns
      [:div.column.is-half.hold-panel-sender [:h1.title.is-2 "Sender"]]
      [:div.column.is-half.hold-panel-receiver [:h1.title.is-2 "Receiver"]]]
     (doall
       (for [[role-step _data] steps]
        (columns-slice role-step (step-view role-step))))]))

(comment
  (require '[re-frame.db :refer [app-db]])
  @re-frame.db/app-db
  (get-in @app-db [:hodl-invoice])
  (get-in @app-db [:hodl-invoice :steps-progress [:receiver :generate-invoice]])
  (get-in @app-db [:hodl-invoice :steps-progress [:sender :pay-invoice]])
  (get-in @app-db [:hodl-invoice :steps-progress [:receiver :settle-invoice]])
  )
