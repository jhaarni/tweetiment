(ns tweetiment.twitter
  (:require
   [oauth.client :refer :all]   
   [twitter.oauth :refer :all]
   [twitter.callbacks :refer :all]
   [twitter.callbacks.handlers :refer :all]
   [twitter.api.restful :refer :all]
   [clojure.tools.reader.edn :as edn])
  (:import
   (twitter.callbacks.protocols SyncSingleCallback)))

(def config (edn/read-string (slurp "config.edn")))

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
  (statuses-user-timeline :oauth-creds my-creds :params {:screen-name user}))

