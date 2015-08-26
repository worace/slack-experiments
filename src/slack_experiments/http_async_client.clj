(ns slack-experiments.http-async-client)

;;HTTP Async-client
(defn on-open [ws]
  (println "Connected to WebSocket."))

(defn on-close [ws code reason]
  (println "Connection to WebSocket closed.\n"
           (format "(Code %s, reason: %s)" code reason)))

(defn on-error [ws e]
  (println "ERROR:" e))

(defn handle-message [ws msg]
  (prn "got message:" msg))

(defn http-async-client-conn [url]
  (let [client (ac/create-client)]
    (ac/websocket
     client
     url
     :open on-open
     :close on-close
     :byte handle-message)
    client))

(defn http-async-client-connect [url]
  (println "Connecting...")
  (with-open [client (ac/create-client)]
    (let [ws (ac/websocket client
                               url
                               :open  on-open
                               :close on-close
                               :byte  handle-message)]
      ;; this loop-recur is here as a placeholder to keep the process
      ;; from ending, so that the message-handling function will continue to
      ;; print messages to STDOUT until Ctrl-C is pressed
      (loop [] (recur)))))
