(ns server.js-helpers)

(defn js-obj->clj-map [js-obj]
  (into {}
        (map (fn [k] [(keyword k) (aget js-obj k)]))
        (js/Object.keys js-obj)))
