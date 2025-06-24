(ns app.ui.pages.lnurl-login
  (:require
    [components.button :as button]
    [re-frame.core :as rf]
    [oops.core :refer [oget oset!]]
    [app.ui.pages.lnurl-login.events :as lnurl-events]
    [app.ui.pages.lnurl-login.subscriptions :as lnurl-subs]))


(defn copy-to-clipboard [s]
  (let [promise (.writeText (oget js/navigator :clipboard) s)]
    (.then promise (fn [_] (js/console.log "Done copying to the clipboard")))))

(defn copy-with-blur [e]
  (let [target (oget e [:target])]
  (.select target)
  (-> (copy-to-clipboard (oget target :value))
      (.then (fn [_]
               (oset! target :selectionStart (oget e [:target :selectionEnd]))
               (.blur target))))))

(defn show
  []
  (let [login-lnurl @(rf/subscribe [::lnurl-subs/login-lnurl-qr-data])
        url @(rf/subscribe [::lnurl-subs/login-url])
        lnurl @(rf/subscribe [::lnurl-subs/login-lnurl])
        login-status @(rf/subscribe [::lnurl-subs/login-status])]
    [:div.box
      [:h4.title.is-4 "Login with LNURL"]
      (when login-status
        [:article.message.is-success
         [:div.message-header "Login successful!"]
         [:div.message-body "You have been logged in"]])
      [:div.box
       (if login-lnurl
         [:div
          [:img {:src login-lnurl}]
          [:p.control.has-icons-right
           [:input {:type :text
                    :class [:input]
                    :value lnurl
                    :read-only true
                    :on-click #(copy-with-blur %)}]
           [:a.button.mt-3 {:href (str "lightning:" lnurl)} "Open in your Lightning wallet!"]
           [:span {:class [:icon :is-small :is-right]}
            [:i.far.fa-clipboard ]]]
          [:pre {:class [:has-fixed-height :mt-3]}
           [:code.language-json url]]]
         [button/button {:label "Get LNURL for login"
                         :size "medium"
                         :background-color "is-primary"
                         :on-click #(rf/dispatch [::lnurl-events/request-new-lnurl])}])]]))
