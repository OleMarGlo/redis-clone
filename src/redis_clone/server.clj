(ns redis-clone.server
  (:require [redis-clone.resp :as resp]
            [redis-clone.store :as store]
            [redis-clone.command :as commands])
  (:import [java.net ServerSocket]))

(defn handle-client
  [client store]
  (let [input (.getInputStream client)
        output (.getOutputStream client)]
    (loop []
      
      (println "Waiting for resp")
      (when-let [request (resp/decode input)]
        (println "decoded request:" (pr-str request))
        (let [response (commands/handle-command request store)]
          (.write output (.getBytes (pr-str response))))
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

