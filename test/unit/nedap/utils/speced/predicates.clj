(ns unit.nedap.utils.speced.predicates
  (:require
   [clojure.test :refer :all]
   [nedap.utils.spec.predicates :as sut]))

(deftest pos-integer?
  (are [x expected] (= expected
                       (sut/pos-integer? x))
    nil                              false
    ""                               false
    []                               false
    -1                               false
    -1.0                             false
    0                                false
    0.0                              false
    1.0                              false
    Double/MAX_VALUE                 false
    1                                true
    (Integer. 1)                     true
    (Long. 1)                        true
    (clojure.lang.BigInt/fromLong 1) true
    (BigInteger/valueOf 1)           true
    (Short. "1")                     true
    (Long. 1)                        true))
