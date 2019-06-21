(ns unit.nedap.utils.speced.doc
  (:require
   #?(:clj [clojure.repl :as repl])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.utils.spec.api #?(:clj :refer :cljs :refer-macros) [check!]]
   [nedap.utils.speced :as sut])
  #?(:cljs (:require-macros [cljs.repl :as repl])))

(sut/def-with-doc ::email "An email" string?)

(deftest doc
  (testing "The def-with-doc docstring appears"
    (is (= (str "-------------------------\n"
                ":unit.nedap.utils.speced.doc/email\n"
                "Spec\n"
                "  string?\n\n"
                ;; the docstring:
                "An email\n")
           (with-out-str
             (sut/doc ::email)))))

  (testing "Other than that def-with-doc behavior, `doc` will print the same exact contents than its `#'repl/doc` counterpart"
    (are [x] (= (with-out-str
                  (repl/doc x))
                (with-out-str
                  (sut/doc x)))
      +
      try
      clojure.core)))
