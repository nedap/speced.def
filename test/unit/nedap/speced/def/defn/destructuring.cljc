(ns unit.nedap.speced.def.defn.destructuring
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [clojure.string :as string]
   [nedap.speced.def :as sut]
   [nedap.speced.def.impl.parsing :as impl.parsing]
   [nedap.utils.spec.api :refer [check!]]
   [unit.nedap.test-helpers :refer [every-and-at-least-one?]])
  #?(:cljs (:require-macros [unit.nedap.speced.def.defn.destructuring :refer [the-defns]])))

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
   (defmacro the-defns []
     (let [clj? (-> &env :ns nil?)
           xs {:arity-1 (if clj?
                          '(sut/defn
                             arity-1
                             [{:keys [^::age age
                                      ^{::sut/spec ::temperature} temperature
                                      ^long length
                                      ^String name
                                      ^boolean? cool?]}]
                             [age temperature length name cool?])

                          '(sut/defn
                             arity-1
                             [{:keys [^::age age
                                      ^{::sut/spec ::temperature} temperature
                                      ^number length
                                      ^js/String name
                                      ^boolean? cool?]}]
                             [age temperature length name cool?]))

               :arity-n (if clj?
                          '(sut/defn
                             arity-n
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
                             arity-n
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
         arity-1-macroexpansion '(def arity-1
                                   (clojure.core/fn
                                     ([{:keys [age temperature length name cool?]}]
                                      {:pre  [(nedap.utils.spec.api/check!

                                               :unit.nedap.speced.def.defn.destructuring/age
                                               age

                                               :unit.nedap.speced.def.defn.destructuring/temperature
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

         arity-n-macroexpansion '(def arity-n
                                   (clojure.core/fn
                                     ([{:keys [age temperature length name cool?]}]
                                      {:pre  [(nedap.utils.spec.api/check!

                                               :unit.nedap.speced.def.defn.destructuring/age
                                               age

                                               :unit.nedap.speced.def.defn.destructuring/temperature
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

                                               :unit.nedap.speced.def.defn.destructuring/age
                                               age

                                               :unit.nedap.speced.def.defn.destructuring/temperature
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
      arity-1
      arity-n))

  (testing "Arity 2"
    (are [f] (= [false "n" 2 1.0 1]
                (f {:age 1 :temperature 1.0 :length 2 :name "n" :cool? false}
                   {:age 1 :temperature 1.0 :length 2 :name "n" :cool? false}))
      arity-n)))

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

(deftest type-hint-emission

  (are [input] (= #?(:clj (list nil nil 'long String Boolean)
                     :cljs '(nil nil number string boolean))
                  (->> input meta :arglists first first :keys (map meta) (map :tag)))
    #'arity-1
    #'arity-n)

  (are [input] (= #?(:clj (list '(nil nil) (list 'long String Boolean))
                     :cljs '((nil nil) (number string boolean)))
                  (->> input meta :arglists second (map (fn [arg]
                                                          (->> arg :keys (map meta) (map :tag))))))
    #'arity-n))

#?(:clj
   (deftest wrong-metadata
     (testing "metadata can only be placed for symbols"
       (are [input] (try
                      (eval input)
                      false
                      (catch Exception e
                        (-> e .getCause .getMessage (string/includes? "Only symbols can be attached spec metadata"))))
         '(nedap.speced.def/defn wrong-metadata-1 [{:keys ^string? []}])

         '(nedap.speced.def/defn wrong-metadata-2 [^string? {:keys []}])

         '(nedap.speced.def/defn wrong-metadata-3 [& ^string? []])

         '(nedap.speced.def/defn wrong-metadata-4 [[[[[[^string? {:keys []}]]]]]])))))
