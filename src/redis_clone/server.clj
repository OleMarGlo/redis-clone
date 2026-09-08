(ns redis-clone.server)

(defn start!
  [port]  
  (with-open [server (java.net.ServerSocket. port)]
    (println (str "Starting server on localhost:" port))

    (let [client (.accept server)]
      (println "Client connected:" (.getRemoteSocketAddress client))
      (.close client))))


