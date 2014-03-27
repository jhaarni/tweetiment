(defproject tweetiment "0.1.0-SNAPSHOT"
    :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/data.json "0.2.4"]
                 [org.clojure/algo.generic "0.1.2"]
                 [clj-time "0.6.0"]
                 [org.clojure/java.jdbc "0.3.3"]
                 [postgresql "9.1-901.jdbc4"]
                 [hiccup "1.0.5"]
                 [noir "1.3.0"]
                 [twitter-api "0.7.5"]
                 [org.clojure/tools.reader "0.8.3"]]
    :min-lein-version "2.0.0"
    :uberjar-name "tweetiment-standalone.jar"
    :profiles {:uberjar {:aot :all}}
    :main ^:skip-aot tweetiment.server)
