(ns nedap.utils.spec.impl.type-hinting
  #?(:clj (:import
           (clojure.lang IMeta))))

(def this-ns *ns*)

(defn type-hint?
  ([x]
   (assert (not= *ns* this-ns) "For an accurate `resolve` call (see below).")
   #?(:clj  (or (class? x)
                (and (symbol? x)
                     (class? (resolve x))))
      ;; maybe: starts by capital:
      :cljs (and (symbol? x)
                 (let [c (-> x name first)]
                   (= c (-> c .toUpperCase))))))

  ([x clj?]
   (assert (not= *ns* this-ns) "For an accurate `resolve` call (see below).")
   #?(:clj  (if clj?
              (or (class? x)
                  (and (symbol? x)
                       (class? (resolve x))))
              (and (symbol? x)
                   (let [c (-> x name first)]
                     (= c (Character/toUpperCase c)))))
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
