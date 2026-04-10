(ns app.ui.pages.hodl-invoice.views
  (:require
    [oops.core :refer [oget oset!]]))

(defn node-config
  [{:keys [on-submit on-use-defaults set-url set-rune role]} {:keys [url rune result error]}]
  [:div.field
   [:div.field
    [:label.label "Core Lightning REST API base URL"]
    [:div.control
     [:input.input
      {:type "text"
       :value @url
       :placeholder "http://localhost:8184/v1"
       :on-change set-url}]]]
   [:div.field
    [:label.label "Rune"]
    [:div.control
     [:input.input
      {:type "text"
       :value @rune
       :placeholder "k8QQvNMUJMpC5u-fdTgpndC9hOWPgIcpN8I2t299KxU9MA=="
       :on-change set-rune}]]]

   (case (:status result)
     :ok
     [:div
      [:div.field
       [:span.tag.is-medium.is-primary "Connection OK"]]]

     :in-progress
     [:div.field
      [:span.tag.is-medium.is-warning "Checking..."]]

     :error
     [:div
      [:div.field
       [:span.tag.is-medium.is-danger "Error"]]]

     [:div.buttons
      [:button.button {:on-click on-use-defaults} "Use defaults"]
      [:button.button.is-primary {:on-click on-submit} "Check connection"]])])

(defn invoice-form
  [{:keys [amount description on-change on-submit]}]
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
     [:button.button {:on-click on-submit} "Generate invoice"]]]])

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
  [invoice actions & [el-key]]
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
          :on-click (fn [_e] (((:on-click action) invoice)))}
         (:title action)])]]))

(defn pick-invoice
  [{:keys [invoices actions]}]
  (doall
    (for [invoice invoices]
     (invoice-card invoice actions :pick-invoice))))

(defn settle-invoice
  [{:keys [invoice actions]}]
  (invoice-card invoice actions :settle-invoice))
