(ns redis-clone.store)

(defprotocol Store
  (set-value! [store key value])
  (get-value [store key])
  (delete-value! [store key]))

(defn create-store
  []
  (let [state (atom {})]
    (reify Store
      (set-value! [this key value]
        (swap! state assoc key value))
      (get-value [this key]
        (get @state key))
      (delete-value! [this key]
        (let [[before after] (swap-vals! state dissoc key)]
          (- (count before) (count after)))))))

