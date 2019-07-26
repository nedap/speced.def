(ns nedap.speced.def.test-runner
  (:require
   [cljs.nodejs :as nodejs]
   [cljs.test :refer-macros [run-tests]]
   [unit.nedap.speced.def.def-with-doc]
   [unit.nedap.speced.def.defn]
   [unit.nedap.speced.def.defn.destructuring]
   [unit.nedap.speced.def.defn.nilable-specs]
   [unit.nedap.speced.def.defn.parsing]
   [unit.nedap.speced.def.defn.pre-post]
   [unit.nedap.speced.def.defprotocol]
   [unit.nedap.speced.def.defprotocol.explicit-specs]
   [unit.nedap.speced.def.defprotocol.inline-function-specs]
   [unit.nedap.speced.def.defprotocol.multiple-arities]
   [unit.nedap.speced.def.defprotocol.nilable-specs]
   [unit.nedap.speced.def.defprotocol.type-hinting]
   [unit.nedap.speced.def.fn]
   [unit.nedap.speced.def.impl.parsing.instance-spec]
   [unit.nedap.speced.def.spec-assertion]))

(nodejs/enable-util-print!)

(defn -main []
  (run-tests
   'unit.nedap.speced.def.def-with-doc
   'unit.nedap.speced.def.defn
   'unit.nedap.speced.def.defn.destructuring
   'unit.nedap.speced.def.defn.nilable-specs
   'unit.nedap.speced.def.defn.parsing
   'unit.nedap.speced.def.defn.pre-post
   'unit.nedap.speced.def.defprotocol
   'unit.nedap.speced.def.defprotocol.explicit-specs
   'unit.nedap.speced.def.defprotocol.inline-function-specs
   'unit.nedap.speced.def.defprotocol.multiple-arities
   'unit.nedap.speced.def.defprotocol.nilable-specs
   'unit.nedap.speced.def.defprotocol.type-hinting
   'unit.nedap.speced.def.fn
   'unit.nedap.speced.def.impl.parsing.instance-spec
   'unit.nedap.speced.def.spec-assertion))

(set! *main-cli-fn* -main)
