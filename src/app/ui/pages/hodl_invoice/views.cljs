(ns app.ui.pages.hodl-invoice.views
  (:require
    [clojure.string :as str]
    [oops.core :refer [oget oset!]]))

(defn validation-errors
  [k validations]
  (when-let [errors (get validations k)] [:p.help.is-danger (str/join ". " errors)]))

(defn message
  [content & [type]]
  [:article.message {:class [type]}
   [:div.message-body content]])

(defn node-config
  [{:keys [on-submit on-use-defaults set-url set-rune]} {:keys [url rune status error validations]}]
  [:div
   [:div.field
    [:label.label "Core Lightning REST API base URL"]
    [:div.control
     [:input.input
      {:type "text"
       :class (when (:api-base-url @validations) [:is-danger])
       :value @url
       :placeholder "http://localhost:8184/v1"
       :on-change set-url}]]
    [validation-errors :api-base-url @validations]]
   [:div.field
    [:label.label "Rune"]
    [:div.control
     [:input.input
      {:type "text"
       :class (when (:rune @validations) [:is-danger])
       :value @rune
       :placeholder "k8QQvNMUJMpC5u-fdTgpndC9hOWPgIcpN8I2t299KxU9MA=="
       :on-change set-rune}]]
    [validation-errors :rune @validations]]
   [:div.field
    [:div.buttons
     [:button.button {:on-click on-use-defaults} "Use defaults"]
     [:button.button.is-primary {:on-click on-submit} "Check connection"]]]
   (case @status
     :ok
     [message "Connection to node OK" :is-success]

     :in-progress
     [message "Checking connection" :is-info]

     :error
     [message @error :is-danger]

     [message "Please fill the data or use defaults" :is-warning])
   ])

(defn invoice-form
  [{:keys [amount description disabled? on-change on-submit]}]
  [:div
   [:div.field
    [:label.label "Amount"]
    [:div.control
     [:input.input
      {:type "number"
       :on-change on-change
       :value @amount
       :min 1000
       :max 1000000}]]]
   [:div.field
    [:div.control
     [:button.button.is-primary {:on-click on-submit :disabled disabled?} "Generate invoice"]]]])

(defn- copy-to-clipboard [s]
  (let [promise (.writeText (oget js/navigator :clipboard) s)]
    (.then promise (fn [_] (js/console.log "Done copying to the clipboard")))))

(defn- copy-with-blur [e]
  (let [target (oget e [:target])]
    (.select target)
    (-> (copy-to-clipboard (oget target :value))
        (.then (fn [_]
                 (oset! target :selectionStart (oget e [:target :selectionEnd]))
                 (.blur target))))))

(defn invoice-display
  [lnurl qr-code-image]
  [:div.field
   [:img {:src qr-code-image}]
   [:p.control.has-icons-right
    [:input {:type :text
             :class [:input]
             :value lnurl
             :read-only true
             :on-click #(copy-with-blur %)}]
    [:span {:class [:icon :is-small :is-right]}
     [:i.far.fa-clipboard ]]]
   [:pre {:class [:has-fixed-height :mt-3]}
    [:code.language-json lnurl]]])

(defn- invoice-card
  [invoice disabled? actions & [el-key]]
  (let [gen-key (fn [sub-id] (str (or (name el-key) (subs (:bolt11 invoice) 0 10)) sub-id))]
    [:div.card {:key (gen-key "card")}
     [:div.card-content
      [:div.content
       [:b "Invoice:"]
       [:p (subs (:bolt11 invoice)  0 29) "..."]
       [:b "Amount:"]
       [:p (str (:amount invoice) " " (:currency invoice))]
       [:b "Description:"]
       [:p (:description invoice)]
       [:b "Created at:"]
       [:p (:created-at invoice)]
       [:b "Expires:"]
       [:p (:expiry invoice)]]]
     [:div.card-footer
      (for [action actions]
        [:button.button.is-primary.card-footer-item
         {:key (gen-key "card-footer")
          :disabled disabled?
          :on-click (fn [_e] ((:on-click action) invoice))}
         (:title action)])]]))

(defn pay-invoice
  [{:keys [invoices disabled? error actions]}]
  [:div
   (when error [message error :is-danger])
   (doall
     (for [invoice invoices]
       (invoice-card invoice disabled? actions :pick-invoice)))])

(defn settle-invoice
  [{:keys [invoice disabled? actions]}]
  (invoice-card invoice disabled? actions :settle-invoice))
