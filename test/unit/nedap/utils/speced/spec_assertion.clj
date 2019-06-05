(ns unit.nedap.utils.speced.spec-assertion
  (:require [clojure.test :refer :all]
            [nedap.utils.spec.api :as sut]
            [nedap.utils.speced :as speced]
            [clojure.spec.alpha :as s]))

(s/def ::age number?)
(speced/defn accepts-age [^::age x] x)
(speced/defn ^::age returns-age [x] x)
(speced/defn accepts-number [^number? x] x)
(speced/defn ^number? returns-number [x] x)

(deftest spec-assertion-thrown?-defmethod
  (is (spec-assertion-thrown? 'string? (sut/check! string? 123)))
  (is (spec-assertion-thrown? 'number? (sut/check! number? "123")))
  (is (spec-assertion-thrown? ::age    (accepts-age "1234")))
  (is (spec-assertion-thrown? ::age    (returns-age "1234")))
  (is (spec-assertion-thrown? 'number? (accepts-number "1234")))
  (is (spec-assertion-thrown? 'number? (returns-number "1234"))))
