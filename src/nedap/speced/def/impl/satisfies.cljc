(ns nedap.speced.def.impl.satisfies
  (:refer-clojure :exclude [satisfies?])
  #?(:clj (:import (clojure.lang IMeta))))

(defn satisfies?
  [{:keys [extend-via-metadata method-builders] :as protocol}
   val]
  (or (and extend-via-metadata
           (instance? IMeta val)
           (some (partial contains? (meta val))
                 (map symbol (keys method-builders))))
      #?(:clj  (clojure.core/satisfies? protocol val)
         :cljs (cljs.core/satisfies? protocol val))))
