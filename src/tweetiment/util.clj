(ns tweetiment.util
  (:require [clojure.java.io :as io]))

(defn as-resource [path]
  (when path
    (-> (Thread/currentThread) .getContextClassLoader (.getResource path))))

(defn slurpr [path]
  (slurp (io/file (as-resource path))))

