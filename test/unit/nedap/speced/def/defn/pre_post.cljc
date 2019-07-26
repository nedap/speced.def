(ns unit.nedap.speced.def.defn.pre-post
  "This ns duplicates `unit.nedap.speced.def.defn`, but adding a 'manual' :pre/:post map to each example defn.

  That way the tests can remain comprehensible."
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.speced.def :as sut]
   [unit.nedap.test-helpers :refer [every-and-at-least-one?]]))

(do
  #?@(:clj
      [(spec/def ::age pos?)

       (spec/def ::temperature double?)

       (spec/def ::name (spec/and string? (fn [x]
                                            (-> x count (< 10)))))

       (defn present? [x]
         (some? x))

       (spec/def ::present? present?)

       (doseq [[k v] {:no-metadata            '(sut/defn no-metadata [x]
                                                 {:pre  [:pre]
                                                  :post [:post]}
                                                 (-> x (* x) str))

                      :no-metadata-n          '(sut/defn no-metadata-n
                                                 ([x]
                                                  {:pre  [:pre]
                                                   :post [:post]}
                                                  (-> x (* x) str))
                                                 ([x y]
                                                  {:pre  [:pre]
                                                   :post [:post]}
                                                  (-> x (* y) str)))

                      :concise-metadata       '(sut/defn ^::present?
                                                 concise-metadata
                                                 ^::name
                                                 [^::age x]
                                                 {:pre  [:pre]
                                                  :post [:post]}
                                                 (-> x (* x) str))

                      :concise-metadata-n     '(sut/defn ^::present?
                                                 concise-metadata-n
                                                 (^::name
                                                  [^::age x]
                                                  {:pre  [:pre]
                                                   :post [:post]}
                                                  (-> x (* x) str))
                                                 (^::name
                                                  [^::age x ^::temperature y]
                                                  {:pre  [:pre]
                                                   :post [:post]}
                                                  (-> x (* y) str)))

                      :explicit-metadata      '(sut/defn ^{::sut/spec ::present?}
                                                 explicit-metadata
                                                 ^{::sut/spec ::name}
                                                 [^{::sut/spec ::age} x]
                                                 {:pre  [:pre]
                                                  :post [:post]}
                                                 (-> x (* x) str))

                      :explicit-metadata-n    '(sut/defn ^{::sut/spec ::present?}
                                                 explicit-metadata-n
                                                 (^{::sut/spec ::name}
                                                  [^{::sut/spec ::age} x]
                                                  {:pre  [:pre]
                                                   :post [:post]}
                                                  (-> x (* x) str))
                                                 (^{::sut/spec ::name}
                                                  [^{::sut/spec ::age} x
                                                   ^{::sut/spec ::temperature} y]
                                                  {:pre  [:pre]
                                                   :post [:post]}
                                                  (-> x (* y) str)))

                      :type-hinted-metadata   '(sut/defn
                                                 type-hinted-metadata
                                                 ^String
                                                 [^Double x]
                                                 {:pre  [:pre]
                                                  :post [:post]}
                                                 (when (< 0 x 100)
                                                   (-> x (* x) str)))

                      :type-hinted-metadata-n '(sut/defn
                                                 type-hinted-metadata-n
                                                 (^String [^Double x]
                                                  {:pre  [:pre]
                                                   :post [:post]}
                                                  (when (< 0 x 100)
                                                    (-> x (* x) str)))

                                                 (^String [^Double x ^Double y]
                                                  {:pre  [:pre]
                                                   :post [:post]}
                                                  (when (< 0 x 100)
                                                    (-> x (* y) str))))

                      :inline-function        '(sut/defn ^present?
                                                 inline-function
                                                 ^string?
                                                 [^double? x]
                                                 {:pre  [:pre]
                                                  :post [:post]}
                                                 (when (< 0 x 100)
                                                   (-> x (* x) str)))

                      :inline-function-n      '(sut/defn ^present?
                                                 inline-function-n
                                                 (^string? [^double? x]
                                                  {:pre  [:pre]
                                                   :post [:post]}
                                                  (when (< 0 x 100)
                                                    (-> x (* x) str)))

                                                 (^string? [^double? x ^double? y]
                                                  {:pre  [:pre]
                                                   :post [:post]}
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
                                                                                       {:pre  [:pre]
                                                                                        :post [:post]}
                                                                                       (-> x (* x) str))))
             no-metadata-n-macroexpansion          '(def no-metadata-n
                                                      (clojure.core/fn
                                                        ([x]
                                                         {:pre [:pre], :post [:post]}
                                                         (-> x (* x) str))
                                                        ([x y]
                                                         {:pre [:pre], :post [:post]}
                                                         (-> x (* y) str))))

             concise-metadata-macroexpansion       '(def concise-metadata
                                                      (clojure.core/fn
                                                        ([x]
                                                         {:pre
                                                          [:pre
                                                           (nedap.utils.spec.api/check! :unit.nedap.speced.def.defn.pre-post/age x)],
                                                          :post
                                                          [:post
                                                           (nedap.utils.spec.api/check! :unit.nedap.speced.def.defn.pre-post/present? %)
                                                           (nedap.utils.spec.api/check! :unit.nedap.speced.def.defn.pre-post/name %)]}
                                                         (-> x (* x) str))))

             concise-metadata-n-macroexpansion     '(def concise-metadata-n
                                                      (clojure.core/fn
                                                        ([x]
                                                         {:pre
                                                          [:pre
                                                           (nedap.utils.spec.api/check! :unit.nedap.speced.def.defn.pre-post/age x)],
                                                          :post
                                                          [:post
                                                           (nedap.utils.spec.api/check! :unit.nedap.speced.def.defn.pre-post/present? %)
                                                           (nedap.utils.spec.api/check! :unit.nedap.speced.def.defn.pre-post/name  %)]}
                                                         (-> x (* x) str))
                                                        ([x y]
                                                         {:pre
                                                          [:pre
                                                           (nedap.utils.spec.api/check! :unit.nedap.speced.def.defn.pre-post/age x
                                                                                        :unit.nedap.speced.def.defn.pre-post/temperature y)],
                                                          :post
                                                          [:post
                                                           (nedap.utils.spec.api/check! :unit.nedap.speced.def.defn.pre-post/present? %)
                                                           (nedap.utils.spec.api/check! :unit.nedap.speced.def.defn.pre-post/name  %)]}
                                                         (-> x (* y) str))))

             explicit-metadata-macroexpansion      '(def explicit-metadata
                                                      (clojure.core/fn
                                                        ([x]
                                                         {:pre
                                                          [:pre
                                                           (nedap.utils.spec.api/check! :unit.nedap.speced.def.defn.pre-post/age x)],
                                                          :post
                                                          [:post
                                                           (nedap.utils.spec.api/check! :unit.nedap.speced.def.defn.pre-post/present? %)
                                                           (nedap.utils.spec.api/check! :unit.nedap.speced.def.defn.pre-post/name %)]}
                                                         (-> x (* x) str))))

             explicit-metadata-n-macroexpansion    '(def explicit-metadata-n
                                                      (clojure.core/fn
                                                        ([x]
                                                         {:pre
                                                          [:pre
                                                           (nedap.utils.spec.api/check! :unit.nedap.speced.def.defn.pre-post/age x)],
                                                          :post
                                                          [:post
                                                           (nedap.utils.spec.api/check! :unit.nedap.speced.def.defn.pre-post/present? %)
                                                           (nedap.utils.spec.api/check! :unit.nedap.speced.def.defn.pre-post/name  %)]}
                                                         (-> x (* x) str))
                                                        ([x y]
                                                         {:pre
                                                          [:pre
                                                           (nedap.utils.spec.api/check! :unit.nedap.speced.def.defn.pre-post/age x
                                                                                        :unit.nedap.speced.def.defn.pre-post/temperature y)],
                                                          :post
                                                          [:post
                                                           (nedap.utils.spec.api/check! :unit.nedap.speced.def.defn.pre-post/present? %)
                                                           (nedap.utils.spec.api/check! :unit.nedap.speced.def.defn.pre-post/name  %)]}
                                                         (-> x (* y) str))))

             type-hinted-metadata-macroexpansion   '(def type-hinted-metadata
                                                      (clojure.core/fn
                                                        ([x]
                                                         {:pre
                                                          [:pre
                                                           (nedap.utils.spec.api/check! (fn [x]
                                                                                          (if (clojure.core/class? Double)
                                                                                            (clojure.core/instance? Double x)
                                                                                            (clojure.core/satisfies? Double x)))
                                                                                        x)],
                                                          :post
                                                          [:post
                                                           (nedap.utils.spec.api/check! (fn [x]
                                                                                          (if (clojure.core/class? String)
                                                                                            (clojure.core/instance? String x)
                                                                                            (clojure.core/satisfies? String x)))
                                                                                        %)]}
                                                         (when (< 0 x 100)
                                                           (-> x (* x) str)))))

             type-hinted-metadata-n-macroexpansion '(def type-hinted-metadata-n
                                                      (clojure.core/fn
                                                        ([x]
                                                         {:pre
                                                          [:pre
                                                           (nedap.utils.spec.api/check!
                                                            (fn [x]
                                                              (if (clojure.core/class? Double)
                                                                (clojure.core/instance? Double x)
                                                                (clojure.core/satisfies? Double x)))
                                                            x)],
                                                          :post
                                                          [:post
                                                           (nedap.utils.spec.api/check!
                                                            (fn [x]
                                                              (if (clojure.core/class? String)
                                                                (clojure.core/instance? String x)
                                                                (clojure.core/satisfies? String x)))
                                                            %)]}
                                                         (when (< 0 x 100)
                                                           (-> x (* x) str)))
                                                        ([x y]
                                                         {:pre
                                                          [:pre
                                                           (nedap.utils.spec.api/check!
                                                            (fn [x]
                                                              (if (clojure.core/class? Double)
                                                                (clojure.core/instance? Double x)
                                                                (clojure.core/satisfies? Double x)))
                                                            x
                                                            (fn [x]
                                                              (if (clojure.core/class? Double)
                                                                (clojure.core/instance? Double x)
                                                                (clojure.core/satisfies? Double x)))
                                                            y)],
                                                          :post
                                                          [:post
                                                           (nedap.utils.spec.api/check!
                                                            (fn [x]
                                                              (if (clojure.core/class? String)
                                                                (clojure.core/instance? String x)
                                                                (clojure.core/satisfies? String x)))
                                                            %)]}
                                                         (when (< 0 x 100)
                                                           (-> x (* y) str)))))

             inline-function-macroexpansion        '(def inline-function
                                                      (clojure.core/fn
                                                        ([x]
                                                         {:pre [:pre
                                                                (nedap.utils.spec.api/check!
                                                                 (clojure.spec.alpha/and
                                                                  double?
                                                                  (fn [x]
                                                                    (if (clojure.core/class? java.lang.Double)
                                                                      (clojure.core/instance? java.lang.Double x)
                                                                      (clojure.core/satisfies? java.lang.Double x))))
                                                                 x)],
                                                          :post
                                                          [:post
                                                           (nedap.utils.spec.api/check! present? %)
                                                           (nedap.utils.spec.api/check!
                                                            (clojure.spec.alpha/and
                                                             string?
                                                             (fn [x]
                                                               (if (clojure.core/class? java.lang.String)
                                                                 (clojure.core/instance? java.lang.String x)
                                                                 (clojure.core/satisfies? java.lang.String x))))
                                                            %)]}
                                                         (when (< 0 x 100)
                                                           (-> x (* x) str)))))
             inline-function-n-macroexpansion      '(def
                                                      inline-function-n
                                                      (clojure.core/fn
                                                        ([x]
                                                         {:pre [:pre
                                                                (nedap.utils.spec.api/check!
                                                                 (clojure.spec.alpha/and
                                                                  double?
                                                                  (fn [x]
                                                                    (if (clojure.core/class? java.lang.Double)
                                                                      (clojure.core/instance? java.lang.Double x)
                                                                      (clojure.core/satisfies? java.lang.Double x))))
                                                                 x)],
                                                          :post
                                                          [:post
                                                           (nedap.utils.spec.api/check! present? %)
                                                           (nedap.utils.spec.api/check!
                                                            (clojure.spec.alpha/and
                                                             string?
                                                             (fn [x]
                                                               (if (clojure.core/class? java.lang.String)
                                                                 (clojure.core/instance? java.lang.String x)
                                                                 (clojure.core/satisfies? java.lang.String x))))
                                                            %)]}
                                                         (when (< 0 x 100)
                                                           (-> x (* x) str)))
                                                        ([x y]
                                                         {:pre [:pre
                                                                (nedap.utils.spec.api/check!
                                                                 (clojure.spec.alpha/and
                                                                  double?
                                                                  (fn [x]
                                                                    (if (clojure.core/class? java.lang.Double)
                                                                      (clojure.core/instance? java.lang.Double x)
                                                                      (clojure.core/satisfies? java.lang.Double x))))
                                                                 x
                                                                 (clojure.spec.alpha/and
                                                                  double?
                                                                  (fn [x]
                                                                    (if (clojure.core/class? java.lang.Double)
                                                                      (clojure.core/instance? java.lang.Double x)
                                                                      (clojure.core/satisfies? java.lang.Double x))))
                                                                 y)],
                                                          :post
                                                          [:post
                                                           (nedap.utils.spec.api/check! present? %)
                                                           (nedap.utils.spec.api/check!
                                                            (clojure.spec.alpha/and
                                                             string?
                                                             (fn [x]
                                                               (if (clojure.core/class? java.lang.String)
                                                                 (clojure.core/instance? java.lang.String x)
                                                                 (clojure.core/satisfies? java.lang.String x))))
                                                            %)]}
                                                         (when (< 0 x 100)
                                                           (-> x (* y) str))))))))

       (deftest correct-execution
         (testing "Arity 1"
           (are [f] (testing f
                      (= "64.0" (f 8.0)))
             no-metadata
             no-metadata-n
             concise-metadata
             concise-metadata-n
             explicit-metadata
             explicit-metadata-n
             type-hinted-metadata
             type-hinted-metadata-n
             inline-function
             inline-function-n))

         (testing "Arity 2"
           (are [f] (testing f
                      (= "16.0" (f 8.0 2.0)))
             no-metadata-n
             concise-metadata-n
             explicit-metadata-n
             type-hinted-metadata-n
             inline-function-n)))

       (deftest preconditions-are-checked

         (testing "Arity 1"
           (with-out-str
             (let [arg 0]
               (are [expectation f] (testing f
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
                 :thrown     inline-function
                 :thrown     inline-function-n))))

         (testing "Arity 2"
           (with-out-str
             (let [arg 0]
               (are [expectation f] (testing f
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
                 :thrown     inline-function-n)))))

       (deftest postconditions-are-checked
         (testing "Arity 1"
           (with-out-str
             (let [arg 99999]
               (are [expectation f] (testing f
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
                 :thrown     inline-function
                 :thrown     inline-function-n))))

         (testing "Arity 2"
           (with-out-str
             (let [arg1 99999
                   arg2 100000]
               (are [expectation f] (testing f
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
                 :thrown     inline-function-n)))))

       (deftest type-hint-emission
         (testing "Type hints are preserved or emitted"

           (testing "Return value hinting for single-arity functions"
             (are [v] (testing v
                        (-> v meta :tag #{String}))
               #'type-hinted-metadata
               #'type-hinted-metadata-n
               #'inline-function
               #'inline-function-n))

           (testing "Arglist hinting for single-arity functions"
             (are [v] (testing v
                        (-> v meta :arglists first meta :tag #{String}))
               #'type-hinted-metadata
               #'inline-function))

           (testing "Arglist hinting for single-arity functions"
             (are [v] (testing v
                        (-> v meta :arglists ffirst meta :tag #{`Double}))
               #'type-hinted-metadata
               #'inline-function))

           (testing "Return value hinting for multi-arity functions"
             (are [v] (testing v
                        (->> v
                             meta
                             :arglists
                             (map meta)
                             (map :tag)
                             (every-and-at-least-one? #{String})))
               #'type-hinted-metadata-n
               #'inline-function-n))

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
               #'inline-function-n))))]))
