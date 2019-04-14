(ns nedap.utils.spec.impl.predicates)

(defn coercer [pred]
  #?(:clj  (fn [x]
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
                                   (catch #?(:clj Exception :cljs js/Error) _
                                     x))))
     :cljs (fn [x]
             (cond
               (pred x)          x
               (not (string? x)) x
               :else             (try
                                   (js/parseInt x 10)
                                   (catch js/Error _
                                     x))))))
