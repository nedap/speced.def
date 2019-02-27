(ns nedap.utils.spec.impl.parsing
  (:require
   [nedap.utils.spec.impl.check :refer [check!]]
   [nedap.utils.spec.specs :as specs]))

(defn proper-spec-metadata? [metadata-map extracted-specs]
  (if (-> extracted-specs count #{1})
    (check! ::specs/spec-metadata metadata-map)
    true))

(defn extract-specs-from-metadata [metadata-map]
  {:post [(check! #{0 1} (count %)
                  (partial proper-spec-metadata? metadata-map) %)]}
  (->> metadata-map
       (map (fn [[k v]]
              (cond
                (and (qualified-keyword? k)
                     (true? v))
                {:spec k
                 :type-annotation nil}

                (and (qualified-keyword? k)
                     (-> k name #{"spec"}))
                {:spec v
                 :type-annotation nil}

                (and (#{:tag} k)
                     (symbol? v))
                {:spec (list 'fn ['x]
                             (list `instance? v 'x))
                 :type-annotation (resolve v)})))
       (filter some?)))

(defn ^{:author "Rich Hickey"
        :license "Eclipse Public License 1.0"
        :comment "Adapted from clojure.core/defn, with modifications."}
  fntails
  [name & fdecl]
  {:pre [(check! symbol? name)]}
  (let [m (if (string? (first fdecl))
            {:doc (first fdecl)}
            {})
        fdecl (if (string? (first fdecl))
                (next fdecl)
                fdecl)
        m (if (map? (first fdecl))
            (conj m (first fdecl))
            m)
        fdecl (if (map? (first fdecl))
                (next fdecl)
                fdecl)
        fdecl (if (vector? (first fdecl))
                (list fdecl)
                fdecl)
        m (if (map? (last fdecl))
            (conj m (last fdecl))
            m)
        fdecl (if (map? (last fdecl))
                (butlast fdecl)
                fdecl)]
    (first fdecl)))
