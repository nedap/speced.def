(ns nedap.utils.spec.predicates
  (:require
   [nedap.utils.speced :as speced]))

(speced/defn ^Boolean pos-integer?
  "Is `x` positive (as per `clojure.core/pos?`) and integer (as per `clojure.core/integer?`)?

  This function is recommended over `clojure.core/pos?` and `clojure.pos/pos-int?` for maximum abstraction."
  [x]
  (and (integer? x)
       (pos? x)))
