(ns tweetiment.web
  (:require
    [clj-time.coerce :as c]
    [clj-time.format :as f]
    [clojure.string :as s]
    [ring.util.codec :refer [url-encode]]
    [noir.core :refer [defpage defpartial]]
    [noir.response :refer [redirect]]
    [noir.session :as session]
    [noir.statuses :refer [set-page!]]
    [noir.request :refer :all]
    [hiccup.core :refer :all]
    [hiccup.page :refer :all]
    [hiccup.form :refer :all]
    [hiccup.element :refer :all]
    [tweetiment.core :refer [user-sentiment trim-decimals grade-sentiment]]
    [tweetiment.twitter :as tw]
    [tweetiment.dada :as db]))

(def navigation {:home ["/" "Home"] :highscore ["/scores" "Highscores"] :about ["/about" "About"]})

(defn get-return-uri [request]
   (str (name (:scheme request)) "://" (get (:headers request) "host") "/return"))

(defn navi-item [nav current]
  (let [k (key nav)
        [l n] (val nav)]
    (if (= k current)
      n
      (link-to {:class "blue"} l n))))

(defn seq-join [delim sequence]
  (rest (interleave (repeat (count sequence) delim) sequence)))

(defn fmt-date [date]
  (let [dt (c/from-date date)
        fmt (f/formatter "MM/dd/yyyy")]
    (f/unparse fmt dt)))

(defpartial navi [current]
  [:div#navi  
    (seq-join " | " (for [nav navigation] (navi-item nav current)))])

(defpartial footer [nav]
        [:div#footer
        [:span#footer-text [:p#navi nav]]])

(defpartial layout [nav & content]
  (html5
    [:head
      [:title "Happy Tweeter"] 
      (include-css "happy.css?r=1")]
    [:body 
      [:div#wrap
      [:div#content
        [:div#title [:h1 "Happy Tweeter"]]
        content]]
      (footer (navi nav))]))

(defpartial happy []
  [:div#image
    (image {:height 150} "Twitter_logo_blue.png")]
  [:div#text
    [:p "Happy Tweeter will read your recent tweets to see how happy you are!"]
    [:p "Just sign in with Twitter and Go!"]]
  [:div#login 
    [:a {:href "/auth"}
      (image {:height 25} "sign-in-with-twitter-gray.png")]])
    
(defpartial result [name num]
    [:div#small-image 
      (image {:height 90} "Twitter_logo_blue.png")]
    [:div#result [(if (< num 0) :div.neg :div.thq) "THQ:" num]]
    [:div#text 
      [:p (grade-sentiment num) num]
      [:p "Tweet to tell your followers how happy you are."]]
    [:script "!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0];
        if(!d.getElementById(id)){js=d.createElement(s);js.id=id;
        js.src='https://platform.twitter.com/widgets.js';
        fjs.parentNode.insertBefore(js,fjs);}}(document,'script','twitter-wjs');"]
    [:div#tweet 
      (link-to 
        {:class "twitter-share-button" :data-lang "en"} 
        (str "https://twitter.com/share?text=" 
          (url-encode (str "I got " num " as my Tweet Happiness Quotient! See yours at http://HappyTweeter.com")))
        "Tweet")])

(defpartial error-page []
  [:div [:h2#error "Sorry!"]]
  [:div#text 
    [:p "I am sorry, something went wrong. This makes me somewhat unhappy."]
    [:p "Hopefully you have better luck next time."]])

(defpartial not-found []
  [:div [:h2#error "Sorry!"]]
  [:div#text 
    [:p "I am sorry, there's nothing here by that name."]])

(defpartial scores [lst]
  [:div#highscore
    [:h2 "Highscores"]
    [:table#pure-table-horizontal 
    [:tr  [:th "Name"] [:th "THQ"] [:th "Date"]]
    (for [score lst]
      [:tr [:td (:NAME score)] [:td (:THQ score)] [:td (fmt-date (:TIMESTAMP score))]])]])

(set-page! 404 (layout :error (not-found)))

(set-page! 500 (layout :error (error-page)))

(defpage [:get "/"] {}
  (layout :home (happy)))

(defpage [:get "/auth"] {}
  (let [return-uri (get-return-uri (ring-request))
        rtoken (tw/get-request-token return-uri)
        u (tw/oauth-uri rtoken)]
    (session/put! :rtoken rtoken)    
    (redirect u))) 

(defpage [:get "/return"] params 
  (if-not (:oauth_token params)
    (redirect "/")
    ; else
    (let [token (:oauth_token params)
      rtoken (session/get :rtoken)
      verifier (:oauth_verifier params)
      atoken (tw/get-access-token rtoken verifier)]
      (session/put! :atoken atoken)
      (redirect "/thq"))))

(defpage [:get "/thq"] {}
  (let [atoken (session/get :atoken)
        sname (:screen_name atoken)
        sent (trim-decimals (user-sentiment sname))]
    (db/save-score sname sent)    
    (layout :result (result sname sent))))

(defpage [:get "/scores"] {}
  (layout :highscore (scores (db/get-top-scores 10))))

(defpage  "/about" {}
  (layout :about [:div#text (slurp "about.txt")]))

(defpage "/afinn" {} 
  (layout :afinn [:div#pre [:pre (slurp "AFINN-README.txt")]]))


