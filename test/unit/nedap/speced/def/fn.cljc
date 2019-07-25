(ns unit.nedap.speced.def.fn
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [clojure.string :as string]
   [nedap.speced.def :as sut]
   [nedap.speced.def.impl.parsing :as impl.parsing]
   [nedap.utils.spec.api :refer [check!]]
   [nedap.utils.test.api :refer [macroexpansion=]]
   [unit.nedap.test-helpers :refer [every-and-at-least-one?]])
  #?(:cljs (:require-macros [unit.nedap.speced.def.fn :refer [the-fns]])))

(spec/def ::age pos?)

(spec/def ::temperature (spec/and #?(:clj  double?
                                     :cljs number?)
                                  pos?))

(spec/def ::name (spec/and string? (fn [x]
                                     (-> x count (< 10)))))

(defn present? [x]
  (some? x))

(spec/def ::present? present?)

#?(:clj
   (defmacro the-fns []
     (let [clj? (-> &env :ns nil?)
           xs {:arity-1               (if clj?
                                        '(def arity-1 (sut/fn
                                                        [{:keys [^::age age
                                                                 ^{::sut/spec ::temperature} temperature
                                                                 ^long length
                                                                 ^String name
                                                                 ^boolean? cool?]}]
                                                        [age temperature length name cool?]))

                                        '(def arity-1 (sut/fn
                                                        [{:keys [^::age age
                                                                 ^{::sut/spec ::temperature} temperature
                                                                 ^number length
                                                                 ^js/String name
                                                                 ^boolean? cool?]}]
                                                        [age temperature length name cool?])))

               :arity-n               (if clj?
                                        '(def arity-n (sut/fn
                                                        ([{:keys [^::age age
                                                                  ^{::sut/spec ::temperature} temperature
                                                                  ^long length
                                                                  ^String name
                                                                  ^boolean? cool?]}]
                                                         [age temperature length name cool?])

                                                        ([{:keys [^::age age
                                                                  ^{::sut/spec ::temperature} temperature]}
                                                          {:keys [^long length
                                                                  ^String name
                                                                  ^boolean? cool?]}]
                                                         [cool? name length temperature age])))

                                        '(def arity-n (sut/fn

                                                        ([{:keys [^::age age
                                                                  ^{::sut/spec ::temperature} temperature
                                                                  ^number length
                                                                  ^js/String name
                                                                  ^boolean? cool?]}]
                                                         [age temperature length name cool?])

                                                        ([{:keys [^::age age
                                                                  ^{::sut/spec ::temperature} temperature]}
                                                          {:keys [^number length
                                                                  ^js/String name
                                                                  ^boolean? cool?]}]
                                                         [cool? name length temperature age]))))

               :spec-in-name          '(def spec-in-name (sut/fn ^string? spec-in-name-xxx
                                                           [x]
                                                           x))

               :spec-in-argv          '(def spec-in-argv (sut/fn spec-in-argv-xxx
                                                           ^string? [x]
                                                           x))

               :spec-in-argv-no-name  '(def spec-in-argv-no-name (sut/fn
                                                                   ^string? [x]
                                                                   x))

               :spec-in-name-and-argv '(def spec-in-name-and-argv (sut/fn ^string? spec-in-name-and-argv-xxx
                                                                    ^some? [x]
                                                                    x))}]
       (cond->> xs
         clj? (map (fn [[k v]]
                     [k (list 'quote v)]))
         true (into {})))))

#?(:clj (doseq [[k v] (the-fns)]
          (eval v)
          (eval `(def ~(-> k
                           name
                           (str "-macroexpansion")
                           symbol)
                   ~(list 'quote (macroexpand (last (macroexpand v))))))))

#?(:cljs (the-fns))

#?(:clj
   (deftest macroexpansion
     (testing "It macroexpands to known-good (and evidently-good) forms"
       (are [input expected] (macroexpansion= expected input)
         arity-1-macroexpansion               '(fn*
                                                ([p__196417]
                                                 (clojure.core/let
                                                  [{:keys [age temperature length name cool?]} p__196417]
                                                   (clojure.core/assert
                                                    (nedap.utils.spec.api/check!

                                                     :unit.nedap.speced.def.fn/age
                                                     age

                                                     :unit.nedap.speced.def.fn/temperature
                                                     temperature

                                                     (fn [x]
                                                       (clojure.core/instance? java.lang.Long x))
                                                     length

                                                     (fn [x]
                                                       (clojure.core/instance? String x))
                                                     name

                                                     (clojure.spec.alpha/and
                                                      boolean?
                                                      (fn [x]
                                                        (clojure.core/instance? java.lang.Boolean x)))
                                                     cool?))
                                                   (clojure.core/let [% [age temperature length name cool?]]
                                                     %))))

         arity-n-macroexpansion               '(fn*
                                                ([p__196485]
                                                 (clojure.core/let
                                                  [{:keys [age temperature length name cool?]} p__196485]
                                                   (clojure.core/assert
                                                    (nedap.utils.spec.api/check!

                                                     :unit.nedap.speced.def.fn/age
                                                     age

                                                     :unit.nedap.speced.def.fn/temperature
                                                     temperature

                                                     (fn [x]
                                                       (clojure.core/instance? java.lang.Long x))
                                                     length

                                                     (fn [x]
                                                       (clojure.core/instance? String x))
                                                     name

                                                     (clojure.spec.alpha/and
                                                      boolean?
                                                      (fn [x]
                                                        (clojure.core/instance? java.lang.Boolean x)))
                                                     cool?))
                                                   (clojure.core/let [% [age temperature length name cool?]]
                                                     %)))

                                                ([p__196486 p__196487]
                                                 (clojure.core/let
                                                  [{:keys [age temperature]} p__196486
                                                   {:keys [length name cool?]} p__196487]
                                                   (clojure.core/assert
                                                    (nedap.utils.spec.api/check!

                                                     :unit.nedap.speced.def.fn/age
                                                     age

                                                     :unit.nedap.speced.def.fn/temperature
                                                     temperature

                                                     (fn [x]
                                                       (clojure.core/instance? java.lang.Long x))
                                                     length

                                                     (fn [x]
                                                       (clojure.core/instance? String x))
                                                     name

                                                     (clojure.spec.alpha/and
                                                      boolean?
                                                      (fn [x]
                                                        (clojure.core/instance? java.lang.Boolean x)))
                                                     cool?))
                                                   (clojure.core/let [% [cool? name length temperature age]]
                                                     %))))

         spec-in-name-macroexpansion          '(fn* spec-in-name-xxx
                                                    ([x]
                                                     (clojure.core/let [% x]
                                                       (clojure.core/assert (nedap.utils.spec.api/check!
                                                                             (clojure.spec.alpha/and string?
                                                                                                     (fn [x]
                                                                                                       (clojure.core/instance? java.lang.String x)))
                                                                             %))
                                                       %)))

         spec-in-argv-macroexpansion          '(fn* spec-in-argv-xxx
                                                    ([x]
                                                     (clojure.core/let [% x]
                                                       (clojure.core/assert (nedap.utils.spec.api/check!
                                                                             (clojure.spec.alpha/and string?
                                                                                                     (fn [x]
                                                                                                       (clojure.core/instance? java.lang.String x))) %))
                                                       %)))

         spec-in-argv-no-name-macroexpansion  '(fn*
                                                ([x]
                                                 (clojure.core/let [% x]
                                                   (clojure.core/assert (nedap.utils.spec.api/check!
                                                                         (clojure.spec.alpha/and string?
                                                                                                 (fn [x]
                                                                                                   (clojure.core/instance? java.lang.String x)))
                                                                         %))
                                                   %)))

         spec-in-name-and-argv-macroexpansion '(fn* spec-in-name-and-argv-xxx
                                                    ([x]
                                                     (clojure.core/let [% x]
                                                       (clojure.core/assert
                                                        (nedap.utils.spec.api/check!
                                                         (clojure.spec.alpha/and string?
                                                                                 (fn [x]
                                                                                   (clojure.core/instance? java.lang.String x)))
                                                         %))
                                                       (clojure.core/assert (nedap.utils.spec.api/check! some? %))
                                                       %)))))))

(deftest correct-execution
  (testing "fns that exercise preconditions. Arity 1"
    (are [f] (= [1 1.0 2 "n" false]
                (f {:age 1 :temperature 1.0 :length 2 :name "n" :cool? false}))
      arity-1
      arity-n))

  (testing "fns that exercise preconditions. Arity 2"
    (are [f] (= [false "n" 2 1.0 1]
                (f {:age 1 :temperature 1.0 :length 2 :name "n" :cool? false}
                   {:age 1 :temperature 1.0 :length 2 :name "n" :cool? false}))
      arity-n))

  (testing "fns that exercise postconditions"
    (are [f] (is (= "a" (f "a")))
      spec-in-name
      spec-in-argv
      spec-in-argv-no-name
      spec-in-name-and-argv)))

(def validation-failed #"Validation failed")

(deftest preconditions-are-checked
  (testing "Arity 1"
    (let [correct-values {:age 1, :temperature 1.0, :length 2, :name "n", :cool? false}]
      (are [desc args] (testing desc
                         (is (thrown-with-msg? #?(:clj Exception :cljs js/Error)
                                               validation-failed
                                               (with-out-str
                                                 (arity-1 args))))
                         (is (thrown-with-msg? #?(:clj Exception :cljs js/Error)
                                               validation-failed
                                               (with-out-str
                                                 (arity-n args)))))
        "bad :age"
        (assoc correct-values :age -1)

        "bad :temperature"
        (assoc correct-values :temperature -1.0)

        "bad :length"
        (assoc correct-values :length #?(:clj  2.0
                                         :cljs "2.0"))

        "bad :name"
        (assoc correct-values :name 31)

        "bad :cool?"
        (assoc correct-values :cool? 2))))

  (testing "Arity 2"
    (let [correct-arg1 {:age 1, :temperature 1.0}
          correct-arg2 {:length 2, :name "n", :cool? false}]
      (are [desc arg1 arg2] (testing desc
                              (is (thrown-with-msg? #?(:clj Exception :cljs js/Error)
                                                    validation-failed
                                                    (with-out-str
                                                      (arity-n arg1 arg2)))))
        "bad :age"
        (assoc correct-arg1 :age -1)           correct-arg2

        "bad :temperature"
        (assoc correct-arg1 :temperature -1.0) correct-arg2

        "bad :length"
        correct-arg1                           (assoc correct-arg2 :length #?(:clj  2.0
                                                                              :cljs "2.0"))

        "bad :name"
        correct-arg1                           (assoc correct-arg2 :name 31)

        "bad :cool?"
        correct-arg1                           (assoc correct-arg2 :cool? 2)))))

(deftest postconditions-are-checked
  (are [f] (is (thrown-with-msg? #?(:clj Exception :cljs js/Error)
                                 validation-failed
                                 (with-out-str
                                   (f nil))))
    spec-in-name
    spec-in-argv
    spec-in-argv-no-name
    spec-in-name-and-argv))
