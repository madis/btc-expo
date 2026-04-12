(ns app.ui.pages.hodl-invoice.db
  (:require
    [malli.core :as m]
    [malli.error :as me]
    [malli.dev.cljs]
    [malli.dev.pretty]
    ))

(def default-db
  {:hodl-invoice
   {:steps-progress {}
    :current-step 0}})


(def url-regex #"^https?://(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)$")

(def NodeInfo
  [:maybe
   [:map
    [:alias :string]
    [:version :string]]])

(def Status [:enum :in-progress :ok :error])

(def ConnectionCheck
  [:map
   [:rune :string]
   [:api-base-url [:and :string [:re url-regex]]]
   [:status {:optional true} Status]
   [:result {:optional true} NodeInfo]])

(def GenerateInvoiceResult
  [:map
   [:bolt11 :string]
   [:preimage :string]
   [:payment-hash :string]])

(def Error
  [:map
   [:message :string]])

(def InvoiceGeneration
  [:map
   [:new-invoice-amount [:int {:min 1000}]]
   [:status Status]
   [:result GenerateInvoiceResult]
   [:error Error]])

(def InvoiceInfo
  [:map
   [:qr-code :string]
   [:status Status]
   [:error [:maybe Error]]])

(def schema
  [:map
   [:steps-progress
    [:map
     [[:receiver :check-connection] ConnectionCheck]
     [[:sender :check-connection]] ConnectionCheck
     [[:receiver :generate-invoice]] InvoiceGeneration
     [[:receiver :show-invoice-info]] InvoiceInfo]]])

(comment
  ; (require '[malli.core :as m])
  ; (require '[malli.error :as me])
  ; (require '[malli.dev.cljs])
  ; (require '[malli.dev.pretty :as pretty])
  (malli.dev.cljs/start! {:report (malli.dev.pretty/reporter)})
  (m/validate InvoiceInfo {:qr-code "42" :status :ok :error nil})
  (def result (m/explain ConnectionCheck {:rune "abcde" :api-base-url "hello"}))
  (get-in result [:errors 0])
  (me/humanize result))

(defn validate [spec data]
  (me/humanize (m/explain spec data)))
