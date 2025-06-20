(ns server.storage.atom-storage
  (:require
    [server.storage :refer [Storage]]
    [lambdaisland.glogi :as log]
    [mount.core :refer [defstate]]))

(defrecord AtomStorage [state]
  Storage
  (add-to-store! [_ key value]
    (swap! state assoc key value))

  (get-from-store [_ key]
    (get @state key))

  (in-store? [_ key]
    (contains? @state key)))

(defstate storage
  :start (->AtomStorage (atom {}))
  :stop #(log/info :storage "Stopping AtomStorage"))
