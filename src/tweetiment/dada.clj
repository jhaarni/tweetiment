(ns tweetiment.dada
  (:require
    [clojure.java.jdbc :as sql]
    [clojure.java.io :as io]))

(def db-uri 
  (or (System/getenv "DATABASE_URL") "postgresql://janne:@localhost:5432/janne"))

(defn db-initialized? []
  (-> (sql/query db-uri
        ["select count(*) from information_schema.tables 
          where table_name='highscore'"])
      first :count pos?))

(defn create-highscore-table []
  (println "CREATING HIGHSCORE TABLE")
  (sql/db-do-commands
    db-uri
    (sql/create-table-ddl
      :highscore
      [:id :serial "PRIMARY KEY"]
      [:timestamp :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]
      [:name "varchar(30)"]
      [:thq "smallint"])
    "CREATE INDEX timestamp_index ON highscore (timestamp)")
    "CREATE INDEX thq_index ON highscore (thq)")

(defn create-tables []
  (create-highscore-table))

(defn save-score [name quotient]
  (sql/insert! db-uri
    :highscore
    {:name name
     :thq quotient}))

(defn get-top-scores [n]
  (sql/query db-uri
    ["select  name, thq, max(timestamp) as timestamp 
     from highscore group by name, thq order by thq desc limit ?" n]))
