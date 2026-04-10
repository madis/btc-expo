(ns app.ui.pages.hodl-invoice.config)

(def steps
  [[:receiver :check-connection]
    [:sender :check-connection]
    [:receiver :generate-invoice]
    [:receiver :show-invoice-info]
    ; [:sender :pick-invoice]
    [:sender :pay-invoice]
    [:receiver :settle-invoice]
    [:sender :show-confirmation]])

(def dev-node-conf
  {:sender
   {:api-base-url "http://127.0.0.1:8184"
    :rune "k8QQvNMUJMpC5u-fdTgpndC9hOWPgIcpN8I2t299KxU9MA=="}
   :receiver
   {:rune "3CXAe3AWOInD5r6A1wlbFZ5BJWbh-FJIvUPuYNlxJlE9MA=="
    :api-base-url "http://127.0.0.1:8182"}})
