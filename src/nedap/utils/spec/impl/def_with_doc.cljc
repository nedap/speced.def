(ns nedap.utils.spec.impl.def-with-doc
  (:require
   [clojure.core.protocols]
   #?(:cljs [cljs.repl])
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   [nedap.utils.spec.api :refer [check!]])
  #?(:cljs (:require-macros [nedap.utils.spec.impl.def-with-doc]))
  #?(:clj (:import (java.io Writer))))

(defrecord Docstring [docstring]
  clojure.core.protocols/Datafiable
  (datafy [_]
    docstring))

#?(:clj
   (defmethod print-method Docstring
     [this ^Writer writer]
     (let [^String docstring (-> this :docstring)]
       (-> writer (.write docstring)))))

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
           (list `swap! symbol-doc-registry `assoc (list 'quote (symbol spec-name)) (list `map->Docstring {:docstring docstring}))
           (list (if (-> &env :ns some?)
                   'cljs.spec.alpha/def
                   'clojure.spec.alpha/def)
                 spec-name
                 spec))))
