(ns server.js-helpers)

(defn- looks-numeric?
  [x]
  (boolean (re-find #"^-?\d+(\.\d+)?$" x)))

(defn js-obj->clj-map [js-obj]
  (into {}
        (map (fn [k]
               [(keyword k)
                (if (looks-numeric? (aget js-obj k))
                  (parse-double (aget js-obj k))
                  (aget js-obj k))]))
        (js/Object.keys js-obj)))
