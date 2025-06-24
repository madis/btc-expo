(ns app.ui.pages.lnurl-login.events
  (:require
    [re-frame.core :as rf]
    [app.queries :as queries]
    [lambdaisland.uri :as li-uri]
    ["qrcode" :as QRCode]
    [clojure.string :as str]))

(rf/reg-event-db
  ::new-lnurl-qr-data
  (fn [db [_ data-url]]
    (assoc-in db [:operations :lnurl :qr-data] data-url)))

(rf/reg-event-fx
  :poll-http-endpoint
  (fn [_cofx [_ {:keys [url tries-done max-tries time-between-tries on-success on-failure stop-criteria]
                :as poll-params}]]
    (let [poll-id (random-uuid)
          tries-done (or tries-done 0)
          updated-poll-params (merge poll-params {:poll-id poll-id :tries-done (+ 1 tries-done)})]
      (js/console.log ":poll-http-endpoint" poll-params)
      (js/console.log ":poll-http-endpoint try more?" (<= tries-done max-tries))
      (when (<= tries-done max-tries)
        {:fx [[:fetch {:method :get
                       :url url
                       :mode :cors
                       :response-content-type :json
                       :credentials :omit
                       :on-success [::poll-result-success updated-poll-params]
                       :on-failure [::poll-result-failure updated-poll-params]}]]}))))

(rf/reg-event-fx
  ::poll-result-success
  (fn [_ [_ poll-params raw-result]]
    (let [result (js->clj (.parse js/JSON (get-in raw-result [:body])) :keywordize-keys true)
          stop-criteria (:stop-criteria poll-params)]
      (if (stop-criteria result)
        {:fx [[:dispatch [(:on-success poll-params) result]]]}
        {:fx [[:dispatch-later [{:ms (:time-between-tries poll-params)
                                 :dispatch [:poll-http-endpoint poll-params]}]]]}))))

(rf/reg-event-fx
  ::poll-result-failure
  (fn [poll-params]
    (js/console.log "::poll-result-failure" poll-params)))


(rf/reg-event-db
  ::login-success
  (fn [db [_ session]]
    (js/console.log "::login-success" session)
    (assoc-in db [:login :status] true)))

(rf/reg-event-db
  ::login-failure
  (fn [db [_ err]]
    (assoc-in db [:login :status] false)))

(rf/reg-event-fx
  ::new-lnurl-success
  (fn [{:keys [db]} [_ result]]
    (let [json-body (get-in result [:body])
          parsed-body (js->clj (.parse js/JSON json-body) :keywordize-keys true)
          lnurl-from-response (str/upper-case (:lnurl parsed-body))
          random-hex-id (-> (:url parsed-body)
                            (li-uri/parse ,,,)
                            (li-uri/query-map ,,,)
                            :k1)
          api-endpoint (queries/api-url db "/api/login-status")]
      (.then (.toDataURL QRCode lnurl-from-response) #(rf/dispatch [::new-lnurl-qr-data %]))
      {:fx [[:dispatch
             [:poll-http-endpoint
              {:url (str api-endpoint "?sid=" random-hex-id)
               :max-tries 10
               :time-between-tries 2000
               :stop-criteria (fn [result]
                                (js/console.log ":stop-criteria checking" result)
                                (= (:status result) "OK"))
               :on-success ::login-success
               :on-failure ::login-failure}]]]
       :db
       (-> db
           (assoc-in ,,, [:operations :lnurl :lnurl] lnurl-from-response)
           (assoc-in ,,, [:operations :lnurl :random-hex-id] random-hex-id)
           (assoc-in ,,, [:operations :lnurl :url] (get parsed-body :url)))})))

(rf/reg-event-db
  ::new-lnurl-failure
  (fn [db [_ err]]
    (assoc-in db [:operations :lnurl :error] err)))

(rf/reg-event-fx
  ::request-new-lnurl
  (fn [{:keys [db]} _]
    {:fx [[:fetch {:method :get
                   :url (queries/api-url db "/api/lnurl")
                   :mode :cors
                   :response-content-type :json
                   :credentials :omit
                   :on-success [::new-lnurl-success]
                   :on-failure [::new-lnurl-failure]}]]}))
