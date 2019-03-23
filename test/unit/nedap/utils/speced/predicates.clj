(ns unit.nedap.utils.speced.predicates
  (:require
   [clojure.test :refer :all]
   [nedap.utils.spec.predicates :as sut]))

(deftest integers
  (are [v
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
    (Long. 1)                         sut/neg-integer? false sut/nat-integer? true  sut/pos-integer? true))
