(ns unit.nedap.speced.def.predicates
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.speced.def.predicates :as sut]
   [spec-coerce.core :as spec-coerce]))

(deftest integers
  #?(:clj (are [v
                f ef
                g eg
                h eh] (do
                        (is (= ef (f v)))
                        (is (= eg (g v)))
                        (is (= eh (h v))))
            nil                               sut/neg-integer? false sut/nat-integer? false sut/pos-integer? false
            ""                                sut/neg-integer? false sut/nat-integer? false sut/pos-integer? false
            []                                sut/neg-integer? false sut/nat-integer? false sut/pos-integer? false
            Double/MIN_VALUE                  sut/neg-integer? false sut/nat-integer? false sut/pos-integer? false
            Double/MAX_VALUE                  sut/neg-integer? false sut/nat-integer? false sut/pos-integer? false
            -1.0                              sut/neg-integer? false sut/nat-integer? false sut/pos-integer? false
            0.0                               sut/neg-integer? false sut/nat-integer? false sut/pos-integer? false
            1.0                               sut/neg-integer? false sut/nat-integer? false sut/pos-integer? false

            -1                                sut/neg-integer? true  sut/nat-integer? false sut/pos-integer? false

            0                                 sut/neg-integer? false sut/nat-integer? true  sut/pos-integer? false

            1                                 sut/neg-integer? false sut/nat-integer? true  sut/pos-integer? true

            (Integer. -1)                     sut/neg-integer? true  sut/nat-integer? false sut/pos-integer? false
            (Long. -1)                        sut/neg-integer? true  sut/nat-integer? false sut/pos-integer? false
            (clojure.lang.BigInt/fromLong -1) sut/neg-integer? true  sut/nat-integer? false sut/pos-integer? false
            (BigInteger/valueOf -1)           sut/neg-integer? true  sut/nat-integer? false sut/pos-integer? false
            (Short. "-1")                     sut/neg-integer? true  sut/nat-integer? false sut/pos-integer? false
            (Long. -1)                        sut/neg-integer? true  sut/nat-integer? false sut/pos-integer? false

            (Integer. 0)                      sut/neg-integer? false sut/nat-integer? true  sut/pos-integer? false
            (Long. 0)                         sut/neg-integer? false sut/nat-integer? true  sut/pos-integer? false
            (clojure.lang.BigInt/fromLong 0)  sut/neg-integer? false sut/nat-integer? true  sut/pos-integer? false
            (BigInteger/valueOf 0)            sut/neg-integer? false sut/nat-integer? true  sut/pos-integer? false
            (Short. "0")                      sut/neg-integer? false sut/nat-integer? true  sut/pos-integer? false
            (Long. 0)                         sut/neg-integer? false sut/nat-integer? true  sut/pos-integer? false

            (Integer. 1)                      sut/neg-integer? false sut/nat-integer? true  sut/pos-integer? true
            (Long. 1)                         sut/neg-integer? false sut/nat-integer? true  sut/pos-integer? true
            (clojure.lang.BigInt/fromLong 1)  sut/neg-integer? false sut/nat-integer? true  sut/pos-integer? true
            (BigInteger/valueOf 1)            sut/neg-integer? false sut/nat-integer? true  sut/pos-integer? true
            (Short. "1")                      sut/neg-integer? false sut/nat-integer? true  sut/pos-integer? true
            (Long. 1)                         sut/neg-integer? false sut/nat-integer? true  sut/pos-integer? true)))

