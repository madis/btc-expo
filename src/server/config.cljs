(ns server.config
  (:require
    [mount.core :refer [defstate]]
    [server.storage.atom-storage :refer [->AtomStorage]]))

(goog-define base-url "http://localhost:3000")
(goog-define uploads-root-url "http://localhost:3000/uploads")

(defstate config
  :start {:base-url base-url
          :uploads-root-url uploads-root-url})

(defstate storage
  :start (->AtomStorage (atom {})))
