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
      (when-let [request (.readLine reader)]
        (println "Received:" request)

        (let [response (commands/handle-command request)]
          (.println writer response))

        (recur)))))

(defn run-server!
  [listener clients]
  (loop []
    (let [client (.accept listener)]

      (swap! clients conj client)

      (try
        (handle-client client)
        (finally
          (swap! clients disj client)
          (.close client))))
    (recur)))

(defn start!
  [port]
  (let [listener (ServerSocket. port)
        clients  (atom #{})
        worker   (future
                   (run-server! listener clients))]

    (println (str "Starting server on localhost:" port))

    {:listener listener
     :clients clients
     :worker worker}))

(defn stop!
  [server]
  (.close (:listener server))

  (doseq [client @(:clients server)]
    (.close client)))

