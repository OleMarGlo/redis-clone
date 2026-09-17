(ns redis-clone.command 
  (:require
   [clojure.string :as str]
   [redis-clone.store :as store]))

(defn- ping-command
  []
  "PONG")

(defn- get-command
  [key kv-store]
  (store/get-value kv-store key))

(defn- set-command
  [key value kv-store]
  (store/set-value! kv-store key value)
  "OK")

(defn- delete-command
  [key kv-store]
  (store/delete-value! kv-store key))

(defn- parse-command
  [cmd]
  (str/split cmd #"\s+"))

(defn handle-command
  [command
   kv-store]
  (let [[cmd & args] (parse-command command)]
    (cond
      (= cmd "PING") (ping-command)
      (= cmd "SET") (let [[key value] args]
                      (set-command key value kv-store))
      (= cmd "GET") (let [[key] args]
                      (get-command key kv-store))
      (= cmd "DELETE") (let [[key] args]
                       (delete-command key kv-store))
      :else "ERR unknown command")))
