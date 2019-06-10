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

(def rebl-doc-registry
  "Like `#'doc-registry`, but in a format that REBL can be navigate and render better.

  In REBL, you can open `@rebl-doc-registry`,
  and see all registered specs that have declared docstrings."
  (atom {} :validator (fn [x]
                        (check! (spec/map-of ::doc-registry-symbol some?) x))))
