(ns nedap.utils.spec.doc
  (:require
   [clojure.spec.alpha :as spec]
   [nedap.utils.spec.api :refer [check!]]))

(def doc-registry
  "Whenever `#'def-with-doc` is used, the spec (keyword) -> docstring (string) mapping will be stored here,
  so arbitrary tools can query it."
  (atom {} :validator (fn [x]
                        (check! (spec/map-of qualified-keyword? some?) x))))

(spec/def ::doc-registry-symbol (spec/and qualified-symbol?
                                          (fn [s]
                                            (let [k (keyword s)]
                                              (get @doc-registry k)))))

(def doc-registry-as-symbols
  "Like `#'doc-registry`, but keys are symbols instead of keywords.
  Those symbols will map to REBL-friendly navigable objects.

  In REBL, you can open `@doc-registry-as-symbols`,
  and see all registered specs that have declared docstring."
  (atom {} :validator (fn [x]
                        (check! (spec/map-of ::doc-registry-symbol some?) x))))
