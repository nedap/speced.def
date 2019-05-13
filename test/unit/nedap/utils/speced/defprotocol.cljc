(ns unit.nedap.utils.speced.defprotocol
  (:refer-clojure :exclude [defprotocol])
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
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
   "Docstring")

  (^::int
   do-other-thing [^::this this
                   ^::x boolean]
   "Docstring"))

(speced/defprotocol UnspecifiedRetValProtocol
  "A protocol having a method with non-speced return value"
  (do-it-unspeced-ret [^::this this
                       ^::x boolean]
    "Docstring")

  (do-other-thing-unspeced-ret [^::this this
                                ^::x boolean]
    "Docstring"))

(defrecord Sut [age]
  ExampleProtocol
  (--do-it [this x]
    (if x
      42
      :fail))

  (--do-other-thing [this x]
    (if x
      42
      :fail))

  UnspecifiedRetValProtocol
  (--do-it-unspeced-ret [this x]
    (if x
      42
      :fail))

  (--do-other-thing-unspeced-ret [this x]
    (if x
      42
      :fail)))

(def validation-failed #"Validation failed")

(deftest defprotocol

  (testing "Return values are computed"
    (is (= 42 (do-it (->Sut 42) true)))
    (is (= 42 (do-it-unspeced-ret (->Sut 42) true))))

  (testing "Argument validation"
    (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                                (-> (->Sut 42) (do-it :not-a-boolean)))))
    (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                                (-> (->Sut 42) (do-it-unspeced-ret :not-a-boolean))))))

  (testing "Return value validation"
    (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                                (-> (->Sut 42) (do-it false))))
        "`false` will cause the method not to return an int")
    (is (with-out-str
          (-> (->Sut 42) (do-it-unspeced-ret false)))
        "Unspecified ret val allows the method to succeed"))

  (testing "Validation of the object that implements the protocol"
    (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                                (-> (->Sut :not-an-int) (do-it true)))))
    (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                                (-> (->Sut :not-an-int) (do-it-unspeced-ret true)))))))

(deftest multiple-methods
  (testing "Protocols can have more than one method, compiling fine and emitting valid executable code"

    (testing "Return values are computed"
      (is (= 42 (do-other-thing (->Sut 42) true)))
      (is (= 42 (do-other-thing-unspeced-ret (->Sut 42) true))))

    (testing "Argument validation"
      (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                                  (-> (->Sut 42) (do-other-thing :not-a-boolean)))))
      (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                                  (-> (->Sut 42) (do-other-thing-unspeced-ret :not-a-boolean))))))

    (testing "Return value validation"
      (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                                  (-> (->Sut 42) (do-other-thing false))))
          "`false` will cause the method not to return an int")
      (is (with-out-str
            (-> (->Sut 42) (do-other-thing-unspeced-ret false)))
          "Unspecified ret val allows the method to succeed"))

    (testing "Validation of the object that implements the protocol"
      (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                                  (-> (->Sut :not-an-int) (do-other-thing true)))))
      (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                                  (-> (->Sut :not-an-int) (do-other-thing-unspeced-ret true))))))))
