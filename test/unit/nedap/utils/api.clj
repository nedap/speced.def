(ns unit.nedap.utils.api
  (:require
   [clojure.test :refer :all]
   [nedap.utils.spec.api :as sut]))

(deftest check!
  (let [f (fn [x y]
            {:pre [(sut/check! int? x
                               boolean? y)]
             :post [(sut/check! string? %)]}
            (when y
              (str x)))]
    (is (f 42 true))
    (testing ":post"
      (is (thrown? Exception (with-out-str
                               (f 42 false)))))
    (testing ":pre"
      (is (thrown? Exception (with-out-str
                               (f :not-an-int true))))
      (is (thrown? Exception (with-out-str
                               (f 42 :not-a-boolean)))))))
