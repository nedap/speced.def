(ns nedap.utils.spec.impl.satisfies
  (:refer-clojure :exclude [satisfies?])
  (:import
   (clojure.lang IMeta)))

(defn satisfies?
  [{:keys [extend-via-metadata method-builders] :as protocol}
   val]
  (or (and extend-via-metadata
           (instance? IMeta val)
           (some (partial contains? (meta val))
                 (map symbol (keys method-builders))))
      (clojure.core/satisfies? protocol val)))
