(ns unit.nedap.utils.speced.defn
  "NOTE: all these demonstrate the following aspect documented in `:nedap.utils.spec.specs/spec-metadata`:

  > A defn with return value metadata for both its name and argument vector will emit spec checking for both.

  In practice, that is completely optional and you are free to use 2, 1, or 0 return value hints in any position of your choice."
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [clojure.string :as string]
   [nedap.utils.spec.impl.parsing :as impl.parsing]
   [nedap.utils.speced :as sut]
   [unit.nedap.test-helpers :refer [every-and-at-least-one?]])
  #?(:cljs (:require-macros [unit.nedap.utils.speced.defn :refer [the-defns]])))

(spec/def ::age pos?)

(spec/def ::temperature #?(:clj  double?
                           :cljs number?))

(spec/def ::name (spec/and string? (fn [x]
                                     (-> x count (< 10)))))

(defn present? [x]
  (some? x))

(spec/def ::present? present?)

#?(:clj
   (defmacro the-defns []
     (let [clj? (-> &env :ns nil?)
           xs {:no-metadata                '(sut/defn no-metadata [x]
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

               :type-hinted-metadata       (if clj?
                                             '(sut/defn
                                                type-hinted-metadata
                                                ^String
                                                [^Double x]
                                                (when (< 0 x 100)
                                                  (-> x (* x) str)))

                                             '(sut/defn
                                                type-hinted-metadata
                                                ^js/String
                                                [^js/Number x]
                                                (when (< 0 x 100)
                                                  (-> x (* x) str))))

               :type-hinted-metadata-alt   (if clj?
                                             '(sut/defn
                                                ^String
                                                type-hinted-metadata-alt
                                                [^Double x]
                                                (when (< 0 x 100)
                                                  (-> x (* x) str)))

                                             '(sut/defn
                                                ^js/String
                                                type-hinted-metadata-alt
                                                [^js/Number x]
                                                (when (< 0 x 100)
                                                  (-> x (* x) str))))

               :type-hinted-metadata-n     (if clj?
                                             '(sut/defn
                                                type-hinted-metadata-n
                                                (^String [^Double x]
                                                 (when (< 0 x 100)
                                                   (-> x (* x) str)))

                                                (^String [^Double x, ^Double y]
                                                 (when (< 0 x 100)
                                                   (-> x (* y) str))))

                                             '(sut/defn
                                                type-hinted-metadata-n
                                                (^js/String [^js/Number x]
                                                 (when (< 0 x 100)
                                                   (-> x (* x) str)))

                                                (^js/String [^js/Number x, ^js/Number y]
                                                 (when (< 0 x 100)
                                                   (-> x (* y) str)))))

               :type-hinted-metadata-n-alt (if clj?
                                             '(sut/defn
                                                ^String type-hinted-metadata-n-alt
                                                ([^Double x]
                                                 (when (< 0 x 100)
                                                   (-> x (* x) str)))

                                                ([^Double x, ^Double y]
                                                 (when (< 0 x 100)
                                                   (-> x (* y) str))))

                                             '(sut/defn
                                                ^js/String type-hinted-metadata-n-alt
                                                ([^js/Number x]
                                                 (when (< 0 x 100)
                                                   (-> x (* x) str)))

                                                ([^js/Number x, ^js/Number y]
                                                 (when (< 0 x 100)
                                                   (-> x (* y) str)))))

               :inline-function            (if clj?
                                             '(sut/defn ^present?
                                                inline-function
                                                ^string?
                                                [^double? x]
                                                (when (< 0 x 100)
                                                  (-> x (* x) str)))

                                             '(sut/defn ^present?
                                                inline-function
                                                ^string?
                                                [^number? x]
                                                (when (< 0 x 100)
                                                  (-> x (* x) str))))

               :inline-function-n          (if clj?
                                             '(sut/defn ^present?
                                                inline-function-n
                                                (^string? [^double? x]
                                                 (when (< 0 x 100)
                                                   (-> x (* x) str)))

                                                (^string? [^double? x, ^double? y]
                                                 (when (< 0 x 100)
                                                   (-> x (* y) str))))

                                             '(sut/defn ^present?
                                                inline-function-n
                                                (^string? [^number? x]
                                                 (when (< 0 x 100)
                                                   (-> x (* x) str)))

                                                (^string? [^number? x, ^number? y]
                                                 (when (< 0 x 100)
                                                   (-> x (* y) str)))))

               :inline-function-alt        (if clj?
                                             '(sut/defn ^string?
                                                inline-function-alt
                                                [^double? x]
                                                (when (< 0 x 100)
                                                  (-> x (* x) str)))

                                             '(sut/defn ^string?
                                                inline-function-alt
                                                [^number? x]
                                                (when (< 0 x 100)
                                                  (-> x (* x) str))))

               :inline-function-n-alt      (if clj?
                                             '(sut/defn ^string?
                                                inline-function-n-alt
                                                ([^double? x]
                                                 (when (< 0 x 100)
                                                   (-> x (* x) str)))

                                                ([^double? x, ^double? y]
                                                 (when (< 0 x 100)
                                                   (-> x (* y) str))))

                                             '(sut/defn ^string?
                                                inline-function-n-alt
                                                ([^number? x]
                                                 (when (< 0 x 100)
                                                   (-> x (* x) str)))

                                                ([^number? x, ^number? y]
                                                 (when (< 0 x 100)
                                                   (-> x (* y) str)))))

               :primitive                  (if clj?
                                             '(sut/defn ^string? primitive
                                                [^double x]
                                                (when (< 0 x 100)
                                                  (-> x (* x) str)))

                                             '(sut/defn ^string primitive
                                                [^number x]
                                                (when (< 0 x 100)
                                                  (-> x (* x) str))))

               :primitive-n                (if clj?
                                             '(sut/defn ^string? primitive-n
                                                ([^double x]
                                                 (when (< 0 x 100)
                                                   (-> x (* x) str)))

                                                ([^double x, ^double y]
                                                 (when (< 0 x 100)
                                                   (-> x (* y) str))))

                                             '(sut/defn ^string primitive-n
                                                ([^number x]
                                                 (when (< 0 x 100)
                                                   (-> x (* x) str)))

                                                ([^number x, ^number y]
                                                 (when (< 0 x 100)
                                                   (-> x (* y) str)))))

               :primitive-alt              (if clj?
                                             '(sut/defn primitive-alt
                                                ^string?
                                                [^double x]
                                                (when (< 0 x 100)
                                                  (-> x (* x) str)))

                                             '(sut/defn primitive-alt
                                                ^string
                                                [^number x]
                                                (when (< 0 x 100)
                                                  (-> x (* x) str))))

               :primitive-n-alt            (if clj?
                                             '(sut/defn primitive-n-alt
                                                (^string? [^double x]
                                                 (when (< 0 x 100)
                                                   (-> x (* x) str)))

                                                (^string? [^double x, ^double y]
                                                 (when (< 0 x 100)
                                                   (-> x (* y) str))))

                                             '(sut/defn primitive-n-alt
                                                (^string [^number x]
                                                 (when (< 0 x 100)
                                                   (-> x (* x) str)))

                                                (^string [^number x, ^number y]
                                                 (when (< 0 x 100)
                                                   (-> x (* y) str)))))}]
       (cond->> xs
         clj? (map (fn [[k v]]
                     [k (list 'quote v)]))
         true (into {})))))

#?(:clj (doseq [[k v] (the-defns)]
          (eval v)
          (eval `(def ~(-> k
                           name
                           (str "-macroexpansion")
                           symbol)
                   ~(list 'quote (macroexpand v))))))

#?(:cljs (the-defns))

#?(:clj
   (deftest macroexpansion
     (testing "It macroexpands to known-good (and evidently-good) forms"
       (are [input expected] (= expected input)
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
         inline-function-n-macroexpansion      '(def inline-function-n
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

         primitive-macroexpansion              '(def primitive
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
         primitive-n-macroexpansion            '(def primitive-n
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
                                                       (-> x (* y) str)))))))))

(deftest correct-execution
  (testing "Arity 1"
    (are [f] (= #?(:clj "64.0"
                   :cljs "64") (f 8.0))
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
      primitive-n
      primitive-alt
      primitive-n-alt))

  (testing "Arity 2"
    (are [f] (= #?(:clj "16.0"
                   :cljs "16") (f 8.0 2.0))
      no-metadata-n
      concise-metadata-n
      explicit-metadata-n
      type-hinted-metadata-n
      type-hinted-metadata-n-alt
      inline-function-n
      inline-function-n-alt
      primitive-n
      primitive-n-alt)))

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
          :thrown     primitive-n
          :thrown     primitive-alt
          :thrown     primitive-n-alt))))

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
          :thrown     primitive-n
          :thrown     primitive-n-alt)))))

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
          :thrown     primitive-n
          :thrown     primitive-alt
          :thrown     primitive-n-alt))))

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
          :thrown     primitive-n
          :thrown     primitive-n-alt)))))

(deftest type-hint-emission
  (testing "Type hints are preserved or emitted"

    (testing "Return value hinting for single-arity functions"
      (are [v] (testing v
                 (-> v meta :tag #{#?(:clj String
                                      :cljs 'string)}))
        #'type-hinted-metadata
        #'type-hinted-metadata-n
        #'type-hinted-metadata-alt
        #'type-hinted-metadata-n-alt
        #'inline-function
        #'inline-function-n
        #'inline-function-alt
        #'inline-function-n-alt
        #'primitive
        #'primitive-n
        #'primitive-alt
        #'primitive-n-alt))

    ;; in CLJS, arglists themselves don't ever get a :tag - only its members.
    #?(:clj
       (testing "Arglist hinting for single-arity functions"
         (are [v] (testing v
                    (-> v meta :arglists first meta :tag #{#?(:clj String
                                                              :cljs 'string)}))
           #'type-hinted-metadata
           #'type-hinted-metadata-alt
           #'inline-function
           #'inline-function-alt
           #'primitive
           #'primitive-n
           #'primitive-alt
           #'primitive-n-alt)))

    (testing "Arguments hinting for single-arity functions"
      (are [v] (testing v
                 (-> v meta :arglists ffirst meta :tag #{#?(:clj `Double
                                                            :cljs 'number)}))
        #'type-hinted-metadata
        #'type-hinted-metadata-alt
        #'inline-function
        #'inline-function-alt))

    (testing "Primitive arguments hinting for single-arity functions"
      (are [v] (testing v
                 (-> v meta :arglists ffirst meta :tag #{#?(:clj 'double
                                                            :cljs 'number)}))
        #'primitive
        #'primitive-alt))

    ;; in CLJS, arglists themselves don't ever get a :tag - only its members.
    #?(:clj
       (testing "Arglist hinting for multi-arity functions"
         (are [v] (testing v
                    (->> v
                         meta
                         :arglists
                         (map meta)
                         (map :tag)
                         (every-and-at-least-one? #{#?(:clj String
                                                       :cljs 'string)})))
           #'type-hinted-metadata-n
           #'type-hinted-metadata-n-alt
           #'inline-function-n
           #'inline-function-n-alt
           #'primitive
           #'primitive-n
           #'primitive-alt
           #'primitive-n-alt)))

    (testing "Arguments hinting for multi-arity functions"
      (are [v] (testing v
                 (->> v
                      meta
                      :arglists
                      (map (fn [arglist]
                             (->> arglist
                                  (map meta)
                                  (map :tag)
                                  (every-and-at-least-one? #{#?(:clj `Double
                                                                :cljs 'number)}))))
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
                                  (every-and-at-least-one? #{#?(:clj 'double
                                                                :cljs 'number)}))))
                      (every-and-at-least-one? true?)))
        #'primitive-n
        #'primitive-n-alt))

    #?(:clj
       (testing ":tag metadata placed in wrong positions is guarded against"
         (are [desc input] (testing desc
                             (testing input
                               (try
                                 (eval input)
                                 false
                                 (catch #?(:clj Exception :cljs js/Error) e
                                   true))))

           "Hinting an arity"
           #?(:clj  '(sut/defn faulty
                       ^String ([x]
                                (-> x (* x) str)))
              :cljs '(sut/defn faulty
                       ^js/String ([x]
                                   (-> x (* x) str)))))))

    #?(:clj
       (testing "Only long and double primitive annotations are supported, just like in clojure.core/defn"
         (are [input good?] (testing input
                              (try
                                (eval input)
                                good?
                                (catch #?(:clj Exception :cljs js/Error) e
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
           '(defn primitive-sample ^double [x])                    true)))

    #?(:clj
       (are [input expected-tag] (testing input
                                   (testing "If the user tags the defn name (as opposed to the arg vector) with a primitive hint"
                                     (eval input)
                                     (testing "The hint will be removed"
                                       (is (-> 'primitive-sample resolve meta :tag nil?)))
                                     (testing "The argument vector will receive the primitive hint instead"
                                       (is (-> 'primitive-sample resolve meta :arglists first meta :tag #{expected-tag})))))
         '(nedap.utils.speced/defn ^long primitive-sample [x])   'long
         '(nedap.utils.speced/defn ^double primitive-sample [x]) 'double))

    #?(:clj
       (testing "Inline function specs can emit type hints of array type"
         (sut/defn ^bytes? bytes-defn [])
         (let [the-class (Class/forName "[B")]
           (is (-> #'bytes-defn meta :tag #{the-class}))
           (is (= the-class (-> #'bytes-defn meta :arglists first meta :tag #{the-class}))))))))

#?(:clj
   (deftest nilable-primitive-specs
     (testing "^:nedap.utils.speced/nilable <primitive hint> is forbidden"
       (are [input] (try
                      (eval input)
                      false
                      (catch Exception e
                        (-> e .getCause .getMessage (string/includes? impl.parsing/forbidden-primitives-message))))
         '(nedap.utils.speced/defn nilable-primitive-specs-1 ^:nedap.utils.speced/nilable ^double [])

         '(nedap.utils.speced/defn nilable-primitive-specs-2 [^:nedap.utils.speced/nilable ^double x])

         '(nedap.utils.speced/defn ^:nedap.utils.speced/nilable ^double nilable-primitive-specs-3 [])))))
