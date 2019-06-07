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
(def accepts-number-fn (speced/fn [^number? x] x))
(def returns-number-fn (speced/fn ^number? returns-number-fn [x] x))

(speced/defprotocol ExampleProtocol
  "Docstring"
  (accepts-string [this ^string? value] "Docstring")

  (^string?
   returns-string [this value] "Docstring"))

(defrecord ExampleRecord []
  ExampleProtocol
  (--accepts-string [this x] x)
  (--returns-string [this x] x))

(deftest spec-assertion-thrown?-defmethod
  #?(:clj
     (do
      (is (spec-assertion-thrown? 'string? (sut/check! string? 123)))
      (is (spec-assertion-thrown? 'number? (sut/check! number? "123")))
      (is (spec-assertion-thrown? ::age    (accepts-age "1234")))
      (is (spec-assertion-thrown? ::age    (returns-age "1234")))
      (is (spec-assertion-thrown? 'number? (accepts-number "1234")))
      (is (spec-assertion-thrown? 'number? (returns-number "1234")))
      (is (spec-assertion-thrown? 'number? (accepts-number-fn "1234")))
      (is (spec-assertion-thrown? 'number? (returns-number-fn "1234")))
      (is (spec-assertion-thrown? 'string? (accepts-string (->ExampleRecord) 1234)))
      (is (spec-assertion-thrown? 'string? (returns-string (->ExampleRecord) 1234))))))


