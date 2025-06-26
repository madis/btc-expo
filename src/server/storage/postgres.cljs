(ns server.storage.postgres
  (:require
    [server.config :refer [config]]
    [mount.core :refer [defstate]]
    [server.storage :refer [Storage all-profiles]]
    [lambdaisland.glogi :as log]
    [oops.core :refer [oget]]
    [honey.sql :as sql]
    ["pg" :as pg]
    [clojure.core.async :as async :refer [go chan <! >! put!]]
    [camel-snake-kebab.extras :as cske]
    [camel-snake-kebab.core :as csk]))

(def pool (pg/Pool. (clj->js {:connectionString (:database-url @config)})))

(defn get-clojurized-rows
  [pg-result]
  (->> (oget pg-result :rows)
       (map js->clj ,,,)
       ((partial cske/transform-keys csk/->kebab-case-keyword) ,,,)))

(defn- query-str->chan
  [conn [query-str params]]
  (let [result-chan (chan 1)]
  (-> (.query conn query-str params)
      (.then ,,, #(put! result-chan (get-clojurized-rows %))))
  result-chan))

(defn query-map->chan
  "Expects a honey-sql query map and returns a new channel with the result rows"
  [conn clj-query]
  (query-str->chan conn (sql/format clj-query)))

(defn query->chan
  [conn query]
  (cond
    (string? query) (query-str->chan conn query)
    (map? query) (query-map->chan conn query)
    :else (throw (ex-info "Only SQL string or Map are supported" {:query query
                                                                  :query-type (type query)}))))

(defrecord PGStorage [pool]
  Storage
  (add-to-store! [_ key value]
    (log/warn :storage/pg "Not implemented"))

  (get-from-store [_ key]
    (log/warn :storage/pg "Not implemented"))

  (in-store? [_ key]
    (log/warn :storage/pg "Not implemented"))

  (all-profiles [_]
    (query->chan pool {:select :* :from :profiles})))

(defn start!
  []
  (println "START storage.postgres")
  (log/info :storage/postgres "Starting mount module")
  (->PGStorage pool))

(defstate storage
  :start (start!)
  :stop (log/info :storage/postgres "Stopping mount module"))
