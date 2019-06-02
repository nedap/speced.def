(ns unit.nedap.utils.speced.defn.parsing
  (:require
   #?(:cljs [cljs.core :refer [Atom]])
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.utils.speced :as sut])
  (:import
   #?(:clj (clojure.lang Atom))))

(sut/defn one
  "Docstring"
  {:meta :omg}
  [x ^Atom a]
  (println 3000)
  (reset! a (* 42 x)))

(sut/defn two
  "Docstring"
  [x ^Atom a]
  (println 3000)
  (reset! a (* 42 x)))

(sut/defn three
  "Docstring"
  {:meta :omg}
  ([x ^Atom a]
   (println 3000)
   (reset! a (* 42 x))))

(sut/defn four
  "Docstring"
  ([x ^Atom a]
   (println 3000)
   (reset! a (* 42 x))))

(deftest parsing
  (are [x] (= "Docstring"
              (-> x meta :doc))
    #'one
    #'two
    #'three
    #'four)

  (are [x] (= :omg
              (-> x meta :meta))
    #'one
    #'three)

  (are [x] (= "3000\n"
              (with-out-str
                (x 21 (atom nil))))
    one
    two
    three
    four)

  (are [x] (let [a (atom nil)]
             (with-out-str
               (x 21 a))
             (= 882 @a))
    one
    two
    three
    four))
