(ns nedap.speced.def.impl.spec-assertion.cljs
  (:require
   [cljs.test :as test]
   #?(:clj [nedap.speced.def.impl.spec-assertion :as impl.spec-assertion])))

#?(:clj
   (defmethod test/assert-expr 'spec-assertion-thrown? [_env msg form]
     (let [spec-sym (second form)
           body (nthnext form 2)]
       (impl.spec-assertion/spec-assertion-thrown? false msg spec-sym body))))
