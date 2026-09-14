(ns redis-clone.server
  (:require [redis-clone.command :as commands])
  (:import [java.net ServerSocket]
           [java.io BufferedReader InputStreamReader PrintWriter]))

(defn handle-client
  [client]
  (let [reader (-> client
                   (.getInputStream)
                   (InputStreamReader.)
                   (BufferedReader.))
        writer (PrintWriter.
                (.getOutputStream client)
                true)]
    (loop []
      (let [request (.readLine reader)]
        (when request
          (println "Recieved:" request)

          (let [response (commands/handle-command request)]
            (.println writer response))))
      (recur))))

(defn start!
  [port]  
  (with-open [server (ServerSocket. port)]
    (println (str "Starting server on localhost:" port))

    (let [client (.accept server)]
      (println "Client connected:" (.getRemoteSocketAddress client))
      (handle-client client))))

