(ns unit.nedap.utils.speced.def-with-doc
  (:require
   [clojure.test :refer :all]
   [nedap.utils.spec.api :refer [check!]]
   [nedap.utils.speced :as speced]))

(speced/def-with-doc ::email "An email" string?)

(deftest def-with-doc
  (is (check! ::email "a@a.a"))
  (is (thrown? Exception (with-out-str
                           (check! ::email :not-an-email)))))
