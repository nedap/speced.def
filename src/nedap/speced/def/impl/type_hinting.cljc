(ns nedap.speced.def.impl.type-hinting
  (:require
   [clojure.string :as string])
  #?(:clj (:import (clojure.lang IMeta))))

(def this-ns *ns*)

(def clj-primitives-map
  {'int      'int
   'ints     'ints
   'long     'long
   'longs    'longs
   'float    'float
   'floats   'floats
   'double   'double
   'doubles  'doubles
   'short    'short
   'shorts   'shorts
   'boolean  'boolean
   'booleans 'booleans
   'byte     'byte
   'bytes    'bytes
   'char     'char
   'chars    'chars})

(def cljs-primitives-map
  {'string  'string
   'boolean 'boolean
   'number  'number})

(defn cljs-type-map [{:keys [string-value boolean-value number-value]}]
  {:pre [string-value boolean-value number-value]}
  {'string                string-value
   'string?               string-value
   'cljs.core/string?     string-value
   'clojure.core/string?  string-value
   'js/String             string-value

   'boolean               boolean-value
   'boolean?              boolean-value
   'cljs.core/boolean?    boolean-value
   'clojure.core/boolean? boolean-value
   'js/Boolean            boolean-value

   'number                number-value
   'number?               number-value
   'cljs.core/number?     number-value
   'clojure.core/number?  number-value
   'js/Number             number-value})

(def cljs-checkable-class-mapping
  "'Checkable' refers to the fact that objects can be _checked_ against e.g. `js/String` in preconditions,
  as instance predicates."
  (cljs-type-map {:string-value  'js/String
                  :boolean-value 'js/Boolean
                  :number-value  'js/Number}))

(def cljs-hint-class-mapping
  (cljs-type-map {:string-value  'string
                  :boolean-value 'boolean
                  :number-value  'number}))

(defn primitives [clj?]
  (if clj?
    clj-primitives-map
    cljs-primitives-map))

(defn primitive? [s clj?]
  (-> (primitives clj?)
      (keys)
      (set)
      (contains? s)))

(defn clj-type-hint? [x]
  (assert (not= *ns* this-ns) (str "For an accurate `resolve` call (see `#'clj-type-hint?`)."
                                   (pr-str [*ns* this-ns])))
  (or (#?(:clj  class?
          :cljs (assert false)) x)
      (and (symbol? x)
           (or (#?(:clj  class?
                   :cljs (assert false)) #?(:clj  (resolve x)
                                            :cljs (assert false)))
               (primitive? x true)))))

(defn cljs-type-hint? [x]
  (or (and (symbol? x)
           (or (-> x str (string/starts-with? "js/"))
               (let [#?(:clj  ^Character c
                        :cljs c) (-> x name first)]
                 (= c #?(:clj  (-> c Character/toUpperCase)
                         :cljs (-> c .toUpperCase))))))
      (#{'boolean 'string 'number} x)))

(defn type-hint?
  ([x]
   #?(:clj  (clj-type-hint? x)
      :cljs (cljs-type-hint? x)))

  ([x clj?]
   (assert (boolean? clj?))
   #?(:clj  (if clj?
              (clj-type-hint? x)
              (cljs-type-hint? x))
      :cljs (do
              (assert (not clj?))
              (cljs-type-hint? x)))))

(defn ensure-proper-type-hint
  [clj? imeta]
  {:pre [(boolean? clj?)]}
  (if-not (instance? IMeta imeta)
    imeta
    (let [{:keys [tag]} (meta imeta)
          tag (cond->> tag
                (not clj?) (get cljs-hint-class-mapping))]
      (if (type-hint? tag clj?)
        (vary-meta imeta assoc :tag tag)
        (vary-meta imeta dissoc :tag)))))

(defn ensure-proper-type-hints
  "Removes / converts type hints that would be extraneous to the compiler:

  * For Clojure, functions cannot be type hints. So they're removed.

  * For ClojureScript, e.g. `js/String` can be passed as a type hint under this library.
  That is converted to `string`, so the related compiler optimizations are enabled."
  [clj? args]
  {:pre [(boolean? clj?)]}
  (with-meta (mapv (partial ensure-proper-type-hint clj?)
                   args)
    (meta (ensure-proper-type-hint clj? args))))

(defn ann->symbol [ann]
  (if (#?(:clj  class?
          :cljs (assert false)) ann)
    #?(:clj  (-> ^Class ann .getName symbol)
       :cljs (assert false))
    ann))

(defn type-hint [args args-sigs]
  (let [result (mapv (fn [arg {:keys [type-annotation]}]
                       (cond-> arg
                         type-annotation (vary-meta assoc :tag (ann->symbol type-annotation))))
                     args
                     args-sigs)]
    (with-meta result (meta args))))
