(ns server.config
  (:require
    [mount.core :refer [defstate]]
    [server.storage.atom-storage :refer [->AtomStorage]]))

(defstate config
  :start {:base-url "https://apps.mad.is/api/login"})

(defstate storage
  :start (->AtomStorage (atom {})))
