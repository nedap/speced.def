(ns nedap.speced.def.impl.let-impl
  "`-impl` suffix is there to avoid a Closure warning."
  (:require
   [nedap.speced.def.impl.analysis :refer [process-name-and-tails]]
   [nedap.utils.spec.api :refer [check!]]))

(defn add-assertion [binding assertion]
  {:pre [(check! #{0 1} (count assertion))]}
  (cond-> binding
    (seq assertion)
    (conj (gensym) (first assertion))))

(defn impl [clj? bindings body]
  {:pre [(check! boolean? clj?)]}
  (let [bindings (->> bindings
                      (partition 2)
                      (map (fn [[left right]]
                             (let [analysis-result (process-name-and-tails {:tail (list [left])
                                                                            :name nil
                                                                            :clj? clj?})
                                   argv (-> analysis-result :tails ffirst)
                                   _ (assert (check! vector? argv
                                                     #{1}    (count argv)))
                                   left (first argv)
                                   prepost (-> analysis-result :tails first second)
                                   _ (assert (check! map? prepost))
                                   assertion (-> prepost :pre)]
                               [left right assertion])))
                      (map (fn [[left right assertion]]
                             {:pre [(check! some? left)]}
                             (add-assertion [left right] assertion)))
                      (apply concat)
                      (vec))]
    `(let ~bindings ~@body)))
