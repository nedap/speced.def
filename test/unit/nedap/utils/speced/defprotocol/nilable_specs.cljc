(ns unit.nedap.utils.speced.defprotocol.nilable-specs
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.test :refer [deftest testing are is use-fixtures]]
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

#?(:clj
   (sut/defprotocol TypeHinted
     ""
     (^::sut/nilable ^String type-hinted [_ ^::sut/nilable ^String x] "")))

#?(:clj
   (sut/defprotocol TypeHinted2
     ""
     (type-hinted2 ^::sut/nilable ^String [_ ^::sut/nilable ^String x] "")))

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
  ^{`--inline                impl
    `--concise               impl
    #?(:clj `--type-hinted)  #?(:clj impl)
    `--explicit              impl
    `--inline2               impl
    `--concise2              impl
    #?(:clj `--type-hinted2) #?(:clj impl)
    `--explicit2             impl}
  {})

(deftest parsing
  (doseq [f [inline concise #?(:clj type-hinted) explicit
             inline2 concise2 #?(:clj type-hinted2) explicit2]]
    (testing f
      (are [arg ret] (= ret (f obj arg))
        "x" "x"
        nil nil)

      (are [arg] (thrown-with-msg? #?(:clj clojure.lang.ExceptionInfo :cljs cljs.core.ExceptionInfo) #"Validation failed" (f obj arg))
        :not-a-nilable-string
        "return-an-int!"))))
