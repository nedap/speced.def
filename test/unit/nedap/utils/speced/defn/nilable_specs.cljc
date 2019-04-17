(ns unit.nedap.utils.speced.defn.nilable-specs
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.utils.speced :as sut]))

(use-fixtures :once (fn [t]
                      (with-out-str
                        (t))))

(spec/def ::string string?)

(sut/defn ^::sut/nilable ^string? inline
  [^::sut/nilable ^string? x]
  (cond
    (nil? x)               nil
    (= "return-an-int!" x) 42
    :else                  x))

(sut/defn inline2
  ^::sut/nilable ^string?
  [^::sut/nilable ^string? x]
  (cond
    (nil? x)               nil
    (= "return-an-int!" x) 42
    :else                  x))

(sut/defn ^::sut/nilable ^::string concise
  [^::sut/nilable ^::string x]
  (cond
    (nil? x)               nil
    (= "return-an-int!" x) 42
    :else                  x))

(sut/defn concise2
  ^::sut/nilable ^::string
  [^::sut/nilable ^::string x]
  (cond
    (nil? x)               nil
    (= "return-an-int!" x) 42
    :else                  x))

#?(:clj
   (sut/defn ^::sut/nilable ^String type-hinted
     [^::sut/nilable ^String x]
     (cond
       (nil? x)               nil
       (= "return-an-int!" x) 42
       :else                  x)))

#?(:clj
   (sut/defn type-hinted2
     ^::sut/nilable ^String
     [^::sut/nilable ^String x]
     (cond
       (nil? x)               nil
       (= "return-an-int!" x) 42
       :else                  x)))

(sut/defn
  ^{::sut/spec    ::string
    ::sut/nilable true}
  explicit
  [^{::sut/spec ::string ::sut/nilable true} x]
  (cond
    (nil? x)               nil
    (= "return-an-int!" x) 42
    :else                  x))

(sut/defn
  explicit2
  ^{::sut/spec    ::string
    ::sut/nilable true}
  [^{::sut/spec ::string ::sut/nilable true} x]
  (cond
    (nil? x)               nil
    (= "return-an-int!" x) 42
    :else                  x))

(deftest parsing
  (doseq [f [inline concise #?(:clj type-hinted) explicit
             inline2 concise2 #?(:clj type-hinted2) explicit2]]
    (testing f
      (are [arg ret] (= ret (f arg))
        "x" "x"
        nil nil)

      (are [arg] (thrown-with-msg? #?(:clj clojure.lang.ExceptionInfo :cljs cljs.core.ExceptionInfo) #"Validation failed" (f arg))
        :not-a-nilable-string
        "return-an-int!"))))
