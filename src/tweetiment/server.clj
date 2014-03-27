(ns tweetiment.server
  (:require [noir.server :as server]
            [tweetiment.web]
            [tweetiment.dada :as db])
  (:gen-class :main true))

(defn -main [& m]
  (if-not (db/db-initialized?) (db/create-tables))
  (server/start 8080 {:mode :prod}))
