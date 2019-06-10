(ns unit.nedap.utils.spec.doc
  (:require
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.utils.spec.api #?(:clj :refer :cljs :refer-macros) [check!]]
   [nedap.utils.spec.doc :as sut]
   [nedap.utils.speced :as speced]))

(speced/def-with-doc ::email "An email" string?)

(deftest doc-registry
  (is (= "An email"
         (get @sut/doc-registry ::email))))

(deftest doc-registry-as-symbols
  (is (= "An email"
         (-> @sut/doc-registry-as-symbols
             (get `email)
             :docstring))))
