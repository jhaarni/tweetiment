(ns tweetiment.util
  (:require [clojure.java.io :as io]))

(defn as-resource [path]
  (when path
    (-> (Thread/currentThread) .getContextClassLoader (.getResourceAsStream path))))

(defn slurpr [path]
  (slurp (as-resource path)))

