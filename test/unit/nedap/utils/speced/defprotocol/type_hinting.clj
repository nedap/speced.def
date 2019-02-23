(ns unit.nedap.utils.speced.defprotocol.type-hinting
  "This ns exercises the type hint emmission of `#'nedap.utils.speced/defprotocol`."
  (:refer-clojure :exclude [defprotocol])
  (:require
   [clojure.test :refer :all]
   [nedap.utils.speced :as speced]))

(speced/defprotocol ExampleProtocol
  "Docstring"
  (^Integer do-it
    [this
     ^Boolean boolean]
    "Docstring"))

(clojure.core/defprotocol PlainProtocol
  (^Integer plain-do-it
    [this
     ^Boolean boolean]))

(defrecord Sut []
  ExampleProtocol
  (--do-it [this x]
    (if x
      (Integer. 42)
      :fail)))

(defrecord Bad []
  ExampleProtocol
  (--do-it [this x]
    (if x
      2.0
      :fail)))

(deftest defprotocol
  (testing "Emitted type hints match clojure.core/defprotocol's behavior"
    (are [x] (-> x meta :tag #{'Integer 'java.lang.Integer})
      #'do-it
      #'--do-it
      #'plain-do-it)
    (are [x] (-> x meta :arglists first second meta :tag #{'Boolean})
      #'do-it
      #'--do-it
      #'plain-do-it))

  (is (= 42 (do-it (Sut.) true)))

  (is (thrown? Exception (with-out-str
                           (do-it (Bad.) true))))

  (is (thrown? Exception (with-out-str
                           (-> (Sut.) (do-it :not-a-boolean)))))

  (is (thrown? Exception (with-out-str
                           (-> (Sut.) (do-it false))))
      "`false` will cause the method not to return an int"))
