(ns nedap.utils.spec.test-runner
  (:require
   [cljs.nodejs :as nodejs]
   [cljs.test :refer-macros [run-tests]]
   [unit.nedap.utils.api]
   [unit.nedap.utils.speced.def-with-doc]
   [unit.nedap.utils.speced.defn]
   [unit.nedap.utils.speced.defn.destructuring]
   [unit.nedap.utils.speced.defn.nilable-specs]
   [unit.nedap.utils.speced.defn.parsing]
   [unit.nedap.utils.speced.defn.pre-post]
   [unit.nedap.utils.speced.defprotocol]
   [unit.nedap.utils.speced.defprotocol.explicit-specs]
   [unit.nedap.utils.speced.defprotocol.inline-function-specs]
   [unit.nedap.utils.speced.defprotocol.multiple-arities]
   [unit.nedap.utils.speced.defprotocol.nilable-specs]
   [unit.nedap.utils.speced.defprotocol.type-hinting]
   [unit.nedap.utils.speced.fn]
   [unit.nedap.utils.speced.predicates]))

(nodejs/enable-util-print!)

(defn -main []
  (run-tests
   'unit.nedap.utils.api
   'unit.nedap.utils.speced.def-with-doc
   'unit.nedap.utils.speced.defn
   'unit.nedap.utils.speced.defn.destructuring
   'unit.nedap.utils.speced.defn.nilable-specs
   'unit.nedap.utils.speced.defn.parsing
   'unit.nedap.utils.speced.defn.pre-post
   'unit.nedap.utils.speced.defprotocol
   'unit.nedap.utils.speced.defprotocol.explicit-specs
   'unit.nedap.utils.speced.defprotocol.inline-function-specs
   'unit.nedap.utils.speced.defprotocol.multiple-arities
   'unit.nedap.utils.speced.defprotocol.nilable-specs
   'unit.nedap.utils.speced.defprotocol.type-hinting
   'unit.nedap.utils.speced.fn
   'unit.nedap.utils.speced.predicates))

(set! *main-cli-fn* -main)
