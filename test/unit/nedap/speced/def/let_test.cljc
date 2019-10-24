(ns unit.nedap.speced.def.let-test
  "This ns uses `-test` suffix for avoiding a Closure warning."
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.speced.def :as sut]
   [nedap.utils.spec.api :refer [check!]]
   [nedap.utils.test.api :refer [macroexpansion=]]
   [nedap.utils.test.api :refer [meta=]]
   [unit.nedap.test-helpers :refer [every-and-at-least-one?]])
  #?(:cljs (:require-macros [unit.nedap.speced.def.let-test :refer [let-specimen-1 let-specimen-2 let-specimen-3 let-specimen-4 let-specimen-5]])))

#?(:clj
   (defmacro let-specimen-1 []
     (if (-> &env :ns nil?)
       '(sut/let [^string? a "A string"
                  {:keys [^long b]} {:b 52}
                  not-speced :anything]
          [a b])
       '(sut/let [^string? a "A string"
                  {:keys [^number b]} {:b 52}
                  not-speced :anything]
          [a b]))))

#?(:clj
   (defmacro let-specimen-2 []
     (if (-> &env :ns nil?)
       '(sut/let [^string? a nil
                  {:keys [^long b]} {:b 52}
                  not-speced :anything]
          [a b])
       '(sut/let [^string? a nil
                  {:keys [^number b]} {:b 52}
                  not-speced :anything]
          [a b]))))

#?(:clj
   (defmacro let-specimen-3 []
     (if (-> &env :ns nil?)
       '(sut/let [^string? a "A string"
                  {:keys [^long b]} {:b "NaN"}
                  not-speced :anything]
          [a b])
       '(sut/let [^string? a "A string"
                  {:keys [^number b]} {:b "NaN"}
                  not-speced :anything]
          [a b]))))

#?(:clj
   (defmacro let-specimen-4 []
     '(sut/let [[^string? a & tail] ["a" 1 2 3]]
        [a tail])))

#?(:clj
   (defmacro let-specimen-5 []
     '(sut/let [[^string? a & tail] [:a 1 2 3]]
        [a tail])))

#?(:clj
   (defmacro macroexpansion-specimens []
     (let [xs {:specimen-1 (macroexpand-1 (macroexpand-1 '(let-specimen-1)))
               :specimen-4 (macroexpand-1 (macroexpand-1 '(let-specimen-4)))}]
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
       (let [the-let '(clojure.core/let [a "A string"
                                         G__440105 (nedap.utils.spec.api/check! (clojure.spec.alpha/and
                                                                                 string?
                                                                                 (fn [x]
                                                                                   (if (clojure.core/class? java.lang.String)
                                                                                     (clojure.core/instance? java.lang.String x)
                                                                                     (clojure.core/satisfies? java.lang.String x))))
                                                                                a)
                                         {:keys [b]} {:b 52}
                                         G__440105 (nedap.utils.spec.api/check! (fn [x]
                                                                                  (if (clojure.core/class? java.lang.Long)
                                                                                    (clojure.core/instance? java.lang.Long x)
                                                                                    (clojure.core/satisfies? java.lang.Long x)))
                                                                                b)
                                         not-speced :anything]
                        [a b])]
         (is (macroexpansion= the-let
                              specimen-1-macroexpansion))))
     (testing "It expands without checks if `#'*assert*` is false"
       (binding [*assert* false]
         (let [the-let '(sut/let [a "A string"
                                  {:keys [b]} {:b 52}
                                  not-speced :anything]
                          [a b])]
           (is (macroexpansion= the-let
                                (macroexpand-1 `(let-specimen-1)))))))
     (testing "type hint metadata is inferred (simple symbols, associative destructuring)"
       (let [[string-hinted
              _
              {[long-hinted] :keys}] (->> specimen-1-macroexpansion
                                          second
                                          (partition 2)
                                          (map first))]
         (is (meta= string-hinted
                    (with-meta 'a {:tag `String})))
         (is (meta= long-hinted
                    (with-meta 'b {:tag 'long})))))
     (testing "type hint metadata is inferred (sequential destructuring)"
       (let [string-hinted (->> specimen-4-macroexpansion second ffirst)]
         (is (meta= string-hinted
                    (with-meta 'a {:tag `String})))))))

(deftest correct-execution
  (is (= ["A string" 52]
         (let-specimen-1)))
  (is (= ["a" '(1 2 3)]
         (let-specimen-4))))

(def validation-failed #"Validation failed")

(deftest assertions-are-checked
  (are [specimen] (thrown-with-msg? #?(:clj Exception :cljs js/Error)
                                    validation-failed
                                    (with-out-str
                                      (specimen)))
    let-specimen-2
    let-specimen-3
    let-specimen-5))
