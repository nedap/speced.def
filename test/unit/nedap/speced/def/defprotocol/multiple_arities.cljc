(ns unit.nedap.speced.def.defprotocol.multiple-arities
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.speced.def :as sut]))

(use-fixtures :once (fn [t]
                      (with-out-str
                        (t))))

(spec/def ::string string?)

(spec/def ::integer integer?)

(sut/defprotocol Inline
  ""
  (inline
    [_]
    [_ ^string? x]
    [_
     ^string? x
     ^integer? y]
    ""))

(sut/defprotocol Concise
  ""
  (concise
    [_]
    [_ ^::string x]
    [_
     ^::string x
     ^::integer y]
    ""))

#?(:clj
   (sut/defprotocol TypeHinted
     ""
     (type-hinted
       [_]
       [_ ^String x]
       [_
        ^String x
        ^Long y]
       "")))

#?(:cljs
   (sut/defprotocol TypeHinted
     ""
     (type-hinted
       [_]
       [_ ^js/String x]
       [_
        ^js/String x
        ^js/Number y]
       "")))

(sut/defprotocol Explicit
  ""
  (explicit
    [_]
    [_ ^{::sut/spec ::string} x]
    [_
     ^{::sut/spec ::string} x
     ^{::sut/spec ::integer} y]
    ""))

(defn impl [_ & [x y]]
  x)

(def obj
  ^{`--inline      impl
    `--concise     impl
    `--type-hinted impl
    `--explicit    impl}
  {})

(deftest parsing
  (doseq [f [inline concise type-hinted explicit]]
    (testing f
      (testing "Arity 1"
        (are [ret] (= ret (f obj))
          nil))
      (testing "Arity 2 - OK"
        (are [arg ret] (= ret (f obj arg))
          "x" "x"))
      (testing "Arity 2 - Spec failure"
        (are [arg] (thrown-with-msg? #?(:clj clojure.lang.ExceptionInfo :cljs cljs.core.ExceptionInfo) #"Validation failed" (f obj arg))
          :not-a-string))
      (testing "Arity 3 - OK"
        (are [arg ret] (= ret (f obj arg 42))
          "x" "x"))
      (testing "Arity 3 - Spec failure"
        (are [arg] (thrown-with-msg? #?(:clj clojure.lang.ExceptionInfo :cljs cljs.core.ExceptionInfo) #"Validation failed" (f obj arg :not-an-int))
          "x")))))
