(ns unit.nedap.speced.def.impl.parsing.instance-spec
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.speced.def.impl.parsing :as sut])
  #?(:cljs (:require-macros [unit.nedap.speced.def.impl.parsing.instance-spec :refer [instance-spec]])))

#?(:clj
   (defmacro instance-spec [class]
     (sut/instance-spec (-> &env :ns nil?)
                        class)))

(defprotocol Foo
  (foo [this]))

(spec/def ::string (instance-spec #?(:clj  String
                                     :cljs js/String)))

(spec/def ::foo (instance-spec Foo))

(deftest works
  (are [input spec expected] (= expected
                                (spec/valid? spec input))
    2               ::string false
    ""              ::string true

    ""              ::foo    false
    (reify Foo
      (foo [this])) ::foo    true))
