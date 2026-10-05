(ns redis-clone.resp.decoder 
  (:require
    [redis-clone.error :as error]))

(declare decode)

(defn- read-until-crlf!
  [input]
  (loop [buffer []
         b (.read input)]
    (if (= b (int \newline))
      (if (= (last buffer) (int \return))
        (pop buffer)
        (error/throw! ::malformed-resp))
      (recur (conj buffer b) (.read input)))))

(defn- bytes->string
  [buffer]
  (String. buffer "UTF-8"))

(defn- bytes->integer
  [buffer]
  (if-let [num (parse-long (->> buffer
                                (map char)
                                (apply str)))]
    num
    (error/throw! ::malformed-resp)))

(defn- read-exactly!
  [input amount]
  (let [buffer (byte-array amount)]
    (loop [offset 0]
      (if (= offset amount)
        buffer
        (let [read-count (.read input buffer offset (- amount offset))]
          (if (= read-count -1)
            (error/throw! ::malformed-resp)
            (recur (+ offset read-count))))))))

(defn- consume-crlf!
  [input]
  (let [bytes (read-exactly! input 2)]
    (when-not (and (= (aget bytes 0) (int \return))
                   (= (aget bytes 1) (int \newline)))
      (error/throw! ::malformed-resp))))

(defn- bytes->array!
  [input]
  (let [amount (bytes->integer (read-until-crlf! input))]
    (loop [cur 0
           decoded []]
      (if (= cur amount)
        decoded
        (do
          (println decoded)
          (recur (inc cur)
                 (conj decoded (decode input))))))))

(defn- read-bulk-string!
  [input]
  (let [bytes (->> (read-until-crlf! input)
                   bytes->integer
                   (read-exactly! input))]
    (consume-crlf! input)
    (bytes->string bytes)))

(defn- read-string!
  [input]
  (->> input
       read-until-crlf!
       bytes->string))

(defn- read-error!
  [input]
  (let [message (->> input
             read-until-crlf!
             bytes->string)]
    (error/replace-message-in-error ::general-error message)))

(defn decode
  [input]
  (let [b (.read input)]
    (when-not (= b -1)
      (let [prefix (char b)]
        (case prefix
          \* (bytes->array! input)
          \: (bytes->integer (read-until-crlf! input))
          \$ (read-bulk-string! input)
          \+ (read-string! input)
          \- (read-error! input)
          (throw (ex-info "Uknown RESP type"
                          {:type :invalid-resp
                           :prefix prefix})))))))
