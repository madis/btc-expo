(ns app.ui.pages.hodl-invoice.db)

(def default-db
  {:hodl-invoice
   {:steps-progress {}
    :current-step 0}})

(def url-regex #"^https?://(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)$")

(def NodeInfo
  [:map [:maybe [:map
      [:alias :string]
      [:version :string]]]])

(def NodeConfig
  [:map
   [:rune :string]
   [:api-base-url [:and :string [:re url-regex]]]
   [:connection-status [:enum :in-progress :ok :error]]
   [:node-info NodeInfo]])


(def schema
  [:map
   [:hodl-invoice
    [:map
     [:sender NodeConfig]
     [:invoice [:maybe :string]]]]])

(comment
  (require '[malli.core :as m])
  (require '[malli.error :as me])
  (m/validate schema default-db)
  (def result (m/explain schema default-db))
  (get-in result [:errors 0])
  (me/humanize result))
