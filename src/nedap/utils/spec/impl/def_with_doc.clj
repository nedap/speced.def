(ns nedap.utils.spec.impl.def-with-doc
  (:require
   [clojure.spec.alpha :as spec]
   [nedap.utils.spec.api :refer [check!]]))

(defmacro def-with-doc
  [spec-name docstring spec]
  {:pre [(check! qualified-keyword? spec-name
                 string? docstring
                 some? spec)]}
  `(spec/def ~spec-name ~spec))
