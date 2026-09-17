(ns redis-clone.server
  (:require [redis-clone.command :as commands]
            [redis-clone.store :as store])
  (:import [java.io BufferedReader InputStreamReader PrintWriter]
           [java.net ServerSocket]))

(defn handle-client
  [client store]
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

        (let [response (commands/handle-command request store)]
          (.println writer response))

        (recur)))))

(defn run-server!
  [{:keys [store listener clients]}]
  (loop []
    (let [client (.accept listener)]

      (swap! clients conj client)

      (try
        (handle-client client store)
        (finally
          (swap! clients disj client)
          (.close client))))
    (recur)))

(defn start!
  [port]
  (let [kv-store (store/create-store)
        listener (ServerSocket. port)
        clients  (atom #{})
        server     {:listener listener
                    :clients clients
                    :store kv-store}
        worker   (future
                   (run-server! server))]

    (println (str "Starting server on localhost:" port))
    (assoc server :worker worker)))

(defn stop!
  [server]
  (.close (:listener server))

  (doseq [client @(:clients server)]
    (.close client)))

