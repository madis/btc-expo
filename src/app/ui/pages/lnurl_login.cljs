(ns app.ui.pages.lnurl-login
  (:require
    [components.button :as button]
    [re-frame.core :as rf]
    ["qrcode" :as QRCode]
    [cljs.core.async.interop]
    [lambdaisland.uri :as li-uri]))


(rf/reg-event-db
  ::new-lnurl-data
  (fn [db [_ data-url]]
    (assoc-in db [:operations :lnurl :data-url] data-url)))

(rf/reg-event-db
  ::new-lnurl-success
  (fn [db [_ result]]
    (let [json-body (get-in result [:body])
          parsed-body (js->clj (.parse js/JSON json-body) :keywordize-keys true)
          lnurl-from-response (str "" (:lnurl parsed-body))
          random-hex-id (-> (:url parsed-body)
                            (li-uri/parse ,,,)
                            (li-uri/query-map ,,,)
                            :k1)]
      (.then (.toDataURL QRCode lnurl-from-response) #(rf/dispatch [::new-lnurl-data %]))
      (-> db
          (assoc-in ,,, [:operations :lnurl :api-response] result)
          (assoc-in ,,, [:operations :lnurl :random-hex-id] random-hex-id)
          (assoc-in ,,, [:operations :lnurl :url] (get parsed-body :url))))))

(rf/reg-event-db
  ::new-lnurl-failure
  (fn [db [_ err]]
    (assoc-in db [:operations :lnurl :error] err)))

(rf/reg-event-fx
  ::request-new-lnurl
  (fn [_ _]
    {:fx [[:fetch {:method :get
                   :url "https://apps.mad.is/api/lnurl"
                   :mode :cors
                   :response-content-type :json
                   :credentials :omit
                   :on-success [::new-lnurl-success]
                   :on-failure [::new-lnurl-failure]}]]}))
(rf/reg-sub
  ::login-lnurl
  (fn [db _]
    (get-in db [:operations :lnurl :data-url])))

(rf/reg-sub
  ::login-url
  (fn [db _]
    (get-in db [:operations :lnurl :url])))

(defn show
  []
  (let [login-lnurl @(rf/subscribe [::login-lnurl])
        url @(rf/subscribe [::login-url])]
    [:div.box
      [:h4.title.is-4 "Login with LNURL"]
      [:div.box
       (if login-lnurl
         [:div
          [:img {:src login-lnurl}]
          [:pre {:class [:has-fixed-height :mt-3]}
           [:code.language-json url]]]
         [button/button {:label "Get LNURL for login"
                         :size "medium"
                         :background-color "is-primary"
                         :on-click #(rf/dispatch [::request-new-lnurl])}])]]))
