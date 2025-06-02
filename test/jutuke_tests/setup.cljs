(ns jutuke-tests.setup
  (:require
    [lambdaisland.glogi.console :as glogi-console]
    [lambdaisland.glogi :as log]))

(glogi-console/install!)

(log/set-levels
  {:glogi/root :info
   'app.lightning :trace
   'jutuke-tests :trace})
