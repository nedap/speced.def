(ns nedap.utils.spec.impl.predicates)

(defn coercer [pred]
  (fn [x]
    (cond
      (pred x)          x
      (not (string? x)) x
      :else             (try
                          (let [v (BigInteger. ^String x)]
                            (if-not (pred v)
                              x
                              (if (<= Long/MIN_VALUE v Long/MAX_VALUE)
                                (Long/parseLong x)
                                v)))
                          (catch Exception _
                            x)))))
