(ns nedap.utils.foo
  (:refer-clojure :exclude [defn defprotocol])
  (:require
   [nedap.utils.speced :refer [defprotocol]]))

(defprotocol ExampleProtocolV
  "Docstring"
  (^int?
   do-it [^some?
          this
          ^boolean?
          boolean]
   "Docstring"))

#_ (do (declare XXdo-it) (clojure.core/defprotocol ExampleProtocolV "Docstring" :extend-via-metadata true (XXdo-it [this boolean] "Docstring")) (cljs.core/defn do-it "Docstring" ([this boolean] {:pre [(nedap.utils.spec.impl.check/check! some? this boolean? boolean)], :post [(nedap.utils.spec.impl.check/check! int? %)]} (XXdo-it this boolean))) ExampleProtocolV)

#_ (do
     (clojure.core/declare XXdo-it)
     (clojure.core/defprotocol ExampleProtocolV "Docstring" :extend-via-metadata true (XXdo-it [this boolean] "Docstring"))
     (cljs.core/defn do-it "Docstring" ([this boolean] {:pre [(nedap.utils.spec.impl.check/check! some? this boolean? boolean)], :post [(nedap.utils.spec.impl.check/check! int? %)]} (XXdo-it this boolean)))
     ExampleProtocolV)
