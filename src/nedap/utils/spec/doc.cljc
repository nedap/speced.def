(ns nedap.utils.spec.doc
  (:require
   [clojure.spec.alpha :as spec]
   [nedap.utils.spec.api :refer [check!]]))

(def doc-registry
  "Whenever `#'def-with-doc` is used, the spec (keyword) -> docstring (string) mapping will be stored here,
  so arbitrary tools can query it."
  (atom {} :validator (fn [x]
                        (check! (spec/map-of qualified-keyword? some?) x))))

(def doc-registry-as-symbols
  "Like `#'doc-registry`, but keys are symbols instead of keywords.
  Those symbols will have `:doc` metadata attached to them.

  Apt for REBL usage: you can open `@doc-registry-as-symbols`,
  and see all registered specs that have declared docstring.

  Given the REBL orientation, this is a set rather than an atom, so the collection will be rendered in a more compact format
  (namely: only keys, instead of keys and values)"
  (atom #{} :validator (fn [x]
                         (check! (spec/coll-of (and qualified-symbol?
                                                    (fn [s]
                                                      (get @doc-registry (keyword s))))
                                               :kind set?)
                                 x))))
