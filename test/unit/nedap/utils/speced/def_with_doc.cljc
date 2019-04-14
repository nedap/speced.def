(ns unit.nedap.utils.speced.def-with-doc
  (:require
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.utils.spec.api #?(:clj :refer :cljs :refer-macros) [check!]]
   [nedap.utils.speced :as speced]))

(speced/def-with-doc ::email "An email" string?)

(deftest def-with-doc
  (is (check! ::email "a@a.a"))
  (is (thrown? #?(:clj Exception :cljs js/Error) (with-out-str
                                                   (check! ::email :not-an-email)))))
