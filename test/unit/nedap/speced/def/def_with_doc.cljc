(ns unit.nedap.speced.def.def-with-doc
  (:require
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.speced.def :as sut]
   [nedap.utils.spec.api #?(:clj :refer :cljs :refer-macros) [check!]]))

(sut/def-with-doc ::email "An email" string?)

(def validation-failed #"Validation failed")

(deftest def-with-doc
  (is (check! ::email "a@a.a"))
  (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                              (check! ::email :not-an-email)))))