(deftest named?
  (are [x expectation] (= expectation
                          (sut/named? x))
    nil                false
    []                 false
    42                 false
    ""                 true
    (symbol "")        true
    (keyword "")       true
    "     "            true
    (symbol "     ")   true
    (keyword "     ")  true
    "  a   "           true
    (symbol "  a   ")  true
    (keyword "   a  ") true
    "a"                true
    'a                 true
    :a                 true
    "a/a"              true
    ::a                true
    'a/a               true))

(spec/def ::neg-integer? sut/neg-integer?)

(spec/def ::nat-integer? sut/nat-integer?)

(spec/def ::pos-integer? sut/pos-integer?)

(deftest coercion
  (testing "Valid values are coerced"
    (are [input spec output] (= output
                                (spec-coerce/coerce spec input))
      -1   ::neg-integer? -1
      "-1" ::neg-integer? -1

      0    ::nat-integer? 0
      "0"  ::nat-integer? 0
      1    ::nat-integer? 1
      "1"  ::nat-integer? 1

      1    ::pos-integer? 1
      "1"  ::pos-integer? 1))
  (testing "Invalid values (as per the predicates themselves) are not coerced"
    (are [input spec output] (= output
                                (spec-coerce/coerce spec input))
      nil  ::neg-integer? nil
      "a"  ::neg-integer? "a"
      0    ::neg-integer? 0
      1    ::neg-integer? 1
      "0"  ::neg-integer? "0"
      "1"  ::neg-integer? "1"

      nil  ::nat-integer? nil
      "a"  ::nat-integer? "a"
      -1   ::nat-integer? -1
      "-1" ::nat-integer? "-1"

      nil  ::pos-integer? nil
      "a"  ::pos-integer? "a"
      -1   ::pos-integer? -1
      "-1" ::pos-integer? "-1"
      0    ::pos-integer? 0
      "1"  ::pos-integer? 1))

  #?(:clj (testing "Types are preserved"
            (are [input spec] (= input
                                 (spec-coerce/coerce spec input))
              (Integer. -1)                     ::neg-integer?
              (Long. -1)                        ::neg-integer?
              (clojure.lang.BigInt/fromLong -1) ::neg-integer?
              (BigInteger/valueOf -1)           ::neg-integer?
              (Short. "-1")                     ::neg-integer?
              (Long. -1)                        ::neg-integer?

              (Integer. 0)                      ::nat-integer?
              (Long. 0)                         ::nat-integer?
              (clojure.lang.BigInt/fromLong 0)  ::nat-integer?
              (BigInteger/valueOf 0)            ::nat-integer?
              (Short. "0")                      ::nat-integer?
              (Long. 0)                         ::nat-integer?

              (Integer. 1)                      ::nat-integer?
              (Long. 1)                         ::nat-integer?
              (clojure.lang.BigInt/fromLong 1)  ::nat-integer?
              (BigInteger/valueOf 1)            ::nat-integer?
              (Short. "1")                      ::nat-integer?
              (Long. 1)                         ::nat-integer?

              (Integer. 1)                      ::pos-integer?
              (Long. 1)                         ::pos-integer?
              (clojure.lang.BigInt/fromLong 1)  ::pos-integer?
              (BigInteger/valueOf 1)            ::pos-integer?
              (Short. "1")                      ::pos-integer?
              (Long. 1)                         ::pos-integer?)))

  #?(:clj (testing "Values which would overflow a Long are coerced to BigInteger"
            (are [input spec expected-class] (= expected-class
                                                (-> (spec-coerce/coerce spec input)
                                                    class))
              "-1111"                             ::neg-integer? Long
              "-11111111111111111111111111111111" ::neg-integer? BigInteger
              (-> Long/MIN_VALUE str)             ::neg-integer? Long
              (-> Long/MIN_VALUE dec' str)        ::neg-integer? BigInteger
              "0"                                 ::nat-integer? Long
              "1111"                              ::nat-integer? Long
              "11111111111111111111111111111111"  ::nat-integer? BigInteger
              "1111"                              ::pos-integer? Long
              "11111111111111111111111111111111"  ::pos-integer? BigInteger
              (-> Long/MAX_VALUE str)             ::pos-integer? Long
              (-> Long/MAX_VALUE inc' str)        ::pos-integer? BigInteger))))
