(ns redis-clone.command)

(defn- ping-command
  []
  "PONG")

(defn handle-command
  [command]
  (cond
    (= command "PING") (ping-command)
    :else "ERR unknown command"))
