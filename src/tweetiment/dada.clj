(ns tweetiment.dada
  (:require
    [clojure.java.jdbc :as sql]
    [clojure.java.io :as io]
    [korma.db :refer [defdb]]
    [korma.core :refer :all]))

(def db-spec {:classname "org.h2.Driver"
              :subprotocol "h2"
              :subname "happy"
              :user "sa"
              :password ""
              :naming {:keys clojure.string/upper-case
                       :fields clojure.string/upper-case}})

(defn db-initialized? []
  (.exists (io/as-file  "happy.h2.db")))

(defn create-highscore-table []
  (sql/with-connection
    db-spec
    (sql/create-table
      :highscore
      [:id "INTEGER PRIMARY KEY AUTO_INCREMENT"]
      [:timestamp :timestamp]
      [:name "varchar(30)"]
      [:thq "smallint"])
    (sql/do-commands
      "CREATE INDEX timestamp_index ON highscore (timestamp)")
    (sql/do-commands
      "CREATE INDEX thq_index ON highscore (thq)")))

(defn create-tables []
  (create-highscore-table))

(defdb db db-spec)

(defentity highscore)

(defn save-score [name quotient]
  (insert highscore
    (values 
      {:name name
       :thq quotient
       :timestamp (new java.util.Date)})))

(defn get-scores []
  (select highscore))

(defn get-top-scores [n]
  (select highscore 
    (fields :NAME :THQ) 
    (order :THQ :desc) 
    (aggregate (max :TIMESTAMP) :TIMESTAMP) 
    (group :NAME :THQ) 
    (limit n)))

