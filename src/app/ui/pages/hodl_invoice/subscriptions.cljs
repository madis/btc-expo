(ns app.ui.pages.hodl-invoice.subscriptions
  (:require
    [re-frame.core :as rf]
    [app.ui.pages.hodl-invoice.config :as config]
    [app.ui.pages.hodl-invoice.helpers :refer [step-data step-key]]))

(rf/reg-sub
  :hodl-invoice/api-base-url
  (fn [db [_ role-step]]
    (step-data db role-step :api-base-url)))

(rf/reg-sub
  :hodl-invoice/rune
  (fn [db [_ role-step]]
    (step-data db role-step :rune)))

(rf/reg-sub
  :hodl-invoice/step-status
  (fn [db [_ role-step]]
    (step-data db role-step :status)))

(rf/reg-sub
  :hodl-invoice/step-error
  (fn [db [_ role-step]]
    (step-data db role-step :error)))


(rf/reg-sub
  :hodl-invoice/invoice
  (fn [db _]
    (when (get-in db (step-key [:receiver :show-invoice-info] :qr-code))
      {:bolt11 (step-data db [:receiver :generate-invoice] :result :bolt11)
       :qr-code (step-data db [:receiver :show-invoice-info] :qr-code)})))

(rf/reg-sub
  :hodl-invoice/payable-invoices
  (fn [db]
    (when (get-in db (step-key [:receiver :show-invoice-info] :qr-code))
      [{:amount (step-data db [:receiver :generate-invoice] :amount)
        :currency "msats"
        :bolt11 (step-data db [:receiver :generate-invoice] :result :bolt11)}])))

(rf/reg-sub
  :hodl-invoice/invoice-to-settle
  (fn [db]
    (when (get-in db (step-key [:receiver :show-invoice-info] :qr-code))
      {:amount (step-data db [:receiver :generate-invoice] :amount)
       :preimage (step-data db [:receiver :generate-invoice] :result :preimage)
       :payment-hash (step-data db [:receiver :generate-invoice] :result :payment-hash)
       :currency "msats"
       :bolt11 (step-data db [:receiver :generate-invoice] :result :bolt11)})))

(rf/reg-sub
  :hodl-invoice/new-invoice-amount
  (fn [db _]
    (step-data db [:receiver :generate-invoice] :amount)))

(rf/reg-sub
  :hodl-invoice/new-invoice-description
  (fn [db _]
    (step-data db [:receiver :generate-invoice] :description)))

(rf/reg-sub
  :hodl-invoice/steps
  (fn [db _]
    (let [steps config/steps
          steps-progress (get-in db [:hodl-invoice :steps-progress])
          steps-included (reduce (fn [acc role-step]
                                   (if (= (get-in steps-progress [role-step :status]) :ok)
                                     (conj acc role-step)
                                     (if (and (seq acc) (#{:ok :waiting-next}
                                                          (get-in steps-progress [(last acc) :status])))
                                       (conj acc role-step)
                                       (reduced acc))))
                                 []
                                 steps)
          steps-or-first (if (seq steps-included) steps-included [(first steps)])]
      (map (fn [role-step]
             [role-step (get-in steps-progress [role-step])]) steps-or-first))))

(rf/reg-sub
  :hodl-invoice/step-validations
  (fn [db [_ role-step]]
    (step-data db role-step :validations)))

(comment
  (subvec [] 0 0))
