(ns nedap.utils.spec.impl.parsing
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   [nedap.utils.spec.impl.check #?(:clj :refer :cljs :refer-macros) [check!]]
   [nedap.utils.spec.impl.type-hinting :refer [type-hint?]]
   [nedap.utils.spec.specs :as specs]))

(defn proper-spec-metadata? [metadata-map extracted-specs]
  (case (-> extracted-specs count)
    0 true
    1 (check! ::specs/spec-metadata metadata-map)
    false))

(def nilable :nedap.utils.speced/nilable)

(def spec-directives #{nilable})

(def spec-directive? (comp spec-directives first))

(defn extract-specs-from-metadata [metadata-map]
  {:post [(check! #{0 1} (->> metadata-map
                              (filter spec-directive?)
                              count)
                  (partial proper-spec-metadata? metadata-map) %)]}
  (let [nilable? (->> metadata-map keys (some #{nilable}))]
    (->> metadata-map
         (remove spec-directive?)
         (map (fn [[k v]]
                (cond
                  (and (qualified-keyword? k)
                       (true? v))
                  {:spec            k
                   :type-annotation nil}

                  (and (qualified-keyword? k)
                       (-> k name #{"spec"}))
                  {:spec            v
                   :type-annotation nil}

                  (and (#{:tag} k)
                       (type-hint? v))
                  {:spec            (list 'fn ['x]
                                          (list `instance? v 'x))
                   :type-annotation #?(:clj  (resolve v)
                                       :cljs v)}

                  (and (#{:tag} k)
                       (not (type-hint? v)))
                  {:spec            v
                   :type-annotation nil})))
         (filter some?)
         (map (fn [{:keys [spec] :as result}]
                (cond-> result
                  nilable? (assoc :spec (list `spec/nilable spec))))))))

(defn ^{:author  "Rich Hickey"
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
