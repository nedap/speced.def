(ns unit.nedap.utils.speced
  (:refer-clojure :exclude [defprotocol])
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer :all]
   [nedap.utils.spec.api :refer [check!]]
   [nedap.utils.speced :as speced]))

(spec/def ::int int?)

(spec/def ::age ::int)

(spec/def ::x boolean?)

(spec/def ::this (spec/keys :req-un [::age]))

(speced/defprotocol ExampleProtocol
  "Docstring"
  (^::int
    do-it [^::this this
           ^::x boolean]
    "Docstring"))

(defrecord Sut [age]
  ExampleProtocol
  (--do-it [this x]
    (if x
      42
      :fail)))

(deftest defprotocol
  (is (= 42 (do-it (->Sut 42) true)))
  (is (thrown? Exception (with-out-str
                           (-> (->Sut 42) (do-it :not-a-boolean)))))
  (is (thrown? Exception (with-out-str
                           (-> (->Sut 42) (do-it false))))
      "`false` will cause the method not to return an int")
  (is (thrown? Exception (with-out-str
                           (-> (->Sut :not-an-int) (do-it true))))))

(speced/def-with-doc ::email "An email" string?)

(deftest def-with-doc
  (is (check! ::email "a@a.a"))
  (is (thrown? Exception (with-out-str
                           (check! ::email :not-an-email)))))
