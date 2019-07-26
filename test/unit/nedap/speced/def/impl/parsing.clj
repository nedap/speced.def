(ns unit.nedap.speced.def.impl.parsing
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer [are deftest testing]]
   [nedap.speced.def :as speced]
   [nedap.speced.def.impl.parsing :as sut]))

(spec/def ::number number?)

(deftest extract-specs-from-metadata

  (testing "clj"
    (are [input expected] (= expected
                             (sut/extract-specs-from-metadata input true))

      {:tag Number}           (list {:spec            (list 'fn ['x]
                                                            (list `instance? 'java.lang.Number 'x))
                                     :type-annotation java.lang.Number})

      {:tag             Number

       ::speced/nilable true} (list {:spec            (list 'clojure.spec.alpha/nilable
                                                            (list 'fn ['x]
                                                                  (list `instance? 'java.lang.Number 'x)))
                                     :type-annotation java.lang.Number})

      {:tag ::number}         (list {:spec            ::number
                                     :type-annotation nil})

      {:tag             ::number
       ::speced/nilable true} (list {:spec            (list 'clojure.spec.alpha/nilable ::number)
                                     :type-annotation nil})

      {:tag `number?}         (list {:spec
                                     (list 'clojure.spec.alpha/and
                                           `number?
                                           (list 'fn ['x]
                                                 (list `instance? 'java.lang.Number 'x)))
                                     :type-annotation java.lang.Number
                                     :was-primitive?  false})

      {:tag             `number?
       ::speced/nilable true} (list {:spec
                                     (list 'clojure.spec.alpha/nilable
                                           (list 'clojure.spec.alpha/and
                                                 `number?
                                                 (list 'fn ['x]
                                                       (list `instance? 'java.lang.Number 'x))))
                                     :type-annotation java.lang.Number
                                     :was-primitive?  false})))

  (testing "cljs"
    (are [input expected] (= expected
                             (sut/extract-specs-from-metadata input false))

      {:tag 'js/Number}       '({:spec            (cljs.spec.alpha/or
                                                   :class-instance
                                                   (fn [x]
                                                     (cljs.core/= js/Number (cljs.core/type x)))
                                                   :protocol-instance
                                                   (fn [x]
                                                     (cljs.core/satisfies? js/Number x)))
                                 :type-annotation js/Number})

      {:tag             'js/Number
       ::speced/nilable true} '({:spec            (cljs.spec.alpha/nilable
                                                   (cljs.spec.alpha/or
                                                    :class-instance
                                                    (fn [x]
                                                      (cljs.core/= js/Number (cljs.core/type x)))
                                                    :protocol-instance
                                                    (fn [x]
                                                      (cljs.core/satisfies? js/Number x))))
                                 :type-annotation js/Number})

      {:tag ::number}         (list {:spec            ::number
                                     :type-annotation nil})

      {:tag             ::number
       ::speced/nilable true} '({:spec            (cljs.spec.alpha/nilable
                                                   ::number)
                                 :type-annotation nil})

      {:tag 'number}          '({:spec            cljs.core/number?
                                 :type-annotation number
                                 :was-primitive?  true})

      {:tag             'number
       ::speced/nilable true} '({:spec            (cljs.spec.alpha/nilable cljs.core/number?)
                                 :type-annotation number
                                 :was-primitive?  true}))))

(deftest infer-spec-from-symbol

  (testing "clj"
    (are [input expected] (= expected
                             (sut/infer-spec-from-symbol true input))
      'clojure.core/string? {:spec            '(clojure.spec.alpha/and clojure.core/string?
                                                                       (fn [x] (clojure.core/instance? java.lang.String x))),
                             :type-annotation java.lang.String,
                             :was-primitive?  false}
      'string               nil))

  (testing "cljs"
    (are [input expected] (= expected
                             (sut/infer-spec-from-symbol false input))
      'cljs.core/string? '{:spec            cljs.core/string?,
                           :type-annotation string,
                           :was-primitive?  false}
      'string            '{:spec            cljs.core/string?,
                           :type-annotation string,
                           :was-primitive?  true})))
