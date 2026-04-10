(ns app.ui.pages.hodl-invoice.events
  (:require
    [re-frame.core :as rf]
    ["qrcode" :as QRCode]
    [goog.crypt :as crypt]
    [goog.crypt.Sha256]
    [app.ui.pages.hodl-invoice.helpers :refer [step-key step-data]]
    [app.ui.pages.hodl-invoice.config :as hodl-invoice.config]
    [lambdaisland.glogi :as log]))

(defn node-api-request-params
  [{:keys [api-base-url rune]}
   command
   params
   {:keys [on-success on-failure]}]
  [:fetch
   {:method :post
    :headers {"Rune" rune
              "Content-Type" "application/json"
              "Accept" "application/json"}
    :url (str api-base-url "/v1/" command)
    :mode :cors
    :body params
    :request-content-type :json
    :response-content-types {#"application/.*json" :json}
    :credentials :same-origin
    :on-success on-success
    :on-failure on-failure}])

(rf/reg-event-db
  :hodl-invoice/use-connection-defaults
  (fn [db [_ [role _ :as role-step]]]
    (assoc-in db (step-key role-step) (get hodl-invoice.config/dev-node-conf role))))

(rf/reg-event-fx
  :hodl-invoice/set-api-base-url
  [(rf/inject-cofx :store)]
  (fn [{:keys [db store]} [_ role-step value]]
    {:db (assoc-in db (step-key role-step :api-base-url) (not-empty value))
     :store (assoc-in store (step-key role-step :api-base-url) (not-empty value))}))

(rf/reg-event-fx
  :hodl-invoice/set-rune
  [(rf/inject-cofx :store)]
  (fn [{:keys [db store]} [_ role-step value]]
    {:db (assoc-in db (step-key role-step :rune) (not-empty value))
     :store (assoc-in store (step-key role-step :rune) (not-empty value))}))

(rf/reg-event-db
  :hodl-invoice/payable-invoices-result
  (fn [db [_ result]]
    (assoc-in db [:hodl-invoice :payable-invoices] [(:body result)])))

(rf/reg-event-db
  :hodl-invoice/payable-invoices-error
  (fn [_db [_ error]]
    (js/console.error ">>> :hodl-invoice/payable-invoices-error" error)))

(rf/reg-event-fx
  :hodl-invoice/request-payable-invoices
  (fn [{:keys [db]} _]
    {:fx [(node-api-request-params
            (get-in db [:hodl-invoice :receiver])
            "decode"
            [(get-in db [:hodl-invoice :receiver :invoice :bolt11])]
            {:on-success [:hodl-invoice/payable-invoices-result]
             :on-failure [:hodl-invoice/payable-invoices-error]})]}))

(rf/reg-event-fx
  :hodl-invoice/set-connection-status-ok
  (fn [{:keys [db] :as cofx} [_ role-step result]]
    (let [node-info {:alias (get-in result [:body :alias])
                     :version (get-in result [:body :version])}]
      {:db (-> db
               (assoc-in ,,, (step-key role-step :status) :ok)
               (update-in ,,, (step-key role-step :result) merge node-info))})))

(rf/reg-event-db
  :hodl-invoice/set-connection-status-error
  (fn [db [_ role-step _result]]
    (-> db
        (assoc-in ,,, (step-key role-step :status) :error)
        (assoc-in ,,, (step-key role-step :result) nil))))

(rf/reg-event-db
  :hodl-invoice/set-new-invoice-amount
  (fn [db [_  amount]]
    (assoc-in db (step-key [:receiver :generate-invoice] :new-invoice-amount) amount)))

(rf/reg-event-fx
  :hodl-invoice/check-connection
  (fn [{:keys [db]} [_ role-step]]
    {:db (assoc-in db (step-key role-step :status) :in-progress)
     :fx [(node-api-request-params
          (select-keys (step-data db role-step) [:api-base-url :rune])
          "getinfo"
          []
          {:on-success [:hodl-invoice/set-connection-status-ok role-step]
           :on-failure [:hodl-invoice/set-connection-status-error role-step]})]}))

(rf/reg-event-db
  :hodl-invoice/pay-invoice-ok
  (fn [db [_ result]]
    (log/info :pay-invoice-ok result)
    (assoc-in db (step-key [:sender :pay-invoice]) {:status :ok :result result})))

(rf/reg-event-db
  :hodl-invoice/pay-invoice-error
  (fn [db [_ result]]
    (log/error :pay-invoice-error result)
    (assoc-in db (step-key [:sender :pay-invoice]) {:status :error
                                                    :error result})))

;; Continuation:
;;   store the invstring along with payable invoice after decoding it
(rf/reg-event-fx
  :hodl-invoice/pay-invoice
  (fn [{:keys [db]} [_ bolt11]]
    (log/info :pay-invoice bolt11)
    {:db (assoc-in db (step-key [:sender :pay-invoice] :status) :waiting-next)
     :fx [(node-api-request-params
            (select-keys (step-data db [:sender :check-connection]) [:api-base-url :rune])
            "xpay"
            {"invstring" bolt11}
            {:on-success [:hodl-invoice/pay-invoice-ok]
             :on-failure [:hodl-invoice/pay-invoice-error]})]}))

(rf/reg-event-fx
  :hodl-invoice/settle-invoice
  (fn [{:keys [db]} [_ preimage]]
    (log/info :settle-invoice preimage)
    {:db (assoc-in db (step-key [:sender :settle-invoice] :status) :in-progress)
     :fx [(node-api-request-params
            (select-keys (step-data db [:receiver :check-connection]) [:api-base-url :rune])
            "settleholdinvoice"
            [preimage]
            {:on-success [:hodl-invoice/settle-invoice-ok]
             :on-failure [:hodl-invoice/settle-invoice-error]})]}))

(rf/reg-event-db
  :hodl-invoice/settle-invoice-ok
  (fn [db [_ result]]
    (log/info :settle-invoice-ok result)
    (assoc-in db (step-key [:receiver :settle-invoice]) {:status :ok :result result})))

(rf/reg-event-db
  :hodl-invoice/settle-invoice-error
  (fn [db [_ result]]
    (log/error :settle-invoice-error result)
    (assoc-in db (step-key [:receiver :settle-invoice]) {:status :error
                                                    :error result})))
(rf/reg-event-db
  :hodl-invoice/invoice-qr-ready
  (fn [db [_ qr-data]]
    (-> db
        (assoc-in ,,, (step-key [:receiver :show-invoice-info]) {:qr-code qr-data
                                                                 :status :ok}))))

(rf/reg-event-fx
  :hodl-invoice/generate-invoice-ok
  (fn [{:keys [db]} [_ preimage result]]
    (let [invoice {:bolt11 (get-in result [:body :bolt11])
                   :preimage (:preimage preimage)
                   :payment-hash (:payment-hash preimage)}]
      (.then (.toDataURL QRCode (get-in result [:body :bolt11]))
             #(rf/dispatch [:hodl-invoice/invoice-qr-ready %]))
      {:db (-> db
               (assoc-in ,,, (step-key [:receiver :generate-invoice] :result) invoice)
               (assoc-in ,,, (step-key [:receiver :generate-invoice] :status) :ok))})))

(rf/reg-event-fx
  :hodl-invoice/generate-invoice-error
  (fn [{:keys [db]} [_ error]]
    (let []
      (js/console.log "ERROR" error)
      {:db (assoc-in db [:hodl-invoice :receiver :invoice] error)})))

(defn sha256
  "Uint8Array → lowercase hex string"
  [bytes]
  (let [hasher (new goog.crypt.Sha256)]
    (.update hasher bytes)
    (->> (.digest hasher)
         (crypt/byteArrayToHex))))

(defn generate-preimage
  []
  (let [random-bytes (js/crypto.getRandomValues (js/Uint8Array. 32))
        preimage-hex (crypt/byteArrayToHex random-bytes)
        hash (sha256 random-bytes)]
    [preimage-hex hash]))

(rf/reg-event-fx
  :hodl-invoice/generate-invoice
  (fn [{:keys [db]} _event]
    (let [amount (step-data db [:receiver :generate-invoice] :new-invoice-amount)
          [preimage-hex payment-hash] (generate-preimage)]
      {:fx [(node-api-request-params
              (select-keys (step-data db [:receiver :check-connection]) [:api-base-url :rune])
              "holdinvoice"
              {"amount" amount "payment_hash" payment-hash}
              {:on-success [:hodl-invoice/generate-invoice-ok {:preimage preimage-hex :payment-hash payment-hash}]
               :on-failure [:hodl-invoice/generate-invoice-error]})]})))
