(ns unit.nedap.utils.speced.defprotocol.inline-function-specs
  "This ns exercises the 'inline function specs' format of `#'nedap.utils.speced/defprotocol`."
  (:refer-clojure :exclude [defprotocol])
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer :all]
   [nedap.utils.speced :as speced]))

(spec/def ::int int?)

(spec/def ::age ::int)

(spec/def ::this (spec/keys :req-un [::age]))

(defn this? [x]
  (spec/valid? ::this x))

(speced/defprotocol ExampleProtocol
  "Docstring"
  (^int?
    do-it [^this?
           this
           ^boolean?
           boolean]
    "Docstring"))

(defrecord Sut [age]
  ExampleProtocol
  (--do-it [this x]
    (if (true? x)
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