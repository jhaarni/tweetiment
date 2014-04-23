(ns tweetiment.twitter
  (:require
   [tweetiment.util :refer [as-resource]]
   [clojure.java.io :as io]
   [oauth.client :refer :all]   
   [twitter.oauth :refer :all]
   [twitter.callbacks :refer :all]
   [twitter.callbacks.handlers :refer :all]
   [twitter.api.restful :refer :all]
   [clojure.tools.reader.edn :as edn])
  (:import
   (twitter.callbacks.protocols SyncSingleCallback)))

(def config 
  (if-let [res (as-resource "config.edn")] 
    (edn/read-string (slurp res))
    ;else
    {:api-key (System/getenv "TW_API_KEY")
     :api-secret (System/getenv "TW_API_SECRET")
     :access-token (System/getenv "TW_ACCESS_TOKEN")
     :token-secret (System/getenv "TW_TOKEN_SECRET")}))

(def my-creds (make-oauth-creds (:api-key config)
                                (:api-secret config)
                                (:access-token config)
                                (:token-secret config)))

(def consumer (make-consumer
  (:api-key config) 
  (:api-secret config)
  "https://api.twitter.com/oauth/request_token"
  "https://api.twitter.com/oauth/access_token"
  "https://api.twitter.com/oauth/authorize"
  :hmac-sha1))

(defn get-request-token [callback-uri]
  (request-token consumer callback-uri))

(defn oauth-uri [request-token]
  (user-approval-uri consumer (:oauth_token request-token)))

(defn get-access-token [request-token verifier] 
  (access-token consumer request-token verifier))

(defn timeline [user]  
  (statuses-user-timeline :oauth-creds my-creds :params {:screen-name user :count 100}))

