(ns unit.nedap.utils.spec.impl.parsing
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer [are deftest testing]]
   [nedap.utils.spec.impl.parsing :as sut]
   [nedap.utils.speced :as speced]))

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
