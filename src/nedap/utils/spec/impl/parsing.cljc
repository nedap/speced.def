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

(defn and-spec [clj? & xs]
  (apply list
         (if clj?
           'clojure.spec.alpha/and
           'cljs.spec.alpha/and)
         xs))

(defn instance-spec [clj? class]
  (list 'fn ['x]
        (list (if clj?
                'clojure.core/instance?
                'cljs.core/instance?)
              class
              'x)))

(def clj-class-mapping
  (->> {`boolean?   `Boolean
        `char?      `Character
        `class?     `Class
        `decimal?   `BigDecimal
        `delay?     `clojure.lang.Delay
        `double?    `Double
        `future?    `java.util.concurrent.Future
        `keyword?   `clojure.lang.Keyword
        `map-entry? `java.util.Map$Entry
        `map?       `clojure.lang.IPersistentMap
        `number?    `Number
        `ratio?     `clojure.lang.Ratio
        `set?       `clojure.lang.IPersistentSet
        `string?    `String
        `symbol?    `clojure.lang.Symbol
        `var?       `clojure.lang.Var
        `vector?    `clojure.lang.IPersistentVector}
       (map (fn [[k v]]
              [[k v]
               [(-> k name symbol) v]]))
       (apply concat)
       (into {})))

(defn class-mapping [clj?]
  (if clj?
    clj-class-mapping
    {'string             'js/String
     'string?            'js/String
     'cljs.core.string?  'js/String

     'boolean            'js/Boolean
     'boolean?           'js/Boolean
     'cljs.core/boolean? 'js/Boolean

     'number             'js/Number
     'number?            'js/Number
     'cljs.core/number?  'js/Number}))

(defn spec-mapping [clj?]
  (if clj?
    {}
    {'string             'cljs.core/string?
     'string?            'cljs.core/string?
     'cljs.core/string?  'cljs.core/string?

     'boolean            'cljs.core/boolean?
     'boolean?           'cljs.core/boolean?
     'cljs.core/boolean? 'cljs.core/boolean?

     'number             'cljs.core/number?
     'number?            'cljs.core/number?
     'cljs.core/number?  'cljs.core/number?}))

(defn fail [& _]
  (assert false))

(defn infer-spec-from-symbol [clj? s]
  "For a few selected cases, one can derive a type hint out of symbol metatata."
  (let [class-mapping (class-mapping clj?)
        spec-mapping (spec-mapping clj?)
        spec (get spec-mapping
                  s
                  (if clj?
                    s
                    nil))
        inferred-class (get class-mapping s)]
    (when inferred-class
      {:spec            (if-not clj? ;; https://git.io/fjn0t
                          spec
                          (and-spec clj?
                                    spec
                                    (instance-spec clj? inferred-class)))
       :type-annotation (cond-> inferred-class
                          clj? #?(:clj  resolve
                                  :cljs fail))})))

(defn extract-specs-from-metadata [metadata-map clj?]
  {:post [(check! #{0 1}                                       (->> metadata-map
                                                                    (filter spec-directive?)
                                                                    (count))
                  (partial proper-spec-metadata? metadata-map) %)]}
  (let [nilable? (->> metadata-map keys (some #{nilable}))]
    (->> metadata-map
         (remove spec-directive?)
         (map (fn [[k v]]
                (let [symbol-inferred-type (and (#{:tag} k)
                                                (not (type-hint? v))
                                                (infer-spec-from-symbol clj? v))]
                  (cond
                    symbol-inferred-type
                    symbol-inferred-type

                    (and (qualified-keyword? k)
                         (true? v))
                    {:spec            k
                     :type-annotation nil}

                    (and (qualified-keyword? k)
                         (-> k name #{"spec"}))
                    {:spec            v
                     :type-annotation nil}

                    (and (#{:tag} k)
                         (type-hint? v clj?))
                    {:spec            (instance-spec clj? v)
                     :type-annotation (if clj?
                                        #?(:clj (resolve v) :cljs (assert false))
                                        v)}

                    (and (#{:tag} k)
                         (not (type-hint? v)))
                    {:spec            v
                     :type-annotation nil}))))
         (filter some?)
         (map (fn [{:keys [spec] :as result}]
                (cond-> result
                  nilable? (assoc :spec (list (if clj?
                                                'clojure.spec.alpha/nilable
                                                'cljs.spec.alpha/nilable)
                                              spec))))))))

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
