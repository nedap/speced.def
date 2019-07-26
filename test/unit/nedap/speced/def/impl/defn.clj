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
                                                                       (if (clojure.core/class? String)
                                                                         (clojure.core/instance? String x)
                                                                         (clojure.core/satisfies? String x))),
                                                   :type-annotation java.lang.String
                                                   :arg             'b}]
        '[a {:keys [^string? b]}]                [{:spec            (list 'clojure.spec.alpha/and 'string?
                                                                          (list 'fn ['x]
                                                                                '(if (clojure.core/class? java.lang.String)
                                                                                   (clojure.core/instance? java.lang.String x)
                                                                                   (clojure.core/satisfies? java.lang.String x)))),
                                                   :type-annotation java.lang.String,
                                                   :was-primitive?  false
                                                   :arg             'b}]
        '[a & [{{{[e ^string? f g] :d} :c} :b}]] [{:spec            (list 'clojure.spec.alpha/and 'string?
                                                                          (list 'fn ['x]
                                                                                '(if (clojure.core/class? java.lang.String)
                                                                                   (clojure.core/instance? java.lang.String x)
                                                                                   (clojure.core/satisfies? java.lang.String x)))),
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

(deftest consistent-tagging?
  (testing "clj"
    (are [ann tails expected] (binding [sut/*clj?* true]
                                (= expected
                                   (sut/consistent-tagging? ann tails true)))
      String []                             true
      nil    []                             true
      String ['([])]                        true
      nil    ['([])]                        true
      String ['(^String [])]                true
      Long   ['(^Long [])]                  true
      nil    ['(^String [])]                true
      String ['(^Long [])]                  false
      String ['(^String []) '(^String [a])] true
      nil    ['(^String []) '(^String [a])] true
      nil    ['(^String []) '([a])]         true
      String ['(^String []) '(^Long [a])]   false))

  (testing "cljs"
    (are [ann tails expected] (binding [sut/*clj?* false]
                                (= expected
                                   (sut/consistent-tagging? ann tails false)))
      'string []                             true
      nil     []                             true
      'string ['([])]                        true
      nil     ['([])]                        true
      'string ['(^string [])]                true
      'long   ['(^string [])]                false
      nil     ['(^string [])]                true
      'string ['(^number [])]                false
      'string ['(^string []) '(^string [a])] true
      nil     ['(^string []) '(^string [a])] true
      nil     ['(^string []) '([a])]         true
      'string ['(^string []) '(^number [a])] false)))
