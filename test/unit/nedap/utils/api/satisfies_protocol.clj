(ns unit.nedap.utils.api.satisfies-protocol
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer :all]
   [nedap.utils.spec.api :as sut]))

(defprotocol DoThings
  :extend-via-metadata true
  (things       [_] "Does things")
  (other-things [_] "Does other things"))

(spec/def ::thing-doer (sut/satisfies-protocol? DoThings))

(defrecord NotRecordThings [])

(defrecord SoonRecordThings [])

(defrecord RecordThings []
  DoThings
  (things       [_] 1)
  (other-things [_] 2))

(deftest satisfies-protocol?
  (testing "Ordinary type"
    (testing "Not implementing the protocol"
      (testing "is not valid"
        (is (not (spec/valid? ::thing-doer (->NotRecordThings))))))
    (testing "Implementing the protocol"
      (let [impl (->RecordThings)]
        (testing "is valid"
          (is (spec/valid? ::thing-doer impl))))))

  (testing "Value with metadata"
    (testing "Not implementing the protocol"
      (let [impl {}]
        (testing "is not valid"
          (is (not (spec/valid? ::thing-doer impl))))))
    (testing "Partial implementing the protocol"
      (let [impl (with-meta {} {`things (fn [_] 1)})]
        (testing "is not valid"
          (is (not (spec/valid? ::thing-doer impl))))))
    (testing "Completely implementing the protocol"
      (let [impl (with-meta {} {`things       (fn [_] 1)
                                `other-things (fn [_] 2)})]
        (testing "is valid"
          (is (spec/valid? ::thing-doer impl))))))

  (testing "Value runtime extended"
    (testing "implementing the protocol"
      (let [impl (->SoonRecordThings)]
        (extend-type SoonRecordThings
          DoThings
          (things [_] :yes)
          (other-things [_] :no))
        (testing "is valid"
          (is (spec/valid? ::thing-doer impl)))))))