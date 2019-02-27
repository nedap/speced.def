(ns unit.nedap.utils.speced.defn
  "NOTE: all these demonstrate the following aspect documented in `:nedap.utils.spec.specs/spec-metadata`:

  > A defn with return value metadata for both its name and argument vector will emit spec checking for both.

  In practice, that is completely optional and you are free to use 2, 1, or 0 return value hints in any position of your choice."
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer :all]
   [nedap.utils.speced :as sut]))

(spec/def ::age pos?)

(spec/def ::temperature int?)

(spec/def ::name (spec/and string? (fn [x]
                                     (-> x count (< 10)))))

(spec/def ::present? some?)

(doseq [[k v] {:no-metadata '(sut/defn no-metadata [x]
                               (-> x (* x) str))

               :no-metadata-n '(sut/defn no-metadata-n
                                 ([x]
                                  (-> x (* x) str))
                                 ([x y]
                                  (-> x (* y) str)))

               :concise-metadata '(sut/defn ^::present?
                                    concise-metadata
                                    ^::name
                                    [^::age x]
                                    (-> x (* x) str))

               :concise-metadata-n '(sut/defn ^::present?
                                      concise-metadata-n
                                      (^::name
                                       [^::age x]
                                       (-> x (* x) str))
                                      (^::name
                                       [^::age x ^::temperature y]
                                       (-> x (* y) str)))

               :explicit-metadata '(sut/defn ^{::spec ::present?}
                                     explicit-metadata
                                     ^{::spec ::name}
                                     [^{::spec ::age} x]
                                     (-> x (* x) str))

               :explicit-metadata-n '(sut/defn ^{::spec ::present?}
                                       explicit-metadata-n
                                       (^{::spec ::name}
                                        [^{::spec ::age} x]
                                        (-> x (* x) str))
                                       (^{::spec ::name}
                                        [^{::spec ::age} x
                                         ^{::spec ::temperature} y]
                                        (-> x (* y) str)))

               :type-hinted-metadata '(sut/defn ^Object
                                        type-hinted-metadata
                                        ^String
                                        [^Long x]
                                        (when (< 0 x 100)
                                          (-> x (* x) str)))

               :type-hinted-metadata-n '(sut/defn ^Object
                                          type-hinted-metadata-n
                                          (^String [^Long x]
                                           (when (< 0 x 100)
                                             (-> x (* x) str)))

                                          (^String [^Long x ^Long y]
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
      no-metadata-macroexpansion '(def no-metadata (clojure.core/fn ([x]
                                                                     {:pre [], :post []}
                                                                     (-> x (* x) str))))
      no-metadata-n-macroexpansion '(def no-metadata-n
                                      (clojure.core/fn
                                        ([x]
                                         {:pre [], :post []}
                                         (-> x (* x) str))
                                        ([x y]
                                         {:pre [], :post []}
                                         (-> x (* y) str))))
      concise-metadata-macroexpansion '(def concise-metadata
                                         (clojure.core/fn
                                           ([x]
                                            {:pre
                                             [(nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/age x)],
                                             :post
                                             [(nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/present? %)
                                              (nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/name %)]}
                                            (-> x (* x) str))))
      concise-metadata-n-macroexpansion '(def concise-metadata-n
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
      explicit-metadata-macroexpansion '(def explicit-metadata
                                          (clojure.core/fn
                                            ([x]
                                             {:pre
                                              [(nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/age x)],
                                              :post
                                              [(nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/present? %)
                                               (nedap.utils.spec.api/check! :unit.nedap.utils.speced.defn/name %)]}
                                             (-> x (* x) str))))
      explicit-metadata-n-macroexpansion '(def explicit-metadata-n
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
      type-hinted-metadata-macroexpansion '(def type-hinted-metadata
                                             (clojure.core/fn
                                               ([x]
                                                {:pre
                                                 [(nedap.utils.spec.api/check! (fn [x] (clojure.core/instance? Long x)) x)],
                                                 :post
                                                 [(nedap.utils.spec.api/check! (fn [x] (clojure.core/instance? Object x)) %)
                                                  (nedap.utils.spec.api/check! (fn [x] (clojure.core/instance? String x)) %)]}
                                                (when (< 0 x 100)
                                                  (-> x (* x) str)))))
      type-hinted-metadata-n-macroexpansion '(def type-hinted-metadata-n
                                               (clojure.core/fn
                                                 ([x]
                                                  {:pre
                                                   [(nedap.utils.spec.api/check!
                                                     (fn [x] (clojure.core/instance? Long x))
                                                     x)],
                                                   :post
                                                   [(nedap.utils.spec.api/check!
                                                     (fn [x] (clojure.core/instance? Object x))
                                                     %)
                                                    (nedap.utils.spec.api/check!
                                                     (fn [x] (clojure.core/instance? String x))
                                                     %)]}
                                                  (when (< 0 x 100) (-> x (* x) str)))
                                                 ([x y]
                                                  {:pre
                                                   [(nedap.utils.spec.api/check!
                                                     (fn [x] (clojure.core/instance? Long x))
                                                     x
                                                     (fn [x] (clojure.core/instance? Long x))
                                                     y)],
                                                   :post
                                                   [(nedap.utils.spec.api/check!
                                                     (fn [x] (clojure.core/instance? Object x))
                                                     %)
                                                    (nedap.utils.spec.api/check!
                                                     (fn [x] (clojure.core/instance? String x))
                                                     %)]}
                                                  (when (< 0 x 100) (-> x (* y) str))))))))

