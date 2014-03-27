(ns tweetiment.dada
  (:require
    [clojure.java.jdbc :as sql]
    [clojure.java.io :as io]
    [korma.db :refer [defdb postgres]]
    [korma.core :refer :all]))

(def database-url (or (System/getenv "DATABASE_URL")
              "postgresql://janne:@localhost:5432/janne"))

(declare count-scores)

(defn db-initialized? []
  (try (count-scores)
    (catch Exception e (println "DATABASE NOT INITIALIZED"))))

(defn create-highscore-table []
  (println "CREATING HIGHSCORE TABLE")
  (sql/with-connection
    database-url
    (sql/create-table
      :highscore
      [:id :serial "PRIMARY KEY"]
      [:timestamp :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]
      [:name "varchar(30)"]
      [:thq "smallint"])
    (sql/do-commands
      "CREATE INDEX timestamp_index ON highscore (timestamp)")
    (sql/do-commands
      "CREATE INDEX thq_index ON highscore (thq)")))

(defn create-tables []
  (create-highscore-table))

(defdb db 
  (let [db-uri (java.net.URI. database-url)
        [user pw] (clojure.string/split (.getUserInfo db-uri) #":")]
    {:classname "org.postgresql.Driver"
     :subprotocol "postgresql"
     :user user
     :password pw 
     :subname (if (= -1 (.getPort db-uri))
                (format "//%s%s" (.getHost db-uri) (.getPath db-uri))
                (format "//%s:%s%s" (.getHost db-uri) (.getPort db-uri) (.getPath db-uri)))}))

(defentity highscore)

(defn save-score [name quotient]
  (insert highscore
    (values 
      {:name name
       :thq quotient})))

(defn count-scores []
  (select highscore (aggregate (count :id) :COUNT)))

(defn get-top-scores [n]
  (select highscore 
    (fields :name :thq) 
    (order :thq :desc) 
    (aggregate (max :timestamp) :TIMESTAMP) 
    (group :name :thq) 
    (limit n)))

