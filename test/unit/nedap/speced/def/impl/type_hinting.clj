(ns unit.nedap.speced.def.impl.type-hinting
  (:require
   [clojure.test :refer [are deftest testing]]
   [nedap.speced.def :as speced]
   [nedap.speced.def.impl.type-hinting :as sut]
   [nedap.utils.test.api :refer [meta=]])
  (:import
   (clojure.lang IMeta)))

(speced/defn assert-type-hint-conversion [^boolean? clj?, ^IMeta input, ^IMeta expected]
  (binding [*print-meta* true]
    (testing (pr-str input)
      (let [result (sut/ensure-proper-type-hints clj? input)]
        (assert (= (pr-str expected)
                   (pr-str result))
                "Sanity check for avoiding false positives")
        (meta= expected result)))))

(deftest ensure-proper-type-hints
  ;; NOTE: in the clj variant, there no `string?` -> `String` conversion, b/c it's done in a different phase
  (testing "clj"
    (are [input expected] (assert-type-hint-conversion true input expected)

      ^{:tag 'String} [(with-meta 'x {:tag 'String})]
      ^{:tag 'String} [(with-meta 'x {:tag 'String})]

      ^{:tag :not-a-hint} [(with-meta 'x {:tag 'String})]
      [(with-meta 'x {:tag 'String})]))

  (testing "cljs"
    (are [input expected] (assert-type-hint-conversion false input expected)

      ^{:tag 'js/String} [(with-meta 'x {:tag 'js/String})]
      ^{:tag 'string}    [(with-meta 'x {:tag 'string})]

      ^{:tag 'string}    [(with-meta 'x {:tag 'string})]
      ^{:tag 'string}    [(with-meta 'x {:tag 'string})]

      ^{:tag 'strAng}    [(with-meta 'x {:tag 'string})]
      [(with-meta 'x {:tag 'string})])))
