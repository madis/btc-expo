(ns server.storage)

(defprotocol Storage
  (get-from-store [this key])
  (add-to-store! [this key value])
  (in-store? [this key]))
