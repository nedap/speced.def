(ns unit.nedap.speced.def.doc
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:clj [clojure.repl :as repl])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.speced.def :as speced]
   [nedap.speced.def.doc :as sut]
   [nedap.utils.spec.api #?(:clj :refer :cljs :refer-macros) [check!]])
  #?(:cljs (:require-macros [cljs.repl :as repl])))

(speced/def-with-doc ::email "An email" string?)

(deftest doc
  (testing "The def-with-doc docstring appears"
    (is (= (str "-------------------------\n"
                ":unit.nedap.speced.def.doc/email\n"
                "Spec\n"
                "  string?\n\n"
                ;; the docstring:
                "An email\n")
           (with-out-str
             (speced/doc ::email)))))

  (testing "Other than that def-with-doc behavior, `doc` will print the same exact contents than its `#'repl/doc` counterpart"
    (are [x] (= (with-out-str
                  (repl/doc x))
                (with-out-str
                  (speced/doc x)))
      +
      try
      clojure.core)))

(deftest doc-registry
  (is (= "An email"
         (get @sut/doc-registry ::email))))

(deftest rebl-doc-registry
  (is (= "An email"
         (-> @sut/rebl-doc-registry
             (get `email)
             :docstring))))
