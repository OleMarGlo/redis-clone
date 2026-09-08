(ns redis-clone.core 
  (:require
    [redis-clone.server :as server]))

(defn -main
  [& args]
  (let [port-str (first args)
        port (if port-str (Integer/parseInt port-str) 9090)]
    (server/start! port)))

