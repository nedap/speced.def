(ns nedap.speced.def.specs
  "Specs for this library."
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   [nedap.speced.def.doc :refer [doc-registry rebl-doc-registry]]
   [nedap.speced.def.impl.def-with-doc #?(:clj :refer :cljs :refer-macros) [def-with-doc]]
   [nedap.speced.def.impl.type-hinting :refer [type-hint?]]))

(def ^:dynamic *clj?* nil)

(def-with-doc ::concise-format
  "Example: ^::foo

(i.e. a namespace-qualified spec name with a `true` value)"
  (spec/and map?
            (partial some (fn [[k v]]
                            (and (qualified-keyword? k)
                                 (true? v)))))
  doc-registry
  rebl-doc-registry)

(def-with-doc ::explicit-format
  "Example: ^{::speced/spec ::foo}

(i.e. using a :nedap.speced.def/spec key, with an arbitrary spec as a value)"
  (spec/and map?
            (partial some (fn [[k v]]
                            (= k :nedap.speced.def/spec))))
  doc-registry
  rebl-doc-registry)

(def-with-doc ::type-hint
  "Example: ^Integer (class hint), ^double (primitive hint)

(i.e. a regular Clojure/ClojureScript type hint, from which a spec and efficient code will be emitted)"
  (spec/and map?
            (partial some (fn [[k v]]
                            {:pre [(boolean? *clj?*)]}
                            (and (= :tag k)
                                 (type-hint? v *clj?*)))))
  doc-registry
  rebl-doc-registry)

(def-with-doc ::inline-function
  "Example: ^boolean?

i.e. a function, passed as if it were type hint. A spec will be emitted against that function.
The function will be detected as such whenever the type hint is not a symbol, a class, or a primitive hint.

If possible, a `:tag` hint will be inferred out of the function spec (e.g. `string?` -> `String`).
Else, the `:tag` metadata will be removed, so as to emit valid Clojure code."
  (spec/and map?
            (partial some (fn [[k v]]
                            (and (= :tag k)
                                 (not (type-hint? v))))))
  doc-registry
  rebl-doc-registry)

(def-with-doc ::spec-metadata
  "'Spec metadata' is metadata passed to this namespace's `#'defn` and `#'defprotocol`, in:

   * argument positions, i.e. before a given argument in the argument vector.
     * within arguments, any destructured _symbols_, no matter the nesting, can also be attached metadata.
       * Note that only symbols are observed: attaching metadata to hashmaps/vectors will throw a compile-time error.
       * Rationale 1: preconditions should be backed by names you specify, not autogenerated names
       * Rationale 2: fewer choices for metadata placement = simpler implementation and simpler usage.

   * return value positions, i.e. before a function's name, or before an argument vector.
     * A defn with return value metadata for both its name and argument vector will emit spec checking for both.
     * `:tag` metadata will be automatically emitted in the right position depending on the compiler (see https://git.io/fjuk7 )

  Refer to the tests (and the project's README) for examples, and to this spec/ns for format descriptions + docstrings."
  (spec/or :concise-format  ::concise-format
           :explicit-format ::explicit-format
           :type-hint       ::type-hint
           :inline-function ::inline-function)
  doc-registry
  rebl-doc-registry)
