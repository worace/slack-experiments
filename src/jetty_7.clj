(ns jetty-7
  (require [clojure.core.async :as a]
           [com.keminglabs.jetty7-websockets-async.core :as ws]))

(def url "ws://localhost:8080")

(def c (a/chan))

(ws/connect! c url)

(a/go (loop []
        (let [ws-req (a/<! c)]
          (a/>! (:in ws-req) "Hello remote websocket server!")
          (recur))))


