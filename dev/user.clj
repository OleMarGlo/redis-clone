(ns user
  (:require [redis-clone.server :as server]))

(defonce running-server (atom nil))

(defn start []
  (when-not @running-server
    (reset! running-server (server/start! 9090))))

(defn stop []
  (when-let [s @running-server]
    (server/stop! s)
    (reset! running-server nil)))

(start)
