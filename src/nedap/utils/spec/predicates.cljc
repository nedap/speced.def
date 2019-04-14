(ns nedap.utils.spec.predicates
  (:require
   [nedap.utils.spec.impl.predicates :as impl]
   [nedap.utils.speced :as speced]
   [spec-coerce.core :as spec-coerce]))

(speced/defn ^boolean? neg-integer?
  "Is `x` negative (as per `clojure.core/neg?`) and integer (as per `clojure.core/integer?`)?

  This function is recommended over `clojure.core/neg?` and `clojure.pos/neg-int?` for maximum abstraction over specific types."
  [x]
  (and (integer? x)
       (neg? x)))

(speced/defn ^boolean? nat-integer?
  "Is `x` non-negative (as per `clojure.core/nat-int?`) and integer (as per `clojure.core/integer?`)?

  This function is recommended over `clojure.core/nat-int?`r maximum abstraction over specific types."
  [x]
  (and (integer? x)
       (not (neg? x))))

(speced/defn ^boolean? pos-integer?
  "Is `x` positive (as per `clojure.core/pos?`) and integer (as per `clojure.core/integer?`)?

  This function is recommended over `clojure.core/pos?` and `clojure.pos/pos-int?` for maximum abstraction over specific types."
  [x]
  (and (integer? x)
       (pos? x)))

(speced/defn ^boolean? named?
  "Is `x` something that `clojure.core/name` can handle?"
  [x]
  (or (string? x)
      (symbol? x)
      (keyword? x)))

(def neg-integer-coercer (impl/coercer neg-integer?))

(def nat-integer-coercer (impl/coercer nat-integer?))

(def pos-integer-coercer (impl/coercer pos-integer?))

(defmethod spec-coerce/sym->coercer `neg-integer? [_] neg-integer-coercer)

(defmethod spec-coerce/sym->coercer `nat-integer? [_] nat-integer-coercer)

(defmethod spec-coerce/sym->coercer `pos-integer? [_] pos-integer-coercer)
