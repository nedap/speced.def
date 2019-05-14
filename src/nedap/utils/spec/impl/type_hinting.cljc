(ns nedap.utils.spec.impl.type-hinting
  #?(:clj (:import
           (clojure.lang IMeta))))

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
  (or (class? x)
      (and (symbol? x)
           (or (class? #?(:clj  (resolve x)
                          :cljs (assert false)))
               (primitive? x true)))))

(defn cljs-type-hint? [x]
  (or (and (symbol? x)
           (let [c (-> x name first)]
             (= c (Character/toUpperCase c))))
      (#{'boolean 'string 'number} x)))

(defn type-hint?
  ([x]
   (assert (not= *ns* this-ns) "For an accurate `resolve` call (see `#'clj-type-hint?`).")
   #?(:clj  (clj-type-hint? x)
      :cljs (cljs-type-hint? x)))

  ([x clj?]
   (assert (not= *ns* this-ns) "For an accurate `resolve` call (see `#'clj-type-hint?`).")
   #?(:clj  (if clj?
              (clj-type-hint? x)
              (cljs-type-hint? x))
      :cljs (assert false))))

(defn strip-extraneous-type-hint
  [imeta]
  (if-not (instance? IMeta imeta)
    imeta
    (let [{:keys [tag]} (meta imeta)]
      (if (type-hint? tag)
        imeta
        (vary-meta imeta dissoc :tag)))))

(defn strip-extraneous-type-hints
  "As per this library's 'syntax', functions can be passed as type hints.

  That wouldn't emit valid Clojure code, so those pseudo type hints are removed (and will be only used for spec validation)."
  [args]
  (with-meta (mapv strip-extraneous-type-hint args)
    (meta (strip-extraneous-type-hint args))))

(defn ann->symbol [ann]
  (if (class? ann)
    (-> ann .getName symbol)
    ann))

(defn type-hint [args args-sigs]
  (let [result (mapv (fn [arg {:keys [type-annotation]}]
                       (cond-> arg
                         type-annotation (vary-meta assoc :tag (ann->symbol type-annotation))))
                     args
                     args-sigs)]
    (with-meta result (meta args))))
