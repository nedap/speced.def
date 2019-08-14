(ns unit.nedap.speced.def.letfn
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [clojure.string :as string]
   [nedap.speced.def :as sut]
   [nedap.speced.def.impl.parsing :as impl.parsing]
   [nedap.utils.spec.api :refer [check!]]
   [nedap.utils.test.api :refer [macroexpansion=]]
   [nedap.utils.test.api :refer [meta=]]
   [unit.nedap.test-helpers :refer [every-and-at-least-one?]])
  #?(:cljs (:require-macros [unit.nedap.speced.def.letfn :refer [letfn-specimen-1 letfn-specimen-2 letfn-specimen-3 letfn-specimen-4 letfn-specimen-5]])))

(defn fnspecs-example [clj?]
  (if clj?
    '[(single-signature [^string? a
                         {:keys [^long b]}
                         not-speced]
                        [a b not-speced])
      (single-signature-wrapped ([^string? a
                                  {:keys [^long b]}
                                  not-speced]
                                 [a b not-speced]))
      (multiple-signatures
       ([^string? a
         {:keys [^long b]}]
        [a b])
       ([^string? a
         {:keys [^long b]}
         not-speced]
        [a b not-speced]))

      (alternative-destructuring [[^string? x]]
                                 x)
      (^string? hinted-name [a]
                            a)
      (hinted-argv ^string? [a]
                   a)]
    '[(single-signature [^string? a
                         {:keys [^number b]}
                         not-speced]
                        [a b not-speced])
      (single-signature-wrapped ([^string? a
                                  {:keys [^number b]}
                                  not-speced]
                                 [a b not-speced]))
      (multiple-signatures
       ([^string? a
         {:keys [^number b]}]
        [a b])
       ([^string? a
         {:keys [^number b]}
         not-speced]
        [a b not-speced]))
      (alternative-destructuring [[^string? x]]
                                 x)
      (^string? hinted-name [a]
                            a)
      (hinted-argv ^string? [a]
                   a)]))

