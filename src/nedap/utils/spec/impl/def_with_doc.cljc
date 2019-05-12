(ns nedap.utils.spec.impl.def-with-doc
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   [nedap.utils.spec.api :refer [check!]])
  #?(:cljs (:require-macros [nedap.utils.spec.impl.def-with-doc])))

#?(:clj
   (defmacro def-with-doc
     [spec-name docstring spec]
     {:pre [(check! qualified-keyword? spec-name
                    string?            docstring
                    some?              spec)]}
     (list (if (-> &env :ns some?)
             'cljs.spec.alpha/def
             'clojure.spec.alpha/def)
           spec-name
           spec)))
