(ns nedap.utils.spec.api
  (:require
   [clojure.spec.alpha :as spec]
   [nedap.utils.spec.impl.check]
   [nedap.utils.spec.impl.satisfies]
   [spec-coerce.core :as coerce]))

(defmacro check!
  "Asserts validity, explaining the cause otherwise. Apt for :pre conditions.

  `args` is a sequence of spec-val pairs."
  [& args]
  {:pre [(-> args count even?)]}
  `(nedap.utils.spec.impl.check/check! ~@args))

(defn coerce-map-indicating-invalidity
  "Tries to coerce the map `m` according to spec `spec`.

  If the coercion isn't possible, `::invalid? true` is associated to the map."
  [spec m]
  ;; Very important: specs must be passed as keywords or symbols,
  ;; but never 'inline' as any other kind of objects.
  ;; Else spec-coerce will fail to coerce things.
  {:pre [(check! qualified-ident? spec
                 map? m)]}
  (let [m (coerce/coerce spec m)]
    (cond-> m
      (not (spec/valid? spec m)) (assoc ::invalid? true))))

(defmacro satisfies-protocol?
  "Generates a predicate that returns true for values that implement `proto`.

  Checking whether vals implement a protocol is done in two ways, either by
  looking for an implementation in metadata of a value or by consulting
  `clojure.core/satisfies?`. See also:
  `nedap.utils.spec.impl.satisfies/satisfies-or-meta?`

  Implementation detail: this check is implemented as a macro. Implementing it
  as a ordinary partial function application causes vals that are implementing
  protocols at runtime to not be considered implementations and also causes
  issues with hot reloading of code."
  [proto]
  `(fn [val#]
     (nedap.utils.spec.impl.satisfies/satisfies-or-meta? ~proto val#)))
