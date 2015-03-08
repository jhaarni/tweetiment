(ns tweetiment.core
  (:require [clojure.string :as s]
            [tweetiment.util :refer [slurpr]]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.algo.generic.functor :as fun]
            [tweetiment.twitter :as tw]))

(defn words [s]
  (map s/lower-case (re-seq #"[a-zA-Z]+" s)))

(defn trim-decimals [n]
   (read-string (re-find #"\d+" (str n))))

(defn tweets [coll]
  (filter :text (json/read-str coll :key-fn keyword)))

(defn make-pair [l]
  (s/split l #"\t+"))

(defn read-scores [f]
  (->> 
    (slurpr f)
    s/split-lines
    (map make-pair)
    flatten
    (apply hash-map)
    (fun/fmap read-string)))

(def scores (read-scores "AFINN-111.txt"))

(defn sentiment [a] 
  (or (scores a) 0))

(defn add-sentiment [n, s] 
  (+ n (sentiment s)))

(defn total-sentiment [v]
  (reduce add-sentiment 0 (flatten v))) 

(defn average [lst] (/ (reduce + lst) (count lst)))

(defn sentiments [coll]
  (->> (map words coll)
      (map total-sentiment)
      (average)
      (double)
      (* 100)))

(defn tweetlist-sentiment [coll]
  (let [texts (map :text coll)] 
    (sentiments texts)))

(defn user-sentiment [user]
  (tweetlist-sentiment (:body (tw/timeline user))))

(defn between [n low high]
  (< low  n (inc high)))

(def result-text "Your Tweet Happiness Quotient is")

(defn grade-sentiment [n]
  (cond
    (<= n 0) (str "Oops.. " result-text " a sorry ")
    (between n 0 50) (str result-text " a meager ")
    (between n 50 100) (str "Nice! " result-text " a great ")
    (between n 100 150) (str "Congratulations! " result-text " a whopping ")
    (between n 150 200) (str "Fabulous! " result-text " an awesome ")
    (> n 200) (str "Incredible!! " result-text " an unbelievable ")))


