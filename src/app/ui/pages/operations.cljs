(ns app.ui.pages.operations
  (:require
    [components.button :as button]
    [re-frame.core :as rf]))

(rf/reg-event-fx
  :request-to-node
  (fn [_ _]
    {:fx [[:fetch {:method :post
                   :url "http://127.0.0.1:8184/v1/getinfo"
                   :headers {"Rune" "<your rune here>"
                             "Accept" "application/json"}
                   :mode :cors
                   :credentials :omit
                   :on-success [::node-request-success]
                   :on-failure [::node-request-failure]}]]}))

(rf/reg-event-db
  ::node-request-success
  (fn [db [_ result]]
    (assoc-in db [:operations :last-request :result] result)))

(rf/reg-event-db
  ::node-request-failure
  (fn [db [_ result]]
    (assoc-in db [:operations :last-request :failure] result)))

(rf/reg-sub
  ::node-request-result
  (fn [db _]
    (get-in db [:operations :last-request :result :body])))

;; START LNURL
(rf/reg-event-db
  ::new-lnurl-data
  (fn [db [_ data-url]]
    (println ">>> ::new-lnurl-data" data-url)
    (assoc-in db [:operations :lnurl :data-url] data-url)))

(rf/reg-event-db
  ::new-lnurl-success
  (fn [db [_ result]]
    {:fx
     [[:dispatch]]}
    (println ">>> ::new-lnurl-success" result)
    (let [json-body (get-in result [:body])
          parsed-body (js->clj (.parse js/JSON json-body) :keywordize-keys true)
          url-from-response (str "" (:lnurl parsed-body))]
      (js/console.log "::new-lnurl-success PARSED:" [parsed-body url-from-response])
      (.then
        (.toDataURL QRCode url-from-response)
        (fn [data-url]
          (js/console.log "QRCode data-url" data-url)
          (rf/dispatch [::new-lnurl-data data-url])))
      (-> db
          (assoc-in ,,, [:operations :lnurl :api-response] result)
          (assoc-in ,,, [:operations :lnurl :url] (get parsed-body :url))))))

(rf/reg-event-db
  ::new-lnurl-failure
  (fn [db [_ err]]
    (assoc-in db [:operations :lnurl] nil)))

(rf/reg-event-fx
  ::request-new-lnurl
  (fn [_ _]
    (println ">>> ::request-new-lnurl")
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

;; END LNURL

(defn lnurl-login
  []
  (let [login-lnurl @(rf/subscribe [::login-lnurl])
        url @(rf/subscribe [::login-url])]
    [:div.box
      [:h4.title.is-4 "Login with LNURL"]
      [:div.box
       (if login-lnurl
         [:div
          [:img {:src login-lnurl}]
          [:code url]]
         [button/button {:label "Get LNURL for login"
                         :size "medium"
                         :background-color "is-primary"
                         :on-click #(rf/dispatch [::request-new-lnurl])}])]]))

(defn show
  []
  (let [result @(rf/subscribe [::node-request-result])]
    [:div.content.is-medium
     [:h2.title.is-2 "Operations"]
     [:div.box
      [:h4.title.is-4 "Query balance"]
      [button/button {:label "Query"
                      :size "medium"
                      :background-color "is-primary"
                      :on-click #(rf/dispatch [:request-to-node])}]]
     [lnurl-login]]))
