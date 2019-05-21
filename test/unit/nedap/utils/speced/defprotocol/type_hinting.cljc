(ns unit.nedap.utils.speced.defprotocol.type-hinting
  "This ns exercises the type hint emission of `#'nedap.utils.speced/defprotocol`."
  (:refer-clojure :exclude [defprotocol])
  (:require
   [clojure.test :refer [are deftest is testing]]
   [nedap.utils.speced :as speced]))

#?(:clj  (speced/defprotocol ExampleProtocol
           "Docstring"
           (^Double do-it
            [this
             ^Boolean boolean]
            "Docstring"))

   :cljs (speced/defprotocol ExampleProtocol
           "Docstring"
           (^js/Number do-it
            [this
             ^js/Boolean boolean]
            "Docstring")))

#?(:clj  (speced/defprotocol ExampleProtocol--AltRetValSyntax
           "Uses an alternative syntax for the type hinting."
           (alt-do-it
             ^Double
             [this
              ^Boolean boolean]
             "Docstring"))

   :cljs (speced/defprotocol ExampleProtocol--AltRetValSyntax
           "Uses an alternative syntax for the type hinting."
           (alt-do-it
             ^js/Number
             [this
              ^js/Boolean boolean]
             "Docstring")))

#?(:clj  (speced/defprotocol ExampleProtocol--FnSyntax
           "Uses 'function' syntax for the type hinting."
           (fn-do-it
             ^double?
             [this
              ^boolean? boolean]
             "Docstring"))

   :cljs (speced/defprotocol ExampleProtocol--FnSyntax
           "Uses 'function' syntax for the type hinting."
           (fn-do-it
             ^number?
             [this
              ^boolean? boolean]
             "Docstring")))

#?(:clj  (speced/defprotocol ExampleProtocol--FnSyntax--Alt
           "Uses 'function' syntax for the type hinting, in an alternative position."
           (^double? fn-do-it-alt
            [this
             ^boolean? boolean]
            "Docstring"))

   :cljs (speced/defprotocol ExampleProtocol--FnSyntax--Alt
           "Uses 'function' syntax for the type hinting, in an alternative position."
           (^number? fn-do-it-alt
            [this
             ^boolean? boolean]
            "Docstring")))

#?(:clj  (clojure.core/defprotocol PlainProtocol
           (^Double plain-do-it
            [this
             ^Boolean boolean]))

   :cljs (cljs.core/defprotocol PlainProtocol
           (^js/Number plain-do-it
            [this
             ^js/Boolean boolean])))

(defn good-impl [x]
  (if (true? x)
    (#?(:clj  Double.
        :cljs js/Number) 42.0)
    :fail))

(defrecord Sut []
  ExampleProtocol
  (--do-it [this x]
    (good-impl x))

  ExampleProtocol--AltRetValSyntax
  (--alt-do-it [this x]
    (good-impl x))

  ExampleProtocol--FnSyntax
  (--fn-do-it [this x]
    (good-impl x))

  ExampleProtocol--FnSyntax--Alt
  (--fn-do-it-alt [this x]
    (good-impl x)))

(defn bad-impl [x]
  :fail)

(defrecord Bad []
  ExampleProtocol
  (--do-it [this x]
    (bad-impl x))

  ExampleProtocol--AltRetValSyntax
  (--alt-do-it [this x]
    (bad-impl x))

  ExampleProtocol--FnSyntax
  (--fn-do-it [this x]
    (bad-impl x))

  ExampleProtocol--FnSyntax--Alt
  (--fn-do-it-alt [this x]
    (bad-impl x)))

(def validation-failed #"Validation failed")

(deftest defprotocol
  #?(:clj
     (testing "Emitted type hints match clojure.core/defprotocol's behavior"
       (are [x] (-> x meta :tag #?(:clj #{'Double `Double}
                                   :cljs #{'js/Number}))
         #'do-it
         #'alt-do-it
         #'fn-do-it
         #'fn-do-it-alt
         #'--do-it
         #'--alt-do-it
         #'--fn-do-it
         #'--fn-do-it-alt
         #'plain-do-it)
       (are [x] (-> x meta :arglists first second meta :tag #?(:clj #{'Boolean `Boolean}
                                                               :cljs #{'js/Boolean}))
         #'do-it
         #'alt-do-it
         #'fn-do-it
         #'fn-do-it-alt
         #'--do-it
         #'--alt-do-it
         #'--fn-do-it
         #'--fn-do-it-alt
         #'plain-do-it)))

  (is (= 42.0 (do-it (->Sut) true)))
  (is (= 42.0 (alt-do-it (->Sut) true)))
  (is (= 42.0 (fn-do-it (->Sut) true)))
  (is (= 42.0 (fn-do-it-alt (->Sut) true)))

  (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                              (do-it (Bad.) true))))
  (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                              (alt-do-it (Bad.) true))))
  (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                              (fn-do-it (Bad.) true))))
  (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                              (fn-do-it-alt (Bad.) true))))

  (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                              (-> (->Sut) (do-it :not-a-boolean)))))
  (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                              (-> (->Sut) (alt-do-it :not-a-boolean)))))
  (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                              (-> (->Sut) (fn-do-it :not-a-boolean)))))
  (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                              (-> (->Sut) (fn-do-it-alt :not-a-boolean)))))

  (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                              (-> (->Sut) (do-it false))))
      "`false` will cause the method not to return an int")
  (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                              (-> (->Sut) (alt-do-it false))))
      "`false` will cause the method not to return an int")
  (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                              (-> (->Sut) (fn-do-it false))))
      "`false` will cause the method not to return an int")
  (is (thrown-with-msg? #?(:clj Exception :cljs js/Error) validation-failed (with-out-str
                                                                              (-> (->Sut) (fn-do-it-alt false))))
      "`false` will cause the method not to return an int"))
