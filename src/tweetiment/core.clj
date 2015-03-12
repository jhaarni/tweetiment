(ns tweetiment.core
  (:require [clojure.string :as s]
            [tweetiment.util :refer [slurpr]]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.algo.generic.functor :as fun]
            [tweetiment.twitter :as tw]))

(defn words [coll]
  (map s/lower-case (re-seq #"[a-zA-Z]+" coll)))

(defn trim-decimals [n]
   (read-string (re-find #"\d+" (str n))))

(defn tweets [coll]
  (filter :text (json/read-str coll :key-fn keyword)))

(defn make-pair [pair-string]
  (s/split pair-string #"\t+"))

(defn read-scores [score-file]
  (->> 
   (slurpr score-file)
   s/split-lines
   (map make-pair)
   flatten
   (apply hash-map)
   (fun/fmap read-string)))

(def scores (read-scores "AFINN-111.txt"))

(defn sentiment [wrd] 
  (or (scores wrd) 0))

(defn add-sentiment [n, snt] 
  (+ n (sentiment snt)))

(defn total-sentiment [coll]
  (reduce add-sentiment 0 (flatten coll))) 

(defn average [coll] (/ (reduce + coll) (count coll)))

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


