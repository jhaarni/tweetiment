(defproject tweetiment "0.1.0-SNAPSHOT"
    :plugins [[lein-ring "0.8.10"]]
    :ring {:handler tweetiment.server/handler}
    :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/data.json "0.2.4"]
                 [org.clojure/algo.generic "0.1.2"]
                 [clj-time "0.6.0"]
                 [postgresql "9.1-901.jdbc4"]
                 [korma "0.3.0-RC6"]
                 [hiccup "1.0.5"]
                 [noir "1.3.0"]
                 [twitter-api "0.7.5"]
                 [org.clojure/tools.reader "0.8.3"]]
    :main tweetiment.server)
