(ns nedap.utils.spec.impl.parsing
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   [nedap.utils.spec.impl.check #?(:clj :refer :cljs :refer-macros) [check!]]
   [nedap.utils.spec.impl.type-hinting :refer [type-hint? primitives primitive?]]
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
  (let [specs (->> xs distinct (remove nil?))
        pred (if clj?
               'clojure.spec.alpha/and
               'cljs.spec.alpha/and)]
    (if (#{1} (count specs))
      (first specs)
      (apply list pred specs))))

(defn instance-spec [clj? class]
  (when-not (primitive? class clj?)
    (list 'fn ['x]
          (list (if clj?
                  'clojure.core/instance?
                  'cljs.core/instance?)
                class
                'x))))

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
       (into {})
       (merge (primitives true))))

(def cljs-class-mapping
  {'string             'js/String
   'string?            'js/String
   'cljs.core.string?  'js/String

   'boolean            'js/Boolean
   'boolean?           'js/Boolean
   'cljs.core/boolean? 'js/Boolean

   'number             'js/Number
   'number?            'js/Number
   'cljs.core/number?  'js/Number})

(defn class-mapping [clj?]
  (if clj?
    clj-class-mapping
    cljs-class-mapping))

(defn fail [& _]
  (assert false))

(defn array-class-expr [class]
  {:pre [(symbol? class)
         (#?(:clj resolve :cljs fail) class)]}
  `(class (make-array ~class 0)))

(defn spec-mapping [clj?]
  (if clj?
    {'int      (instance-spec clj? `Integer)
     'ints     (instance-spec clj? (array-class-expr `Integer))
     'long     (instance-spec clj? `Long)
     'longs    (instance-spec clj? (array-class-expr `Long))
     'float    (instance-spec clj? `Float)
     'floats   (instance-spec clj? (array-class-expr `Float))
     'double   (instance-spec clj? `Double)
     'doubles  (instance-spec clj? (array-class-expr `Double))
     'short    (instance-spec clj? `Short)
     'shorts   (instance-spec clj? (array-class-expr `Short))
     'boolean  (instance-spec clj? `Boolean)
     'booleans (instance-spec clj? (array-class-expr `Boolean))
     'byte     (instance-spec clj? `Byte)
     'bytes    (instance-spec clj? (array-class-expr `Byte))
     'char     (instance-spec clj? `Character)
     'chars    (instance-spec clj? (array-class-expr `Character))}
    {'string             'cljs.core/string?
     'string?            'cljs.core/string?
     'cljs.core/string?  'cljs.core/string?

     'boolean            'cljs.core/boolean?
     'boolean?           'cljs.core/boolean?
     'cljs.core/boolean? 'cljs.core/boolean?

     'number             'cljs.core/number?
     'number?            'cljs.core/number?
     'cljs.core/number?  'cljs.core/number?}))

(defn infer-spec-from-symbol
  "For a few selected cases, one can derive a type hint out of symbol metatata."
  [clj? s]
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
       :type-annotation (cond
                          (primitive? inferred-class clj?) inferred-class
                          clj?                             (#?(:clj resolve :cljs fail) inferred-class)
                          true                             inferred-class)})))

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
                                                (or (not (type-hint? v))
                                                    (primitive? v clj?))
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