(ns redis-clone.resp.decoder)

(defn- read-until-crlf!
  [input]
  (loop [buffer []
         b (.read input)]
    (if (= b (int \newline))
      (if (= (last buffer) (int \return))
        (pop buffer)
        (throw (ex-info "Malformed RESP line ending"
                        {:type :invalid-resp})))
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
    (throw (ex-info "Malformed RESP integer"
                    {:type :invalid-resp}))))

(defn- read-exactly!
  [input amount]
  (let [buffer (byte-array amount)]
    (loop [offset 0]
      (if (= offset amount)
        buffer
        (let [read-count (.read input buffer offset (- amount offset))]
          (if (= read-count -1)
            (throw (ex-info "Malformed RESP data"
                            {:type :invalid-resp}))
            (recur (+ offset read-count))))))))

(defn- consume-crlf!
  [input]
  (let [bytes (read-exactly! input 2)]
    (when-not (and (= (aget bytes 0) (int \return))
                   (= (aget bytes 1) (int \newline)))
      (throw (ex-info "Malformed RESP line ending"
                      {:type :invalid-resp})))))

(declare decode)
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
