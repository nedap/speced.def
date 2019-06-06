(ns unit.nedap.utils.speced.spec-assertion
  (:require
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   [nedap.utils.spec.api :as sut]
   [nedap.utils.speced :as speced]))

(spec/def ::age number?)
(speced/defn accepts-age [^::age x] x)
(speced/defn ^::age returns-age [x] x)
(speced/defn accepts-number [^number? x] x)
(speced/defn ^number? returns-number [x] x)

(deftest spec-assertion-thrown?-defmethod
  #?(:clj
     (do
      (is (spec-assertion-thrown? 'string? (sut/check! string? 123)))
      (is (spec-assertion-thrown? 'number? (sut/check! number? "123")))
      (is (spec-assertion-thrown? ::age    (accepts-age "1234")))
      (is (spec-assertion-thrown? ::age    (returns-age "1234")))
      (is (spec-assertion-thrown? 'number? (accepts-number "1234")))
      (is (spec-assertion-thrown? 'number? (returns-number "1234"))))))
