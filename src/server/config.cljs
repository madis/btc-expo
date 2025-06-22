(ns server.config
  (:require
    [mount.core :refer [defstate]]
    [server.storage.atom-storage :refer [->AtomStorage]]))

(defstate config
  :start {:base-url "https://apps.mad.is"
          :uploads-root-url "http://localhost:3000/uploads"})

(defstate storage
  :start (->AtomStorage (atom {})))
