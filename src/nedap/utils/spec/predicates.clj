(ns nedap.utils.spec.predicates
  (:require
   [nedap.utils.speced :as speced]))

(speced/defn ^Boolean neg-integer?
  "Is `x` negative (as per `clojure.core/neg?`) and integer (as per `clojure.core/integer?`)?

  This function is recommended over `clojure.core/neg?` and `clojure.pos/neg-int?` for maximum abstraction over specific types."
  [x]
  (and (integer? x)
       (neg? x)))

(speced/defn ^Boolean nat-integer?
  "Is `x` non-negative (as per `clojure.core/nat-int?`) and integer (as per `clojure.core/integer?`)?

  This function is recommended over `clojure.core/nat-int?`r maximum abstraction over specific types."
  [x]
  (and (integer? x)
       (not (neg? x))))

(speced/defn ^Boolean pos-integer?
  "Is `x` positive (as per `clojure.core/pos?`) and integer (as per `clojure.core/integer?`)?

  This function is recommended over `clojure.core/pos?` and `clojure.pos/pos-int?` for maximum abstraction over specific types."
  [x]
  (and (integer? x)
       (pos? x)))
