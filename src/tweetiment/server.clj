(ns tweetiment.server
  (:require [noir.server :as server]
            [tweetiment.web]
            [tweetiment.dada :as db])
  (:gen-class :main true))

(defn start-server [port & mode]
  (server/start port {:mode mode}))

(defn -main [& m]
  (if-not (db/db-initialized?) (db/create-tables))
  (if-let [port (System/getenv "PORT")]
    (start-server (read-string port) :prod)
    (start-server 9090 :dev)))
