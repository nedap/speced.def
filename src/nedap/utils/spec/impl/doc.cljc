(ns nedap.utils.spec.impl.doc
  #?(:clj
     (:require
      [clojure.repl :refer [doc]]))
  #?(:cljs (:require-macros [cljs.repl :refer [doc]])))

(defn impl [x clj? doc-registry]
  (let [maybe-println (when-let [docstring (get @doc-registry x)]
                        (let [with-separator (str "\n" docstring)]
                          (list (if clj?
                                  `println
                                  'cljs.core/println)
                                with-separator)))]
    `(do
       (doc ~x)
       ~maybe-println)))
