(ns unit.nedap.utils.speced.defprotocol.nilable-specs
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer :all]
   [nedap.utils.speced :as sut]))

(use-fixtures :once (fn [t]
                      (with-out-str
                        (t))))

(spec/def ::string string?)

(sut/defprotocol Inline
  ""
  (^::sut/nilable ^string? inline [_ ^::sut/nilable ^string? x] ""))

(sut/defprotocol Inline2
  ""
  (inline2 ^::sut/nilable ^string? [_ ^::sut/nilable ^string? x] ""))

(sut/defprotocol Concise
  ""
  (^::sut/nilable ^::string concise [_ ^::sut/nilable ^::string x] ""))

(sut/defprotocol Concise2
  ""
  (concise2 ^::sut/nilable ^::string [_ ^::sut/nilable ^::string x] ""))

(sut/defprotocol TypeHinted
  ""
  (^::sut/nilable ^String type-hinted [_ ^::sut/nilable ^String x] ""))

(sut/defprotocol TypeHinted2
  ""
  (type-hinted2 ^::sut/nilable ^String [_ ^::sut/nilable ^String x] ""))

(sut/defprotocol Explicit
  ""
  (^{::sut/spec    ::string
     ::sut/nilable true}
   explicit
   [_ ^{::sut/spec ::string ::sut/nilable true} x]
   ""))

(sut/defprotocol Explicit2
  ""
  (explicit2
    ^{::sut/spec    ::string
      ::sut/nilable true}
    [_ ^{::sut/spec ::string ::sut/nilable true} x]
    ""))

(defn impl [_ x]
  (cond
    (nil? x)               nil
    (= "return-an-int!" x) 42
    :else                  x))

(def obj
  ^{`--inline       impl
    `--concise      impl
    `--type-hinted  impl
    `--explicit     impl
    `--inline2      impl
    `--concise2     impl
    `--type-hinted2 impl
    `--explicit2    impl}
  {})

(deftest parsing
  (doseq [f [inline concise type-hinted explicit
             inline2 concise2 type-hinted2 explicit2]]
    (testing f
      (are [arg ret] (= ret (f obj arg))
        "x" "x"
        nil nil)

      (are [arg] (thrown-with-msg? clojure.lang.ExceptionInfo #"Validation failed" (f obj arg))
        :not-a-nilable-string
        "return-an-int!"))))
