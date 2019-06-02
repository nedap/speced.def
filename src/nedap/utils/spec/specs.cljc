(ns nedap.utils.spec.specs
  "Specs for this library."
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   [nedap.utils.spec.impl.def-with-doc #?(:clj :refer :cljs :refer-macros) [def-with-doc]]
   [nedap.utils.spec.impl.type-hinting :refer [type-hint?]]))

(def-with-doc ::concise-format
  "Example: ^::foo
(i.e. namespace-qualified spec name with a `true` value)"
  (spec/and map?
            (partial some (fn [[k v]]
                            (and (qualified-keyword? k)
                                 (true? v))))))

(def-with-doc ::explicit-format
  "Example: ^{::speced/spec ::foo}
(i.e. using a :nedap.utils.speced/spec key, with an arbitrary spec as a value)"
  (spec/and map?
            (partial some (fn [[k v]]
                            (= k :nedap.utils.speced/spec)))))

(def-with-doc ::type-hint
  "Example: ^Int
(i.e. a regular Clojure type hint, from which a spec and efficient code will be emmited)"
  (spec/and map?
            (partial some (fn [[k v]]
                            (and (= :tag k)
                                 (type-hint? v))))))

(def-with-doc ::inline-function
  "Example: ^boolean?
i.e. a function, passed as if it were type hint. A spec will be emitted against that function.
The function will be detected as such whenever the type hint is not a symbol or a class.
No :tag metadata will be emitted, since that would that would generate invalid Clojure code."
  (spec/and map?
            (partial some (fn [[k v]]
                            (and (= :tag k)
                                 (not (type-hint? v)))))))

(def-with-doc ::spec-metadata
  "'Spec metadata' is metadata passed to this namespace's `#'defn` and `#'defprotocol`, in:

   * argument positions, i.e. before a given argument in the argument vector; and
   * return value positions, i.e. before a function's name, or before an argument vector.

  A defn with return value metadata for both its name and argument vector will emit spec checking for both.

  Refer to the tests (and the project's README) for examples, and to this spec/ns for format descriptions + docstrings."
  (spec/or :concise-format ::concise-format
           :explicit-format ::explicit-format
           :type-hint ::type-hint
           :inline-function ::inline-function))
