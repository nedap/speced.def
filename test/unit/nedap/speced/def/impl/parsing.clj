(ns unit.nedap.speced.def.impl.parsing
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.string :as string]
   [clojure.test :refer :all]
   [nedap.speced.def :as speced]
   [nedap.speced.def.impl.parsing :as sut]))

(deftest proper-spec-metadata?
  (with-out-str
    (testing "clj"
      (are [metadata-map extracted-specs expected] (= expected
                                                      (sut/proper-spec-metadata? true metadata-map extracted-specs))
        {:tag String} (list {:type-annotation String}) true))

    (testing "cljs"
      (are [metadata-map extracted-specs expected] (try
                                                     (= expected
                                                        (sut/proper-spec-metadata? false metadata-map extracted-specs))
                                                     (catch clojure.lang.ExceptionInfo e
                                                       (is (-> e .getMessage (string/starts-with? "Validation failed")))
                                                       (not expected)))
        {:tag 'js/String} (list {:type-annotation 'string})    true
        {:tag 'js/String} (list {:type-annotation 'js/String}) false))))

(spec/def ::number number?)

(deftest extract-specs-from-metadata
  (testing "clj"
    (are [input expected] (= expected
                             (sut/extract-specs-from-metadata input true))

      {:tag Number}           (list {:spec            (list 'fn ['x]
                                                            '(if (clojure.core/class? java.lang.Number)
                                                               (clojure.core/instance? java.lang.Number x)
                                                               (clojure.core/satisfies? java.lang.Number x)))

                                     :type-annotation java.lang.Number})

      {:tag             Number

       ::speced/nilable true} (list {:spec            (list 'clojure.spec.alpha/nilable
                                                            (list 'fn ['x]
                                                                  '(if (clojure.core/class? java.lang.Number)
                                                                     (clojure.core/instance? java.lang.Number x)
                                                                     (clojure.core/satisfies? java.lang.Number x))))
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
                                                 '(if (clojure.core/class? java.lang.Number)
                                                    (clojure.core/instance? java.lang.Number x)
                                                    (clojure.core/satisfies? java.lang.Number x))))
                                     :type-annotation java.lang.Number
                                     :was-primitive?  false})

      {:tag             `number?
       ::speced/nilable true} (list {:spec
                                     (list 'clojure.spec.alpha/nilable
                                           (list 'clojure.spec.alpha/and
                                                 `number?
                                                 (list 'fn ['x]
                                                       '(if (clojure.core/class? java.lang.Number)
                                                          (clojure.core/instance? java.lang.Number x)
                                                          (clojure.core/satisfies? java.lang.Number x)))))
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
                                 :type-annotation number})

      {:tag             'js/Number
       ::speced/nilable true} '({:spec            (cljs.spec.alpha/nilable
                                                   (cljs.spec.alpha/or
                                                    :class-instance
                                                    (fn [x]
                                                      (cljs.core/= js/Number (cljs.core/type x)))
                                                    :protocol-instance
                                                    (fn [x]
                                                      (cljs.core/satisfies? js/Number x))))
                                 :type-annotation number})

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
                                                                       (fn [x]
                                                                         (if (clojure.core/class? java.lang.String)
                                                                           (clojure.core/instance? java.lang.String x)
                                                                           (clojure.core/satisfies? java.lang.String x)))),
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
