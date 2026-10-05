(ns redis-clone.resp
  (:require
   [redis-clone.resp.decoder :as decoder]
   [redis-clone.resp.encoder :as encoder]))

(defn test-input [s]
  (java.io.ByteArrayInputStream.
   (.getBytes s "UTF-8")))

(defn decode
  [input]
  (decoder/decode input))

(defn encode
  [input]
  (encoder/encode input))
