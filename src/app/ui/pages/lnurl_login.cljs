(ns app.ui.pages.lnurl-login
  (:require
    [components.button :as button]
    [re-frame.core :as rf]
    [app.ui.pages.lnurl-login.events :as lnurl-events]
    [app.ui.pages.lnurl-login.subscriptions :as lnurl-subs]))


(defn show
  []
  (let [login-lnurl @(rf/subscribe [::lnurl-subs/login-lnurl])
        url @(rf/subscribe [::lnurl-subs/login-url])
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
          [:pre {:class [:has-fixed-height :mt-3]}
           [:code.language-json url]]]
         [button/button {:label "Get LNURL for login"
                         :size "medium"
                         :background-color "is-primary"
                         :on-click #(rf/dispatch [::lnurl-events/request-new-lnurl])}])]]))
