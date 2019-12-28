(ns nedap.speced.def.impl.spec-assertion.jvm
  (:require
   [clojure.test :as test]
   [nedap.speced.def.impl.spec-assertion :as impl.spec-assertion]))

(defmethod test/assert-expr 'spec-assertion-thrown? [msg form]
  (clojure.core/let [spec-sym (second form)
                     body     (nthnext form 2)]
    (impl.spec-assertion/spec-assertion-thrown? true msg spec-sym body)))
