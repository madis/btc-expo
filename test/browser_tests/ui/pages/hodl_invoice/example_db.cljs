(ns browser-tests.ui.pages.hodl-invoice.example-db)

(def example-db
  {:steps-progress
   {[:receiver :check-connection]
    {:rune "3CXAe3AWOInD5r6A1wlbFZ5BJWbh-FJIvUPuYNlxJlE9MA==",
     :api-base-url "http://127.0.0.1:8182",
     :status :ok,
     :result {:alias "bob", :version "v25.12"}},
    [:sender :check-connection]
    {:api-base-url "http://127.0.0.1:8184",
     :rune "k8QQvNMUJMpC5u-fdTgpndC9hOWPgIcpN8I2t299KxU9MA==",
     :status :ok,
     :result {:alias "dave", :version "v25.12"}},
    [:receiver :generate-invoice]
    {:new-invoice-amount 15543,
     :result
     {:bolt11
      "lnbcrt155430p1p5asf5msp58sh25e6x5gm3wckfzctndf4fjne5d8vexnyxdgyx62h9tj9qcttqpp5rr9n96kytw0t6e8kwe2td5sj4pvl56nhlgg3mmdcuucmav0ntztsdqqcqzzs9qyysgqtt8u4meuu2kpl4y5mhaj068h409uukz5kcjvm9ejmll3fkngq2xqexanscu8j2jx66d2a44sef4jw6lt4z3jmg9kwxln4a364mwf0xsqm0am9n",
      :preimage
      "c6fcc06bca520310caa70e1690ac7ffbe790a5cbd642fb718a9c87429fc15240",
      :payment-hash
      "18cb32eac45b9ebd64f67654b6d212a859fa6a77fa111dedb8e731beb1f35897"},
     :status :ok},
    [:receiver :show-invoice-info]
    {:qr-code
     "data:image/png;base64,iVBORw0KG",
     :status :ok},
    [:sender :pay-invoice]
    {:status :ok,
     :result
     {:ok? true,
      :redirected? false,
      :final-uri? nil,
      :status-text "Created",
      :type "cors",
      :headers {:content-length "170", :content-type "application/json"},
      :status 201,
      :reader :json,
      :url "http://127.0.0.1:8184/v1/xpay",
      :body
      {:amount_msat 15543,
       :amount_sent_msat 17544,
       :failed_parts 0,
       :payment_preimage
       "c6fcc06bca520310caa70e1690ac7ffbe790a5cbd642fb718a9c87429fc15240",
       :successful_parts 1}}},
    [:sender :settle-invoice] {:status :in-progress},
    [:receiver :settle-invoice]
    {:status :ok,
     :result
     {:ok? true,
      :redirected? false,
      :final-uri? nil,
      :status-text "Created",
      :type "cors",
      :headers {:content-length "2", :content-type "application/json"},
      :status 201,
      :reader :json,
      :url "http://127.0.0.1:8182/v1/settleholdinvoice",
      :body {}}}}})
