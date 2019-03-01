(ns unit.nedap.utils.speced.defprotocol.type-hinting.inline-function-specs
  "This ns exercises the type hint emission of `#'nedap.utils.speced/defprotocol`."
  (:refer-clojure :exclude [defprotocol])
  (:require
   [clojure.test :refer :all]
   [nedap.utils.speced :as speced]))

(speced/defprotocol ExampleProtocol
  "Docstring"
  (^int? do-it
    [this
     ^boolean? boolean]
    "Docstring"))

(clojure.core/defprotocol PlainProtocol
  (plain-do-it
    [this
     boolean]))

(defrecord Sut []
  ExampleProtocol
  (--do-it [this x]
    (if (true? x)
      (Integer. 42)
      :fail)))

(defrecord Bad []
  ExampleProtocol
  (--do-it [this x]
    (if (true? x)
      2.0
      :fail)))

(deftest defprotocol
  (testing "Inline function specs don't result in :tag metadata being emitted."
    (are [x] (-> x meta :tag nil?)
      #'do-it
      #'--do-it
      #'plain-do-it)

    (are [x] (-> x meta :arglists first second meta :tag nil?)
      #'do-it
      #'--do-it
      #'plain-do-it)))
