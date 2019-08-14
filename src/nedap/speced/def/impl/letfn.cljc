(ns nedap.speced.def.impl.letfn
  (:require
   [nedap.speced.def.impl.analysis :refer [process-name-and-tails]]
   [nedap.utils.spec.api :refer [check!]]))

(defn impl [clj? fnspecs body]
  {:pre [(check! boolean? clj?)]}
  (let [fnspecs (->> fnspecs
                     (mapv (fn [[fn-name & tail]]
                             (->> (process-name-and-tails {:name fn-name
                                                           :tail tail
                                                           :clj? clj?})
                                  (:tails)
                                  (cons fn-name)))))]
    `(letfn ~fnspecs ~@body)))
