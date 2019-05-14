(ns unit.nedap.utils.speced.defn
  "NOTE: all these demonstrate the following aspect documented in `:nedap.utils.spec.specs/spec-metadata`:

  > A defn with return value metadata for both its name and argument vector will emit spec checking for both.

  In practice, that is completely optional and you are free to use 2, 1, or 0 return value hints in any position of your choice."
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   [clojure.string :as string]
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.utils.speced :as sut]
   [unit.nedap.test-helpers :refer [every-and-at-least-one?]]))

(do
  #?@(:clj [(spec/def ::age pos?)

            (spec/def ::temperature double?)

            (spec/def ::name (spec/and string? (fn [x]
                                                 (-> x count (< 10)))))

            (defn present? [x]
              some?)

            (spec/def ::present? present?)

            (doseq [[k v] {:no-metadata                '(sut/defn no-metadata [x]
                                                          (-> x (* x) str))

                           :no-metadata-n              '(sut/defn no-metadata-n
                                                          ([x]
                                                           (-> x (* x) str))

                                                          ([x y]
                                                           (-> x (* y) str)))

                           :concise-metadata           '(sut/defn ^::present?
                                                          concise-metadata
                                                          ^::name
                                                          [^::age x]
                                                          (-> x (* x) str))

                           :concise-metadata-n         '(sut/defn ^::present?
                                                          concise-metadata-n
                                                          (^::name
                                                           [^::age x]
                                                           (-> x (* x) str))

                                                          (^::name
                                                           [^::age x, ^::temperature y]
                                                           (-> x (* y) str)))

                           :explicit-metadata          '(sut/defn ^{::sut/spec ::present?}
                                                          explicit-metadata
                                                          ^{::sut/spec ::name}
                                                          [^{::sut/spec ::age} x]
                                                          (-> x (* x) str))

                           :explicit-metadata-n        '(sut/defn ^{::sut/spec ::present?}
                                                          explicit-metadata-n
                                                          (^{::sut/spec ::name}
                                                           [^{::sut/spec ::age} x]
                                                           (-> x (* x) str))

                                                          (^{::sut/spec ::name}
                                                           [^{::sut/spec ::age} x
                                                            ^{::sut/spec ::temperature} y]
                                                           (-> x (* y) str)))

                           :type-hinted-metadata       '(sut/defn
                                                          type-hinted-metadata
                                                          ^String
                                                          [^Double x]
                                                          (when (< 0 x 100)
                                                            (-> x (* x) str)))

                           :type-hinted-metadata-alt   '(sut/defn
                                                          ^String
                                                          type-hinted-metadata-alt
                                                          [^Double x]
                                                          (when (< 0 x 100)
                                                            (-> x (* x) str)))

                           :type-hinted-metadata-n     '(sut/defn
                                                          type-hinted-metadata-n
                                                          (^String [^Double x]
                                                           (when (< 0 x 100)
                                                             (-> x (* x) str)))

                                                          (^String [^Double x, ^Double y]
                                                           (when (< 0 x 100)
                                                             (-> x (* y) str))))

                           :type-hinted-metadata-n-alt '(sut/defn
                                                          ^String type-hinted-metadata-n-alt
                                                          ([^Double x]
                                                           (when (< 0 x 100)
                                                             (-> x (* x) str)))

                                                          ([^Double x, ^Double y]
                                                           (when (< 0 x 100)
                                                             (-> x (* y) str))))

                           :inline-function            '(sut/defn ^present?
                                                          inline-function
                                                          ^string?
                                                          [^double? x]
                                                          (when (< 0 x 100)
                                                            (-> x (* x) str)))

                           :inline-function-n          '(sut/defn ^present?
                                                          inline-function-n
                                                          (^string? [^double? x]
                                                           (when (< 0 x 100)
                                                             (-> x (* x) str)))

                                                          (^string? [^double? x, ^double? y]
                                                           (when (< 0 x 100)
                                                             (-> x (* y) str))))

                           :inline-function-alt        '(sut/defn ^string?
                                                          inline-function-alt
                                                          [^double? x]
                                                          (when (< 0 x 100)
                                                            (-> x (* x) str)))

                           :inline-function-n-alt      '(sut/defn ^string?
                                                          inline-function-n-alt
                                                          ([^double? x]
                                                           (when (< 0 x 100)
                                                             (-> x (* x) str)))

                                                          ([^double? x, ^double? y]
                                                           (when (< 0 x 100)
                                                             (-> x (* y) str))))

                           :primitive                  '(sut/defn ^string? primitive
                                                          [^double x]
                                                          (when (< 0 x 100)
                                                            (-> x (* x) str)))
                           :primitive-n                '(sut/defn ^string? primitive-n
                                                          ([^double x]
                                                           (when (< 0 x 100)
                                                             (-> x (* x) str)))

                                                          ([^double x, ^double y]
                                                           (when (< 0 x 100)
                                                             (-> x (* y) str))))}]
              (eval v)
              (eval `(def ~(-> k
                               name
                               (str "-macroexpansion")
                               symbol)
                       ~(list 'quote (macroexpand v)))))

            (deftest macroexpansion
              (testing "It macroexpands to known-good (and evidently-good) forms"
                (are [x y] (= x y)
                  no-metadata-macroexpansion            '(def no-metadata (clojure.core/fn ([x]
                                                                                            {:pre [], :post []}
                                                                                            (-> x (* x) str))))
                  no-metadata-n-macroexpansion          '(def no-metadata-n
                                                           (clojure.core/fn
                                                             ([x]
                                                              {:pre [], :post []}
                                                              (-> x (* x) str))

                                                             ([x y]
                                                              {:pre [], :post []}
                                                              (-> x (* y) str))))

                  concise-metadata-macroexpansion       '(def concise-metadata
                                                           (clojure.core/fn
                                                             ([x]
                                                              {:pre
                                                               [(nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/age x)],
                                                               :post
                                                               [(nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/present? %)
                                                                (nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/name %)]}
                                                              (-> x (* x) str))))

                  concise-metadata-n-macroexpansion     '(def concise-metadata-n
                                                           (clojure.core/fn
                                                             ([x]
                                                              {:pre
                                                               [(nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/age x)],
                                                               :post
                                                               [(nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/present? %)
                                                                (nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/name  %)]}
                                                              (-> x (* x) str))

                                                             ([x y]
                                                              {:pre
                                                               [(nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/age x
                                                                                             :unit.nedap.utils.speced.defn/temperature y)],
                                                               :post
                                                               [(nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/present? %)
                                                                (nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/name  %)]}
                                                              (-> x (* y) str))))

                  explicit-metadata-macroexpansion      '(def explicit-metadata
                                                           (clojure.core/fn
                                                             ([x]
                                                              {:pre
                                                               [(nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/age x)],
                                                               :post
                                                               [(nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/present? %)
                                                                (nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/name %)]}
                                                              (-> x (* x) str))))

                  explicit-metadata-n-macroexpansion    '(def explicit-metadata-n
                                                           (clojure.core/fn
                                                             ([x]
                                                              {:pre
                                                               [(nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/age x)],
                                                               :post
                                                               [(nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/present? %)
                                                                (nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/name  %)]}
                                                              (-> x (* x) str))

                                                             ([x y]
                                                              {:pre
                                                               [(nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/age x
                                                                                             :unit.nedap.utils.speced.defn/temperature y)],
                                                               :post
                                                               [(nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/present? %)
                                                                (nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/name  %)]}
                                                              (-> x (* y) str))))

                  type-hinted-metadata-macroexpansion   '(def type-hinted-metadata
                                                           (clojure.core/fn
                                                             ([x]
                                                              {:pre
                                                               [(nedap.utils.spec.api/check! (fn [x] (clojure.core/instance? Double x)) x)],
                                                               :post
                                                               [(nedap.utils.spec.api/check! (fn [x] (clojure.core/instance? String x)) %)]}
                                                              (when (< 0 x 100)
                                                                (-> x (* x) str)))))

                  type-hinted-metadata-n-macroexpansion '(def type-hinted-metadata-n
                                                           (clojure.core/fn
                                                             ([x]
                                                              {:pre
                                                               [(nedap.utils.spec.api/check!
                                                                 (fn [x] (clojure.core/instance? Double x))
                                                                 x)],
                                                               :post
                                                               [(nedap.utils.spec.api/check!
                                                                 (fn [x] (clojure.core/instance? String x))
                                                                 %)]}
                                                              (when (< 0 x 100)
                                                                (-> x (* x) str)))

                                                             ([x y]
                                                              {:pre
                                                               [(nedap.utils.spec.api/check!
                                                                 (fn [x] (clojure.core/instance? Double x))
                                                                 x
                                                                 (fn [x] (clojure.core/instance? Double x))
                                                                 y)],
                                                               :post
                                                               [(nedap.utils.spec.api/check!
                                                                 (fn [x] (clojure.core/instance? String x))
                                                                 %)]}
                                                              (when (< 0 x 100)
                                                                (-> x (* y) str)))))

                  inline-function-macroexpansion        '(def inline-function
                                                           (clojure.core/fn
                                                             ([x]
                                                              {:pre [(nedap.utils.spec.api/check!
                                                                      (clojure.spec.alpha/and
                                                                       double?
                                                                       (fn [x] (clojure.core/instance? java.lang.Double x)))
                                                                      x)],
                                                               :post
                                                               [(nedap.utils.spec.api/check! present? %)
                                                                (nedap.utils.spec.api/check!
                                                                 (clojure.spec.alpha/and
                                                                  string?
                                                                  (fn [x] (clojure.core/instance? java.lang.String x)))
                                                                 %)]}
                                                              (when (< 0 x 100)
                                                                (-> x (* x) str)))))
                  inline-function-n-macroexpansion      '(def
                                                           inline-function-n
                                                           (clojure.core/fn
                                                             ([x]
                                                              {:pre [(nedap.utils.spec.api/check!
                                                                      (clojure.spec.alpha/and
                                                                       double?
                                                                       (fn [x] (clojure.core/instance? java.lang.Double x)))
                                                                      x)],
                                                               :post
                                                               [(nedap.utils.spec.api/check! present? %)
                                                                (nedap.utils.spec.api/check!
                                                                 (clojure.spec.alpha/and
                                                                  string?
                                                                  (fn [x] (clojure.core/instance? java.lang.String x)))
                                                                 %)]}
                                                              (when (< 0 x 100)
                                                                (-> x (* x) str)))

                                                             ([x y]
                                                              {:pre [(nedap.utils.spec.api/check!
                                                                      (clojure.spec.alpha/and
                                                                       double?
                                                                       (fn [x] (clojure.core/instance? java.lang.Double x)))
                                                                      x
                                                                      (clojure.spec.alpha/and
                                                                       double?
                                                                       (fn [x] (clojure.core/instance? java.lang.Double x)))
                                                                      y)],
                                                               :post
                                                               [(nedap.utils.spec.api/check! present? %)
                                                                (nedap.utils.spec.api/check!
                                                                 (clojure.spec.alpha/and
                                                                  string?
                                                                  (fn [x] (clojure.core/instance? java.lang.String x)))
                                                                 %)]}
                                                              (when (< 0 x 100)
                                                                (-> x (* y) str)))))

                  primitive-macroexpansion              '(def
                                                           primitive
                                                           (clojure.core/fn
                                                             ([x]
                                                              {:pre
                                                               [(nedap.utils.spec.api/check!
                                                                 (fn [x] (clojure.core/instance? java.lang.Double x))
                                                                 x)],
                                                               :post
                                                               [(nedap.utils.spec.api/check!
                                                                 (clojure.spec.alpha/and string?
                                                                                         (fn [x]
                                                                                           (clojure.core/instance? java.lang.String x)))
                                                                 %)]}
                                                              (when (< 0 x 100)
                                                                (-> x (* x) str)))))
                  primitive-n-macroexpansion            '(def
                                                           primitive-n
                                                           (clojure.core/fn
                                                             ([x]
                                                              {:pre
                                                               [(nedap.utils.spec.api/check!
                                                                 (fn [x]
                                                                   (clojure.core/instance? java.lang.Double x))
                                                                 x)],
                                                               :post
                                                               [(nedap.utils.spec.api/check!
                                                                 (clojure.spec.alpha/and string?
                                                                                         (fn [x]
                                                                                           (clojure.core/instance? java.lang.String x)))
                                                                 %)]}
                                                              (when (< 0 x 100)
                                                                (-> x (* x) str)))
                                                             ([x y]
                                                              {:pre
                                                               [(nedap.utils.spec.api/check!
                                                                 (fn [x]
                                                                   (clojure.core/instance? java.lang.Double x))
                                                                 x
                                                                 (fn [x]
                                                                   (clojure.core/instance? java.lang.Double x))
                                                                 y)],
                                                               :post
                                                               [(nedap.utils.spec.api/check!
                                                                 (clojure.spec.alpha/and string?
                                                                                         (fn [x]
                                                                                           (clojure.core/instance? java.lang.String x)))
                                                                 %)]}
                                                              (when (< 0 x 100)
                                                                (-> x (* y) str))))))))

            (deftest correct-execution
              (testing "Arity 1"
                (are [f] (= "64.0" (f 8.0))
                  no-metadata
                  no-metadata-n
                  concise-metadata
                  concise-metadata-n
                  explicit-metadata
                  explicit-metadata-n
                  type-hinted-metadata
                  type-hinted-metadata-n
                  type-hinted-metadata-alt
                  type-hinted-metadata-n-alt
                  inline-function
                  inline-function-n
                  inline-function-alt
                  inline-function-n-alt
                  primitive
                  primitive-n))

              (testing "Arity 2"
                (are [f] (= "16.0" (f 8.0 2.0))
                  no-metadata-n
                  concise-metadata-n
                  explicit-metadata-n
                  type-hinted-metadata-n
                  type-hinted-metadata-n-alt
                  inline-function-n
                  inline-function-n-alt
                  primitive-n)))

            (deftest preconditions-are-checked

              (testing "Arity 1"
                (with-out-str
                  (let [arg 0]
                    (are [expectation f] (testing [f expectation]
                                           (case expectation
                                             :not-thrown (= "0" (f arg))
                                             :thrown (try
                                                       (f arg)
                                                       false
                                                       (catch #?(:clj Exception :cljs js/Error) e
                                                         (-> e ex-data :spec)))))
                      :not-thrown no-metadata
                      :not-thrown no-metadata-n
                      :thrown     concise-metadata
                      :thrown     concise-metadata-n
                      :thrown     explicit-metadata
                      :thrown     explicit-metadata-n
                      :thrown     type-hinted-metadata
                      :thrown     type-hinted-metadata-n
                      :thrown     type-hinted-metadata-alt
                      :thrown     type-hinted-metadata-n-alt
                      :thrown     inline-function
                      :thrown     inline-function-n
                      :thrown     inline-function-alt
                      :thrown     inline-function-n-alt
                      :thrown     primitive
                      :thrown     primitive-n))))

              (testing "Arity 2"
                (with-out-str
                  (let [arg 0]
                    (are [expectation f] (testing [f expectation]
                                           (case expectation
                                             :not-thrown (= "0" (f arg))
                                             :thrown (try
                                                       (f arg 1)
                                                       false
                                                       (catch #?(:clj Exception :cljs js/Error) e
                                                         (-> e ex-data :spec)))))
                      :not-thrown no-metadata-n
                      :thrown     concise-metadata-n
                      :thrown     explicit-metadata-n
                      :thrown     type-hinted-metadata-n
                      :thrown     type-hinted-metadata-n-alt
                      :thrown     inline-function-n
                      :thrown     inline-function-n-alt
                      :thrown     primitive-n)))))

            (deftest postconditions-are-checked
              (testing "Arity 1"
                (with-out-str
                  (let [arg 99999]
                    (are [expectation f] (testing [f expectation]
                                           (case expectation
                                             :not-thrown (= "9999800001" (f arg))
                                             :thrown (try
                                                       (f arg)
                                                       false
                                                       (catch #?(:clj Exception :cljs js/Error) e
                                                         (-> e ex-data :spec)))))
                      :not-thrown no-metadata
                      :not-thrown no-metadata-n
                      :thrown     concise-metadata
                      :thrown     concise-metadata-n
                      :thrown     explicit-metadata
                      :thrown     explicit-metadata-n
                      :thrown     type-hinted-metadata
                      :thrown     type-hinted-metadata-n
                      :thrown     type-hinted-metadata-alt
                      :thrown     type-hinted-metadata-n-alt
                      :thrown     inline-function
                      :thrown     inline-function-n
                      :thrown     inline-function-alt
                      :thrown     inline-function-n-alt
                      :thrown     primitive
                      :thrown     primitive-n))))

              (testing "Arity 2"
                (with-out-str
                  (let [arg1 99999
                        arg2 100000]
                    (are [expectation f] (testing [f expectation]
                                           (case expectation
                                             :not-thrown (= "9999900000" (f arg1 arg2))
                                             :thrown (try
                                                       (f arg1 arg2)
                                                       false
                                                       (catch #?(:clj Exception :cljs js/Error) e
                                                         (-> e ex-data :spec)))))
                      :not-thrown no-metadata-n
                      :thrown     concise-metadata-n
                      :thrown     explicit-metadata-n
                      :thrown     type-hinted-metadata-n
                      :thrown     inline-function-n
                      :thrown     type-hinted-metadata-n-alt
                      :thrown     inline-function-n-alt
                      :thrown     primitive-n)))))

            (deftest type-hint-emission
              (testing "Type hints are preserved or emitted"

                (testing "Return value hinting for single-arity functions"
                  (are [v] (testing v
                             (-> v meta :tag #{String}))
                    #'type-hinted-metadata
                    #'type-hinted-metadata-n
                    #'type-hinted-metadata-alt
                    #'type-hinted-metadata-n-alt
                    #'inline-function
                    #'inline-function-n
                    #'inline-function-alt
                    #'inline-function-n-alt
                    #'primitive
                    #'primitive-n))

                (testing "Arglist hinting for single-arity functions"
                  (are [v] (testing v
                             (-> v meta :arglists first meta :tag #{String}))
                    #'type-hinted-metadata
                    #'type-hinted-metadata-alt
                    #'inline-function
                    #'inline-function-alt
                    #'primitive
                    #'primitive-n))

                (testing "Arglist hinting for single-arity functions"
                  (are [v] (testing v
                             (-> v meta :arglists ffirst meta :tag #{`Double}))
                    #'type-hinted-metadata
                    #'type-hinted-metadata-alt
                    #'inline-function
                    #'inline-function-alt))

                (testing "Primitive arglist hinting for single-arity functions"
                  (are [v] (testing v
                             (-> v meta :arglists ffirst meta :tag #{'double}))
                    #'primitive))

                (testing "Arglist hinting for multi-arity functions"
                  (are [v] (testing v
                             (->> v
                                  meta
                                  :arglists
                                  (map meta)
                                  (map :tag)
                                  (every-and-at-least-one? #{String})))
                    #'type-hinted-metadata-n
                    #'type-hinted-metadata-n-alt
                    #'inline-function-n
                    #'inline-function-n-alt
                    #'primitive
                    #'primitive-n))

                (testing "Arguments hinting for multi-arity functions"
                  (are [v] (testing v
                             (->> v
                                  meta
                                  :arglists
                                  (map (fn [arglist]
                                         (->> arglist
                                              (map meta)
                                              (map :tag)
                                              (every-and-at-least-one? #{`Double}))))
                                  (every-and-at-least-one? true?)))
                    #'type-hinted-metadata-n
                    #'type-hinted-metadata-n-alt
                    #'inline-function-n
                    #'inline-function-n-alt))

                (testing "Primitive arguments hinting for multi-arity functions"
                  (are [v] (testing v
                             (->> v
                                  meta
                                  :arglists
                                  (map (fn [arglist]
                                         (->> arglist
                                              (map meta)
                                              (map :tag)
                                              (every-and-at-least-one? #{'double}))))
                                  (every-and-at-least-one? true?)))
                    #'primitive-n))

                (testing ":tag metadata placed in wrong positions is guarded against"
                  (are [desc input] (testing desc
                                      (testing input
                                        (try
                                          (eval input)
                                          false
                                          (catch Exception e
                                            true))))

                    "Hinting an arity"
                    '(sut/defn faulty
                       ^String ([x]
                                (-> x (* x) str)))))

                (testing "Only long and double primitive annotations are supported, just like in clojure.core/defn"
                  (are [input good?] (testing input
                                       (try
                                         (eval input)
                                         good?
                                         (catch Exception e
                                           (not good?))))

                    '(nedap.utils.speced/defn primitive-sample [^int x])    false
                    '(nedap.utils.speced/defn primitive-sample ^int [x])    false

                    '(nedap.utils.speced/defn primitive-sample [^long x])   true
                    '(nedap.utils.speced/defn primitive-sample ^long [x])   true

                    '(nedap.utils.speced/defn primitive-sample [^double x]) true
                    '(nedap.utils.speced/defn primitive-sample ^double [x]) true

                    '(defn primitive-sample [^int x])                       false
                    '(defn primitive-sample ^int [x])                       false

                    '(defn primitive-sample [^long x])                      true
                    '(defn primitive-sample ^long [x])                      true

                    '(defn primitive-sample [^double x])                    true
                    '(defn primitive-sample ^double [x])                    true))

                (are [input expected-tag] (testing input
                                            (testing "If the user tags the defn name (as opposed to the arg vector) with a primitive hint"
                                              (eval input)
                                              (testing "The hint will be removed"
                                                (is (-> 'primitive-sample resolve meta :tag nil?)))
                                              (testing "The argument vector will receive the primitive hint instead"
                                                (is (-> 'primitive-sample resolve meta :arglists first meta :tag #{expected-tag})))))
                  '(nedap.utils.speced/defn ^long primitive-sample [x])   'long
                  '(nedap.utils.speced/defn ^double primitive-sample [x]) 'double)))]))
