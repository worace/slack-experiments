(ns slack-experiments.ws-example
  (:require
   [compojure.core :as compojure :refer [GET]]
   [ring.middleware.params :as params]
   [compojure.route :as route]
   [aleph.http :as http]
   [byte-streams :as bs]
   [manifold.stream :as s]
   [manifold.deferred :as d]
   [manifold.bus :as bus]
   [clojure.core.async :as a]
   [gniazdo.core :as ws]
   ))


(def non-ws-resp
  {:status 400
   :headers {"content-type" "application/text"}
   :body "Expected a websocket request."})

(defn echo-handler
  [req]
  (if-let [socket (try
                    @(http/websocket-connection req)
                    (catch Exception e
                      nil))]
    (do (println "got ws req: " req)
        (s/connect socket socket))
    non-ws-resp))

;; Compojure handler
(def handler
  (params/wrap-params
   (compojure/routes
    (GET "/echo" [] echo-handler)
    (route/not-found "No such page."))))

(defn consumer-fn [msg]
  (println "consumer received msg: " msg))

(defn start-server []
  (http/start-server handler {:port 10000}))

(defn ws-connect []
  (ws/connect "ws://localhost:10000/echo"
              :on-connect #(prn 'connected)
              :on-error #(prn 'error)
              :on-receive consumer-fn))

#_(with-open [server (http/start-server handler {:port 10000})]
  (let [conn @(http/websocket-client "ws://localhost:10000/echo")]
    (s/consume consumer-fn conn)
    (for [i (range 3)] (s/put! conn (str i)))
    ))

#_(try
  (let [server (http/start-server handler {:port 10000})
      conn @(http/websocket-client "ws://localhost:10000/echo")]
    (stream/consume consumer-fn conn)
    (for [i (range 3)] (stream/put! conn (str i)))
    (.close server))
  (catch Exception e (str "caught exception: " (.getMessage e)))
  (finally ))
