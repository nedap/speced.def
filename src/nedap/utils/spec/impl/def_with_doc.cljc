(ns nedap.utils.spec.impl.def-with-doc
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:cljs [cljs.repl])
   [nedap.utils.spec.api :refer [check!]])
  #?(:cljs (:require-macros [nedap.utils.spec.impl.def-with-doc])))

#?(:clj
   (defmacro def-with-doc
     [spec-name docstring spec doc-registry symbol-doc-registry]
     {:pre [(check! qualified-keyword? spec-name
                    string?            docstring
                    some?              spec
                    (spec/and symbol?
                              resolve) doc-registry)]}
     (list 'do
           (list `swap! doc-registry `assoc spec-name docstring)
           (list `swap! symbol-doc-registry `conj (list `with-meta (list 'quote (symbol spec-name)) {:doc docstring}))
           (list (if (-> &env :ns some?)
                   'cljs.spec.alpha/def
                   'clojure.spec.alpha/def)
                 spec-name
                 spec))))
