(ns unit.nedap.utils.api.satisfies
  (:refer-clojure :exclude [satisfies?])
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.utils.speced :as sut]))

(defprotocol DoThings
  :extend-via-metadata true
  (things       [_] "Does things")
  (other-things [_] "Does other things"))

(spec/def ::thing-doer #(sut/satisfies? DoThings %))

(defrecord NotRecordThings [])

(defrecord SoonRecordThings [])

(defrecord SoonPartialRecordThings [])

(defrecord PartialRecordThings []
  DoThings
  (things       [_] 1))

(defrecord RecordThings []
  DoThings
  (things       [_] 1)
  (other-things [_] 2))

(deftest satisfies?
  (testing "Ordinary type"
    (testing "Not implementing the protocol"
      (testing "is not valid"
        (is (not (spec/valid? ::thing-doer (->NotRecordThings))))))
    (testing "Partially implementing the protocol"
      (let [impl (->PartialRecordThings)]
        (testing "is valid"
          (is (spec/valid? ::thing-doer impl)))))
    (testing "Completely implementing the protocol"
      (let [impl (->RecordThings)]
        (testing "is valid"
          (is (spec/valid? ::thing-doer impl))))))

  (testing "Value with metadata"
    (testing "Not implementing the protocol"
      (let [impl {}]
        (testing "is not valid"
          (is (not (spec/valid? ::thing-doer impl))))))
    (testing "Partially implementing the protocol"
      (let [impl (with-meta {} {`things (fn [_] 1)})]
        (testing "is valid"
          (is (spec/valid? ::thing-doer impl)))))
    (testing "Completely implementing the protocol"
      (let [impl (with-meta {} {`things       (fn [_] 1)
                                `other-things (fn [_] 2)})]
        (testing "is valid"
          (is (spec/valid? ::thing-doer impl))))))

  (testing "Runtime-extended types"
    (testing "implementing the protocol"
      (let [impl (->SoonRecordThings)]
        (extend-type SoonRecordThings
          DoThings
          (things [_] :yes)
          (other-things [_] :no))
        (testing "is valid"
          (is (spec/valid? ::thing-doer impl)))))
    (testing "Partially implementing the protocol"
      (let [impl (->SoonPartialRecordThings)]
        (extend-type SoonPartialRecordThings
          DoThings
          (things [_] :yes))
        (testing "is valid"
          (is (spec/valid? ::thing-doer impl)))))))
