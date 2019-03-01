(ns nedap.utils.spec.impl.satisfies
  (:import (clojure.lang IObj)))

(defn satisfies-or-meta?
  [proto val]
  "Checks whether `val` satisfies `protocol` by first checking whether there
  is an implementation in metadata, or otherwise consulting
  `clojure.core/satisfies?`.

  This function is a (suggested) workaround for
  https://dev.clojure.org/jira/browse/CLJ-2426"
  (or (and (:extend-via-metadata proto)
           (instance? IObj val)
           (every? #(contains? (meta val) %)
                   (map symbol (keys (:method-builders proto)))))
      (satisfies? proto val)))
