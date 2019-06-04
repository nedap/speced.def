(ns unit.nedap.utils.speced.defn.destructuring
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [clojure.string :as string]
   [nedap.utils.spec.api :refer [check!]]
   [nedap.utils.spec.impl.parsing :as impl.parsing]
   [nedap.utils.speced :as sut]
   [unit.nedap.test-helpers :refer [every-and-at-least-one?]])
  #?(:cljs (:require-macros [unit.nedap.utils.speced.defn.destructuring :refer [the-defns]])))

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
           xs {:simple-arity (if clj?
                               '(sut/defn
                                  simple-arity
                                  [{:keys [^::age age
                                           ^{::sut/spec ::temperature} temperature
                                           ^long length
                                           ^String name
                                           ^boolean? cool?]}]
                                  [age temperature length name cool?])

                               '(sut/defn
                                  simple-arity
                                  [{:keys [^::age age
                                           ^{::sut/spec ::temperature} temperature
                                           ^number length
                                           ^js/String name
                                           ^boolean? cool?]}]
                                  [age temperature length name cool?]))

               :two-arities  (if clj?
                               '(sut/defn
                                  two-arities
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
                                   [cool? name length temperature age]))

                               '(sut/defn
                                  two-arities
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
                                   [cool? name length temperature age])))}]
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
         simple-arity-macroexpansion '(def simple-arity
                                        (clojure.core/fn
                                          ([{:keys [age temperature length name cool?]}]
                                           {:pre  [(nedap.utils.spec.api/check!

                                                    :unit.nedap.utils.speced.defn.destructuring/age
                                                    age

                                                    :unit.nedap.utils.speced.defn.destructuring/temperature
                                                    temperature

                                                    (fn [x]
                                                      (clojure.core/instance? java.lang.Long x))
                                                    length

                                                    (fn [x]
                                                      (clojure.core/instance? String x))
                                                    name

                                                    (clojure.spec.alpha/and boolean?
                                                                            (fn [x]
                                                                              (clojure.core/instance? java.lang.Boolean x)))
                                                    cool?)],
                                            :post []}
                                           [age temperature length name cool?])))

         two-arities-macroexpansion  '(def two-arities
                                        (clojure.core/fn
                                          ([{:keys [age temperature length name cool?]}]
                                           {:pre  [(nedap.utils.spec.api/check!

                                                    :unit.nedap.utils.speced.defn.destructuring/age
                                                    age

                                                    :unit.nedap.utils.speced.defn.destructuring/temperature
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
                                                    cool?)],
                                            :post []}
                                           [age temperature length name cool?])

                                          ([{:keys [age temperature]} {:keys [length name cool?]}]
                                           {:pre  [(nedap.utils.spec.api/check!

                                                    :unit.nedap.utils.speced.defn.destructuring/age
                                                    age

                                                    :unit.nedap.utils.speced.defn.destructuring/temperature
                                                    temperature

                                                    (fn [x]
                                                      (clojure.core/instance? java.lang.Long x))
                                                    length

                                                    (fn [x]
                                                      (clojure.core/instance? String x))
                                                    name

                                                    (clojure.spec.alpha/and boolean?
                                                                            (fn [x]
                                                                              (clojure.core/instance? java.lang.Boolean x)))
                                                    cool?)],
                                            :post []}
                                           [cool? name length temperature age])))))))

(deftest correct-execution
  (testing "Arity 1"
    (are [f] (= [1 1.0 2 "n" false]
                (f {:age 1 :temperature 1.0 :length 2 :name "n" :cool? false}))
      simple-arity
      two-arities))

  (testing "Arity 2"
    (are [f] (= [false "n" 2 1.0 1]
                (f {:age 1 :temperature 1.0 :length 2 :name "n" :cool? false}
                   {:age 1 :temperature 1.0 :length 2 :name "n" :cool? false}))
      two-arities)))

(def validation-failed #"Validation failed")

(deftest preconditions-are-checked
  (testing "Arity 1"
    (are [desc args] (testing desc
                       (is (thrown-with-msg? #?(:clj Exception :cljs js/Error)
                                             validation-failed
                                             (with-out-str
                                               (simple-arity args))))
                       (is (thrown-with-msg? #?(:clj Exception :cljs js/Error)
                                             validation-failed
                                             (with-out-str
                                               (two-arities args)))))
      "bad :age"
      {:age -1, :temperature 1.0, :length 2, :name "n", :cool? false}

      "bad :temperature"
      {:age -1, :temperature 1.0, :length 2, :name "n", :cool? false}

      "bad :length"
      {:age -1, :temperature 1.0, :length 2.0, :name 31, :cool? false}

      "bad :name"
      {:age -1, :temperature 1.0, :length 2, :name 31, :cool? false}

      "bad :cool?"
      {:age -1, :temperature 1.0, :length 2, :name 31, :cool? 2}))

  (testing "Arity 2"
    (are [desc arg1 arg2] (testing desc

                            (is (thrown-with-msg? #?(:clj Exception :cljs js/Error)
                                                  validation-failed
                                                  (with-out-str
                                                    (two-arities arg1 arg2)))))
      "bad :age"
      {:age -1, :temperature 1.0}, {:length 2, :name "n", :cool? false}

      "bad :temperature"
      {:age -1, :temperature 1.0}, {:length 2, :name "n", :cool? false}

      "bad :length"
      {:age -1, :temperature 1.0}, {:length 2.0, :name 31, :cool? false}

      "bad :name"
      {:age -1, :temperature 1.0}, {:length 2, :name 31, :cool? false}

      "bad :cool?"
      {:age -1, :temperature 1.0}, {:length 2, :name 31, :cool? 2})))

(deftest type-hint-emission

  (are [input] (= #?(:clj (list nil nil 'long String Boolean)
                     :cljs '(nil nil number string boolean))
                  (->> input meta :arglists first first :keys (map meta) (map :tag)))
    #'simple-arity
    #'two-arities)

  (are [input] (= #?(:clj (list '(nil nil) (list 'long String Boolean))
                     :cljs '((nil nil) (number string boolean)))
                  (->> input meta :arglists second (map (fn [arg]
                                                          (->> arg :keys (map meta) (map :tag))))))
    #'two-arities))
