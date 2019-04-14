(ns unit.nedap.utils.speced.defprotocol.type-hinting
  "This ns exercises the type hint emission of `#'nedap.utils.speced/defprotocol`."
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

(speced/defprotocol ExampleProtocol--AltRetValSyntax
  "Uses an alternative syntax for the type hinting."
  (alt-do-it
    ^Integer
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
    (if (true? x)
      (Integer. 42)
      :fail))
  ExampleProtocol--AltRetValSyntax
  (--alt-do-it [this x]
    (if (true? x)
      (Integer. 42)
      :fail)))

(defrecord Bad []
  ExampleProtocol
  (--do-it [this x]
    (if (true? x)
      2.0
      :fail))
  ExampleProtocol--AltRetValSyntax
  (--alt-do-it [this x]
    (if (true? x)
      2.0
      :fail)))

(deftest defprotocol
  (testing "Emitted type hints match clojure.core/defprotocol's behavior"
    (are [x] (-> x meta :tag #{'Integer 'java.lang.Integer})
      #'do-it
      #'alt-do-it
      #'--do-it
      #'--alt-do-it
      #'plain-do-it)
    (are [x] (-> x meta :arglists first second meta :tag #{'Boolean})
      #'do-it
      #'alt-do-it
      #'--do-it
      #'--alt-do-it
      #'plain-do-it))

  (is (= 42 (do-it (->Sut) true)))
  (is (= 42 (alt-do-it (->Sut) true)))

  (is (thrown? Exception (with-out-str
                           (do-it (Bad.) true))))
  (is (thrown? Exception (with-out-str
                           (alt-do-it (Bad.) true))))

  (is (thrown? Exception (with-out-str
                           (-> (->Sut) (do-it :not-a-boolean)))))
  (is (thrown? Exception (with-out-str
                           (-> (->Sut) (alt-do-it :not-a-boolean)))))

  (is (thrown? Exception (with-out-str
                           (-> (->Sut) (do-it false))))
      "`false` will cause the method not to return an int")
  (is (thrown? Exception (with-out-str
                           (-> (->Sut) (alt-do-it false))))
      "`false` will cause the method not to return an int"))
