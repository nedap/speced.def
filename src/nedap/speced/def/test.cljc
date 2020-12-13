(ns nedap.speced.def.test
  "Integration with clojure.test or cljs.test.

  Contains vars to be used with https://clojuredocs.org/clojure.test/assert-expr"
  (:require
   [nedap.speced.def]))

(def ^{:arglists '([spec & body])
       :doc "Asserts that evaluating expr throws an ExceptionInfo related to `spec-symbol`. Returns the exception thrown."}
  spec-assertion-thrown?)