#?(:clj
   (defmacro letfn-specimen-1 []
     (let [clj? (-> &env :ns nil?)]
       (list `sut/letfn
             (fnspecs-example clj?)
             '[(single-signature "a" {:b 2} ::anything)
               (single-signature-wrapped "a" {:b 2} ::anything)
               (multiple-signatures "a" {:b 2})
               (multiple-signatures "a" {:b 2} ::anything)]))))

#?(:clj
   (defmacro letfn-specimen-2 []
     (let [clj? (-> &env :ns nil?)]
       (list `sut/letfn
             (fnspecs-example clj?)
             '[(single-signature nil {:b 2} ::anything)]))))

#?(:clj
   (defmacro letfn-specimen-3 []
     (let [clj? (-> &env :ns nil?)]
       (list `sut/letfn
             (fnspecs-example clj?)
             '[(single-signature nil {:b nil} ::anything)]))))

#?(:clj
   (defmacro letfn-specimen-4 []
     (let [clj? (-> &env :ns nil?)]
       (list `sut/letfn
             (fnspecs-example clj?)
             '[(alternative-destructuring ["a string"])]))))

#?(:clj
   (defmacro letfn-specimen-5 []
     (let [clj? (-> &env :ns nil?)]
       (list `sut/letfn
             (fnspecs-example clj?)
             '[(alternative-destructuring [:not-a-string])]))))

#?(:clj
   (defmacro macroexpansion-specimens []
     (let [xs {:specimen-1 (macroexpand-1 (macroexpand-1 '(letfn-specimen-1)))}]
       (->> xs
            (map (fn [[k v]]
                   [k (list 'quote v)]))
            (into {})))))

#?(:clj
   (doseq [[k v] (macroexpansion-specimens)]
     (eval `(def ~(-> k
                      name
                      (str "-macroexpansion")
                      symbol)
              ~(list 'quote v)))))

#?(:clj
   (deftest macroexpansions
     (testing "It expands to a known-good, reasonable-looking form"
       (let [the-letfn '(clojure.core/letfn [(single-signature ([a {:keys [b]} not-speced]
                                                                {:pre  [(nedap.utils.spec.api/check!
                                                                         (fn [x]
                                                                           (if (clojure.core/class? java.lang.Long)
                                                                             (clojure.core/instance? java.lang.Long x)
                                                                             (clojure.core/satisfies? java.lang.Long x)))
                                                                         b

                                                                         (clojure.spec.alpha/and
                                                                          string?
                                                                          (fn [x]
                                                                            (if (clojure.core/class? java.lang.String)
                                                                              (clojure.core/instance? java.lang.String x)
                                                                              (clojure.core/satisfies? java.lang.String x))))
                                                                         a)],
                                                                 :post []}
                                                                [a b not-speced]))
                                             (single-signature-wrapped ([a {:keys [b]} not-speced]
                                                                        {:pre  [(nedap.utils.spec.api/check!
                                                                                 (fn [x]
                                                                                   (if (clojure.core/class? java.lang.Long)
                                                                                     (clojure.core/instance? java.lang.Long x)
                                                                                     (clojure.core/satisfies? java.lang.Long x)))
                                                                                 b

                                                                                 (clojure.spec.alpha/and
                                                                                  string?
                                                                                  (fn [x]
                                                                                    (if (clojure.core/class? java.lang.String)
                                                                                      (clojure.core/instance? java.lang.String x)
                                                                                      (clojure.core/satisfies? java.lang.String x))))
                                                                                 a)]
                                                                         :post []}
                                                                        [a b not-speced]))
                                             (multiple-signatures
                                               ([a {:keys [b]}]
                                                {:pre  [(nedap.utils.spec.api/check!
                                                         (fn [x]
                                                           (if (clojure.core/class? java.lang.Long)
                                                             (clojure.core/instance? java.lang.Long x)
                                                             (clojure.core/satisfies? java.lang.Long x)))
                                                         b

                                                         (clojure.spec.alpha/and
                                                          string?
                                                          (fn [x]
                                                            (if (clojure.core/class? java.lang.String)
                                                              (clojure.core/instance? java.lang.String x)
                                                              (clojure.core/satisfies? java.lang.String x))))
                                                         a)],
                                                 :post []}
                                                [a b])
                                               ([a {:keys [b]} not-speced]
                                                {:pre  [(nedap.utils.spec.api/check!
                                                         (fn [x]
                                                           (if (clojure.core/class? java.lang.Long)
                                                             (clojure.core/instance? java.lang.Long x)
                                                             (clojure.core/satisfies? java.lang.Long x)))
                                                         b

                                                         (clojure.spec.alpha/and
                                                          string?
                                                          (fn [x]
                                                            (if (clojure.core/class? java.lang.String)
                                                              (clojure.core/instance? java.lang.String x)
                                                              (clojure.core/satisfies? java.lang.String x))))
                                                         a)]
                                                 :post []}
                                                [a b not-speced]))
                                             (alternative-destructuring
                                               ([[x]]
                                                {:pre
                                                 [(nedap.utils.spec.api/check!
                                                   (clojure.spec.alpha/and
                                                    string?
                                                    (fn
                                                      [x]
                                                      (if (clojure.core/class? java.lang.String)
                                                        (clojure.core/instance? java.lang.String x)
                                                        (clojure.core/satisfies? java.lang.String x))))
                                                   x)],
                                                 :post []}
                                                x))
                                             (hinted-name
                                               ([a]
                                                {:pre [],
                                                 :post
                                                 [(nedap.utils.spec.api/check!
                                                   (clojure.spec.alpha/and
                                                    string?
                                                    (fn [x]
                                                      (if (clojure.core/class? java.lang.String)
                                                        (clojure.core/instance? java.lang.String x)
                                                        (clojure.core/satisfies? java.lang.String x))))
                                                   %)]}
                                                a))
                                             (hinted-argv
                                               ([a]
                                                {:pre [],
                                                 :post
                                                 [(nedap.utils.spec.api/check!
                                                   (clojure.spec.alpha/and
                                                    string?
                                                    (fn [x]
                                                      (if (clojure.core/class? java.lang.String)
                                                        (clojure.core/instance? java.lang.String x)
                                                        (clojure.core/satisfies? java.lang.String x))))
                                                   %)]}
                                                a))]
                          [(single-signature "a" {:b 2} ::anything)
                           (single-signature-wrapped "a" {:b 2} ::anything)
                           (multiple-signatures "a" {:b 2})
                           (multiple-signatures "a" {:b 2} ::anything)])]
         (is (macroexpansion= the-letfn
                              specimen-1-macroexpansion))))
     (testing "type hint metadata is inferred (simple symbols, associative destrucuring)"
       (let [[string-hinted
              {[long-hinted] :keys}] (->> specimen-1-macroexpansion second first second first)]
         (is (meta= string-hinted
                    (with-meta 'a {:tag `String})))
         (is (meta= long-hinted
                    (with-meta 'b {:tag 'long})))))

     (testing "type hint metadata is inferred (sequential destructuring)"
       (let [string-hinted (->> specimen-1-macroexpansion
                                (second)
                                (filter (comp #{'alternative-destructuring} first))
                                first
                                second
                                ffirst
                                first)]
         (is (meta= string-hinted
                    (with-meta 'x {:tag String})))))
     (testing "type hint metadata is inferred (fn name hinting, argv hinting)"
       (are [input] (meta= (->> specimen-1-macroexpansion
                                (second)
                                (filter (comp #{input} first))
                                first
                                second
                                first)
                           (with-meta '[a] {:tag String}))
         'hinted-name
         'hinted-argv))))

(deftest correct-execution
  (is (= [["a" 2 ::anything]
          ["a" 2 ::anything]
          ["a" 2]
          ["a" 2 ::anything]]
         (letfn-specimen-1)))
  (is (= ["a string"]
         (letfn-specimen-4)))
  (is (= "Hello"
         (sut/letfn [(^string? hinted-name [a]
                       a)]
           (hinted-name "Hello"))))
  (is (= "Bye"
         (sut/letfn [(hinted-argv ^string? [a]
                       a)]
           (hinted-argv "Bye")))))

(def validation-failed #"Validation failed")

(deftest assertions-are-checked
  (are [specimen] (thrown-with-msg? #?(:clj Exception :cljs js/Error)
                                    validation-failed
                                    (with-out-str
                                      (specimen)))
    letfn-specimen-2
    letfn-specimen-3
    letfn-specimen-5
    (fn []
      (sut/letfn [(^string? hinted-name [a]
                    a)]
        (hinted-name :not-a-string)))
    (fn []
      (sut/letfn [(hinted-argv ^string? [a]
                    a)]
        (hinted-argv :not-a-string)))))