(deftest correct-execution
  (testing "Arity 1"
    (are [f] (= "64" (f 8))
      no-metadata
      no-metadata-n
      concise-metadata
      concise-metadata-n
      explicit-metadata
      explicit-metadata-n
      type-hinted-metadata
      type-hinted-metadata-n))

  (testing "Arity 2"
    (are [f] (= "16" (f 8 2))
      no-metadata-n
      concise-metadata-n
      explicit-metadata-n
      type-hinted-metadata-n)))

(deftest preconditions-are-checked
  (testing "Arity 1"
    (with-out-str
      (let [arg 0]
        (are [expectation f] (case expectation
                               :not-thrown (= "0" (f arg))
                               :thrown (try
                                         (f arg 1)
                                         false
                                         (catch Exception e
                                           (-> e ex-data :spec))))
          :not-thrown no-metadata-n
          :thrown     concise-metadata-n
          :thrown     explicit-metadata-n
          :thrown     type-hinted-metadata-n))))

  (testing "Arity 2"
    (with-out-str
      (let [arg 0]
        (are [expectation f] (case expectation
                               :not-thrown (= "0" (f arg))
                               :thrown (try
                                         (f arg)
                                         false
                                         (catch Exception e
                                           (-> e ex-data :spec))))
          :not-thrown no-metadata
          :not-thrown no-metadata-n
          :thrown     concise-metadata
          :thrown     concise-metadata-n
          :thrown     explicit-metadata
          :thrown     explicit-metadata-n
          :thrown     type-hinted-metadata
          :thrown     type-hinted-metadata-n)))))

(deftest postconditions-are-checked
  (testing "Arity 1"
    (with-out-str
      (let [arg 99999]
        (are [expectation f] (case expectation
                               :not-thrown (= "9999800001" (f arg))
                               :thrown (try
                                         (f arg)
                                         false
                                         (catch Exception e
                                           (-> e ex-data :spec))))
          :not-thrown no-metadata
          :not-thrown no-metadata-n
          :thrown     concise-metadata
          :thrown     concise-metadata-n
          :thrown     explicit-metadata
          :thrown     explicit-metadata-n
          :thrown     type-hinted-metadata
          :thrown     type-hinted-metadata-n))))

  (testing "Arity 2"
    (with-out-str
      (let [arg1 99999
            arg2 100000]
        (are [expectation f] (case expectation
                               :not-thrown (= "9999900000" (f arg1 arg2))
                               :thrown (try
                                         (f arg1 arg2)
                                         false
                                         (catch Exception e
                                           (-> e ex-data :spec))))
          :not-thrown no-metadata-n
          :thrown     concise-metadata-n
          :thrown     explicit-metadata-n
          :thrown     type-hinted-metadata-n)))))

;; Plain defn, not sut/defn
(defn plain-defn-arity-1 ^String [s])

;; Plain defn, not sut/defn
(defn plain-defn-arity-n
  (^String [x])
  (^String [x y]))

(deftest type-hint-emission
  (testing "Emitted type hints matches Clojure's behavior"

    (are [v] (-> v meta :tag nil?)
      #'plain-defn-arity-1
      #'type-hinted-metadata meta
      #'type-hinted-metadata-n meta)

    (are [v] (-> v meta :arglists first meta :tag #{`String})
      #'type-hinted-metadata
      #'plain-defn-arity-1)

    (are [v] (->> v
                  meta
                  :arglists
                  (map meta)
                  (map :tag)
                  (every? #{`String}))
      #'type-hinted-metadata-n
      #'plain-defn-arity-n)))
