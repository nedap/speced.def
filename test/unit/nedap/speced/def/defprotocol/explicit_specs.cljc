(ns unit.nedap.speced.def.defprotocol.explicit-specs
  "This ns exercises the 'explicit specs' format of `#'nedap.speced.def/defprotocol`."
  (:refer-clojure :exclude [defprotocol])
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.speced.def :as speced]))

(spec/def ::int int?)

(spec/def ::age ::int)

(spec/def ::x boolean?)

(spec/def ::this (spec/keys :req-un [::age]))

(speced/defprotocol ExampleProtocol
  "Docstring"
  (^{::speced/spec ::int}
    do-it [^{::speced/spec ::this} this
           ^{::speced/spec ::x} boolean]
    "Docstring"))

(defrecord Sut [age]
  ExampleProtocol
  (--do-it [this x]
    (if (true? x)
      42
      :fail)))

(def validation-failed #"Validation failed")

(deftest defprotocol
  (is (= 42 (do-it (->Sut 42) true)))
  (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                              (-> (->Sut 42) (do-it :not-a-boolean)))))
  (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                              (-> (->Sut 42) (do-it false))))
      "`false` will cause the method not to return an int")
  (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                              (-> (->Sut :not-an-int) (do-it true))))))
