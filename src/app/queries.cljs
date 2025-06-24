(ns app.queries)

(defn api-url
  ([db] (get-in db [:config :api-url]))
  ([db path] (str (api-url db) path)))
