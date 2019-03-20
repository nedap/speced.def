(ns unit.nedap.utils.speced.defn.nilable-specs
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer :all]
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

(sut/defn ^::sut/nilable ^String type-hinted
  [^::sut/nilable ^String x]
  (cond
    (nil? x)               nil
    (= "return-an-int!" x) 42
    :else                  x))

(sut/defn type-hinted2
  ^::sut/nilable ^String
  [^::sut/nilable ^String x]
  (cond
    (nil? x)               nil
    (= "return-an-int!" x) 42
    :else                  x))

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
  (doseq [f [inline concise type-hinted explicit
             inline2 concise2 type-hinted2 explicit2]]
    (testing f
      (are [arg ret] (= ret (f arg))
        "x" "x"
        nil nil)

      (are [arg] (thrown-with-msg? clojure.lang.ExceptionInfo #"Validation failed" (f arg))
        :not-a-nilable-string
        "return-an-int!"))))
