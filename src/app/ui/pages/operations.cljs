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
                      :on-click #(rf/dispatch [:request-to-node])}]
      [:div.box
       [:pre
        [:code.language-json
         (.stringify js/JSON (.parse js/JSON result) nil "  ")]]]]]))
