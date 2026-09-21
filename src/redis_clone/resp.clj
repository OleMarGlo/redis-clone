(ns redis-clone.resp
  (:require
   [redis-clone.resp.decoder :as decoder]))

(defn test-input [s]
  (java.io.ByteArrayInputStream.
   (.getBytes s "UTF-8")))

(defn decode
  [input]
  (let [b (.read input)]
    (when-not (= b -1)
      (let [prefix (char b)]
        (case prefix
          \* (decoder/bytes->array! input)
          \: (decoder/bytes->integer (decoder/read-until-crlf! input))
          \$ (decoder/read-bulk-string! input)
          \+ ()
          \- ()
          (throw (ex-info "Uknown RESP type"
                          {:type :invalid-resp
                           :prefix prefix})))))))
