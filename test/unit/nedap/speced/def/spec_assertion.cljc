(ns unit.nedap.speced.def.spec-assertion
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.speced.def :as speced]
   [nedap.utils.spec.api :refer [check!]])
  #?(:cljs (:require-macros [unit.nedap.speced.def.spec-assertion :refer [when-not-u-s-1-1-0]])))

(spec/def ::age number?)

(speced/defn accepts-age [^::age x]
  x)

(speced/defn ^::age returns-age [x]
  x)

(speced/defn accepts-number [^number? x]
  x)

(speced/defn ^number? returns-number [x]
  x)

(def accepts-number-fn (speced/fn [^number? x]
                         x))

(def returns-number-fn (speced/fn ^number? returns-number-fn [x]
                         x))

(speced/defprotocol ExampleProtocol
  "Docstring"
  (accepts-string [this ^string? value]
    "Docstring")

  (^string?
    returns-string [this value]
    "Docstring"))

(defrecord ExampleRecord []
  ExampleProtocol
  (--accepts-string [this x]
    x)

  (--returns-string [this x]
    x))

(deftest spec-assertion-thrown?-defmethod
  (is (spec-assertion-thrown? 'string? (check! string? 123)))
  (is (spec-assertion-thrown? 'number? (check! number? "123")))
  (is (spec-assertion-thrown? ::age    (accepts-age "1234")))
  (is (spec-assertion-thrown? ::age    (returns-age "1234")))
  (is (spec-assertion-thrown? 'number? (accepts-number "1234")))
  (is (spec-assertion-thrown? 'number? (returns-number "1234")))
  (is (spec-assertion-thrown? 'number? (accepts-number-fn "1234")))
  (is (spec-assertion-thrown? 'number? (returns-number-fn "1234")))
  (is (spec-assertion-thrown? 'string? (accepts-string (->ExampleRecord) 1234)))
  (is (spec-assertion-thrown? 'string? (returns-string (->ExampleRecord) 1234))))

(defn parameterized-defn [spec x]
  (check! spec x)
  x)

#?(:clj (defmacro when-not-u-s-1-1-0
          {:style/indent 0}
          [& body]
          (when-not (-> "nedap.speced.def.testing.utils-spec-dep" System/getProperty #{"1.1.0"})
            `(do
               ~@body))))

(deftest parameterized-specs
  (when-not-u-s-1-1-0
    (testing "defns that receive specs as a parameter"
      (is (spec-assertion-thrown? ::age (parameterized-defn ::age "1234")))

      (is (spec-assertion-thrown? #{1 2 3} (parameterized-defn #{1 2 3} "1234")))

      (is (spec-assertion-thrown? number? (parameterized-defn number? "1234"))))))
