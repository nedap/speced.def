(ns unit.nedap.speced.def.impl.defn
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer [are deftest is testing]]
   [nedap.speced.def.impl.defn :as sut]
   [nedap.utils.test.api :refer [meta=]]))

(spec/def ::string string?)

(deftest extract-specs-from-destructurings
  (testing "It omits arguments that are plain symbols, and observes destructured ones"
    (testing "clj"
      (are [input expected] (= expected
                               (sut/extract-specs-from-destructurings true input))
        []                                       []
        '[a]                                     []
        '[a & [b]]                               []
        '[a & b]                                 []
        '[[a ^::string b c]]                     [{:spec            :unit.nedap.speced.def.impl.defn/string,
                                                   :type-annotation nil
                                                   :arg             'b}]
        '[a & [^String b]]                       [{:spec            '(fn [x]
                                                                       (clojure.core/instance? String x)),
                                                   :type-annotation java.lang.String
                                                   :arg             'b}]
        '[a {:keys [^string? b]}]                [{:spec            (list 'clojure.spec.alpha/and 'string?
                                                                          (list 'fn ['x]
                                                                                (list 'clojure.core/instance?
                                                                                      'java.lang.String
                                                                                      'x))),
                                                   :type-annotation java.lang.String,
                                                   :was-primitive?  false
                                                   :arg             'b}]
        '[a & [{{{[e ^string? f g] :d} :c} :b}]] [{:spec            (list 'clojure.spec.alpha/and 'string?
                                                                          (list 'fn ['x]
                                                                                (list 'clojure.core/instance?
                                                                                      'java.lang.String
                                                                                      'x))),
                                                   :type-annotation java.lang.String,
                                                   :was-primitive?  false
                                                   :arg             'f}]))

    (testing "cljs"
      (are [input expected] (= expected
                               (sut/extract-specs-from-destructurings false input))
        []                                       []
        '[a]                                     []
        '[a & [b]]                               []
        '[a & b]                                 []
        '[[a ^::string b c]]                     [{:spec            :unit.nedap.speced.def.impl.defn/string,
                                                   :type-annotation nil
                                                   :arg             'b}]
        '[a & [^string b]]                       '[{:spec            cljs.core/string?,
                                                    :type-annotation string,
                                                    :was-primitive?  true
                                                    :arg             b}]
        '[a {:keys [^string? b]}]                [{:spec            'cljs.core/string?,
                                                   :type-annotation 'string,
                                                   :was-primitive?  false
                                                   :arg             'b}]
        '[a & [{{{[e ^string? f g] :d} :c} :b}]] [{:spec            'cljs.core/string?,
                                                   :type-annotation 'string,
                                                   :was-primitive?  false
                                                   :arg             'f}]))))

(deftest maybe-type-hint-destructured-args
  (testing "clj"
    (are [non-destructured-args all-args expected] (let [result (sut/maybe-type-hint-destructured-args true
                                                                                                       non-destructured-args
                                                                                                       all-args)]
                                                     (is (meta= expected result)
                                                         (binding [*print-meta* true]
                                                           (pr-str result))))

      #{}   1                                          1

      #{}   []                                         []

      #{'a} [(with-meta 'a {:tag `string?})]           [(with-meta 'a {:tag `string?})]

      #{}   [(with-meta 'a {:tag `string?})]           [(with-meta 'a {:tag String})]

      #{'a} [{:keys [(with-meta 'a {:tag `string?})]}] [{:keys [(with-meta 'a {:tag `string?})]}]

      #{}   [{:keys [(with-meta 'a {:tag `string?})]}] [{:keys [(with-meta 'a {:tag String})]}]))

  (testing "cljs"
    (are [non-destructured-args all-args expected] (let [result (sut/maybe-type-hint-destructured-args false
                                                                                                       non-destructured-args
                                                                                                       all-args)]
                                                     (is (meta= expected result)
                                                         (binding [*print-meta* true]
                                                           (pr-str result))))

      #{}   1                                                    1

      #{}   []                                                   []

      #{'a} [(with-meta 'a {:tag 'cljs.core/string?})]           [(with-meta 'a {:tag 'cljs.core/string?})]

      #{}   [(with-meta 'a {:tag 'cljs.core/string?})]           [(with-meta 'a {:tag 'string})]

      #{'a} [{:keys [(with-meta 'a {:tag 'cljs.core/string?})]}] [{:keys [(with-meta 'a {:tag 'cljs.core/string?})]}]

      #{}   [{:keys [(with-meta 'a {:tag 'cljs.core/string?})]}] [{:keys [(with-meta 'a {:tag 'string})]}])))
