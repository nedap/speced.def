(ns unit.nedap.utils.api
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.utils.spec.api :as sut]))

(def validation-failed #"Validation failed")

(deftest check!
  (let [f (fn [x y]
            {:pre  [(sut/check! int? x
                                boolean? y)]
             :post [(sut/check! string? %)]}
            (when y
              (str x)))]
    (is (f 42 true))
    (testing ":post"
      (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                                  (f 42 false)))))
    (testing ":pre"
      (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                                  (f :not-an-int true))))
      (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                                  (f 42 :not-a-boolean)))))))

(spec/def ::age int?)

(spec/def ::user (spec/keys :req-un [::age]))

(deftest coerce-map-indicating-invalidity
  (are [x y] (= y (sut/coerce-map-indicating-invalidity ::user
                                                        x))
    {:age "1"}   {:age 1}
    {:age "one"} {:age           "one"
                  ::sut/invalid? true}))
