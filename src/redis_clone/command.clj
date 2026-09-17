(ns redis-clone.command 
  (:require
   [clojure.string :as str]
   [redis-clone.store :as store]))

(defn- ping-command
  []
  "PONG")

(defn- get-command
  [key store]
  (store/get-value store key))

(defn- set-command
  [key value store]
  (store/set-value! store key value)
  "OK")

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
      :else "ERR unknown command")))
