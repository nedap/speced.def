(ns nedap.speced.def.impl.parsing
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   [clojure.string :as string]
   [nedap.speced.def.impl.type-hinting :refer [cljs-checkable-class-mapping cljs-hint-class-mapping cljs-type-map primitive? primitives type-hint?]]
   [nedap.speced.def.specs :as specs]
   [nedap.utils.spec.impl.check #?(:clj :refer :cljs :refer-macros) [check!]]))

(defn proper-spec-metadata? [clj? metadata-map extracted-specs]
  (binding [specs/*clj?* clj?]
    (case (-> extracted-specs count)
      0 true
      1 (and (check! ::specs/spec-metadata metadata-map)
             (check! (fn [{:keys [type-annotation]}]
                       (if clj?
                         true
                         (not (-> type-annotation str (string/starts-with? "js/")))))
                     (first extracted-specs)))
      false)))

(def nilable :nedap.speced.def/nilable)

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
    (if clj?
      (list 'fn ['x]
            (list 'if (list 'clojure.core/class? class)
                  (list 'clojure.core/instance? class 'x)
                  (list 'clojure.core/satisfies? class 'x)))
      ;; Don't use `cljs.core/instance?`! https://dev.clojure.org/jira/browse/CLJS-98
      (list 'cljs.spec.alpha/or
            :class-instance    (list 'fn ['x]
                                     (list 'cljs.core/= class (list 'cljs.core/type 'x)))
            :protocol-instance (list 'fn ['x]
                                     (list 'cljs.core/satisfies? class 'x))))))

(def base-clj-class-mapping
  {`associative?        `clojure.lang.Associative
   `boolean?            `Boolean
   `bytes?              (symbol "[B")
   `char?               `Character
   `chunked-seq?        `clojure.lang.IChunkedSeq
   `class?              `Class
   `coll?               `clojure.lang.IPersistentCollection
   `counted?            `clojure.lang.Counted
   `decimal?            `BigDecimal
   `delay?              `clojure.lang.Delay
   `double?             `Double
   `false?              `Boolean
   `fn?                 `clojure.lang.Fn
   `future?             `java.util.concurrent.Future
   `ifn?                `clojure.lang.IFn
   `indexed?            `clojure.lang.Indexed
   `keyword?            `clojure.lang.Keyword
   `list?               `clojure.lang.IPersistentList
   `map-entry?          `java.util.Map$Entry
   `map?                `clojure.lang.IPersistentMap
   `number?             `Number
   `qualified-keyword?  `clojure.lang.Keyword
   `qualified-symbol?   `clojure.lang.Symbol
   `ratio?              `clojure.lang.Ratio
   `reader-conditional? `clojure.lang.ReaderConditional
   `realized?           `clojure.lang.IPending
   `record?             `clojure.lang.IRecord
   `reversible?         `clojure.lang.Reversible
   `set?                `clojure.lang.IPersistentSet
   `seq?                `clojure.lang.ISeq
   `sequential?         `clojure.lang.Sequential
   `simple-keyword?     `clojure.lang.Keyword
   `simple-symbol?      `clojure.lang.Symbol
   `sorted?             `clojure.lang.Sorted
   `string?             `String
   `symbol?             `clojure.lang.Symbol
   `tagged-literal?     `clojure.lang.TaggedLiteral
   `true?               `Boolean
   `uri?                `java.net.URI
   `uuid?               `java.util.UUID
   `var?                `clojure.lang.Var
   `vector?             `clojure.lang.IPersistentVector
   `volatile?           `clojure.lang.Volatile})

(do
  #?(:clj (assert (->> base-clj-class-mapping
                       (apply concat)
                       (every? (fn [x]
                                 (or (resolve x)
                                     (Class/forName x))))))))

(def clj-class-mapping
  (->> base-clj-class-mapping
       (map (fn [[k v]]
              [[k v]
               [(-> k name symbol) v]]))
       (apply concat)
       (into {})
       (merge (primitives true))))

(defn class-mapping [clj? checkable?]
  (cond
    clj?       clj-class-mapping
    checkable? cljs-checkable-class-mapping
    true       cljs-hint-class-mapping))

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
    (cljs-type-map {:string-value  'cljs.core/string?
                    :boolean-value 'cljs.core/boolean?
                    :number-value  'cljs.core/number?})))

(defn infer-spec-from-symbol
  "For a few selected cases, one can derive a type hint out of symbol metadata."
  [clj? s]
  {:post [(if-not %
            true
            (-> % :spec))]}
  (let [checkable-class-mapping (class-mapping clj? true)
        hint-class-mapping (class-mapping clj? false)
        spec-mapping (spec-mapping clj?)
        spec (get spec-mapping
                  s
                  (if clj?
                    s
                    nil))
        inferred-checkable-class (get checkable-class-mapping s)
        inferred-hint-class (get hint-class-mapping s)]
    (when (or (and (not clj?)
                   spec)
              inferred-checkable-class
              inferred-hint-class)
      {:spec            (if (or (not clj?) ;; https://git.io/fjn0t
                                (not inferred-checkable-class))
                          spec
                          (and-spec clj?
                                    spec
                                    (instance-spec clj? inferred-checkable-class)))
       :type-annotation (cond
                          (primitive? inferred-hint-class clj?) inferred-hint-class
                          (and clj? inferred-hint-class)        (#?(:clj resolve :cljs fail) inferred-hint-class)
                          true                                  inferred-hint-class)

       :was-primitive?  (primitive? s clj?)})))

(def forbidden-primitives-message
  "Primitive type hints aren't nilable. That would emit code that would fail opaquely.")

(defn class->symbol [#?(:clj  ^Class c
                        :cljs c)]
  {:pre [(#?(:clj  class?
             :cljs (assert false)) c)]}
  (-> c .getName symbol))

(defn extract-specs-from-metadata [metadata-map clj?]
  {:pre  [(check! (spec/nilable map?) metadata-map
                  boolean?            clj?
                  #{0 1}              (->> metadata-map
                                           (filter spec-directive?)
                                           (count)))]
   :post [(check! (partial proper-spec-metadata? clj? metadata-map) %)]}
  (let [metadata-map (cond-> metadata-map
                       (and clj?
                            (-> metadata-map :tag #?(:clj  class?
                                                     :cljs fail))), (update :tag class->symbol))
        nilable? (->> metadata-map keys (some #{nilable}))]
    (->> metadata-map
         (remove spec-directive?)
         (map (fn [[k v]]
                (let [symbol-inferred-type (and (#{:tag} k)
                                                (or (not (type-hint? v clj?))
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
                                        (or (some->> v (infer-spec-from-symbol clj?) :type-annotation)
                                            v))}

                    (and (#{:tag} k)
                         (not (type-hint? v clj?)))
                    {:spec            v
                     :type-annotation nil}))))
         (filter some?)
         (map (fn [{:keys [spec was-primitive?] :as result}]
                (cond
                  nilable? (do
                             (when clj?
                               (assert (not was-primitive?) forbidden-primitives-message))
                             (assoc result :spec (list (if clj?
                                                         'clojure.spec.alpha/nilable
                                                         'cljs.spec.alpha/nilable)
                                                       spec)))
                  true     result))))))

(defn ^{:author  "Rich Hickey"
        :license "Eclipse Public License 1.0"
        :comment "Adapted from clojure.core/defn, with modifications."}
  fntails
  [name & fdecl]
  {:pre [(check! (spec/nilable symbol?) name)]}
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
