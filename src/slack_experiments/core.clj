(ns slack-experiments.core
  (:require [clj-slack-client.core :as client]
            [clj-slack-client.web :as slack-api]
            [com.keminglabs.jetty7-websockets-async.core :as jetty-ws]
            [clojure.core.async :as a]
            [aleph.http :as http]
            [manifold.deferred :as d]
            [manifold.stream :as s]))

(def api-token "xoxb-5170934342-uH6E76g6nfAU6e3qyJwnOBdE")

(defn message-handler [event]
  (println "got slack event: " event))

(defn get-fresh-ws-url []
  (:url (slack-api/rtm-start api-token)))

(defn ws-connection [url]
  @(http/websocket-client url))

;; Attempt at manually looping on it...
#_(defn conn-loop [url]
  (d/loop [conn (ws-connection url)]
    (d/let-flow [taken (s/take! conn)]
                (println "taken: " taken)
                (println (type taken))
                (d/recur conn))))

(defn conn-loop [url]
  (let [conn (ws-connection url)]
    (d/loop [counter 50]
      (Thread/sleep 100)
      (d/let-flow [message (s/take! conn)]
                  (println "got thing: " @message)
                  (if (> counter 0)
                    (d/recur (dec counter))))
      )))

(defn my-consume [f stream]
  (d/loop []
    (d/chain (s/take! stream ::drained)

             ;; if we got a message, run it through `f`
             (fn [msg]
               (if (identical? ::drained msg)
                 ::drained
                 (f msg)))

             ;; wait for the result from `f` to be realized, and
             ;; recur, unless the stream is already drained
             (fn [result]
               (when-not (identical? ::drained result)
                 (d/recur))))))





