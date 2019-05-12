(ns nedap.utils.spec.impl.defn
  (:require
   #?(:clj  [clojure.core.specs.alpha :as specs]
      :cljs [cljs.core.specs.alpha :as specs])
   [nedap.utils.spec.api #?(:clj :refer :cljs :refer-macros) [check!]]
   [nedap.utils.spec.impl.parsing :refer [extract-specs-from-metadata fntails]]
   [nedap.utils.spec.impl.type-hinting :refer [type-hint type-hint? strip-extraneous-type-hints]]))

(defn add-prepost [tails ret-spec clj?]
  (->> tails
       (map (fn [[args maybe-prepost & maybe-rest-of-body :as tail]]
              (let [rest-of-body? (-> maybe-rest-of-body seq)
                    body (if rest-of-body?
                           (cons maybe-prepost rest-of-body?)
                           [maybe-prepost])
                    has-prepost? (and (not= [maybe-prepost] body)
                                      (map? maybe-prepost))
                    body (if has-prepost?
                           (rest body)
                           body)
                    {inner-ret-spec :spec
                     ret-type-ann   :type-annotation} (-> args
                                                          meta
                                                          (extract-specs-from-metadata clj?)
                                                          (first))
                    args-sigs (map (fn [arg arg-meta]
                                     (merge {:arg arg}
                                            (-> arg-meta
                                                (extract-specs-from-metadata clj?)
                                                (first))))
                                   args
                                   (map meta args))
                    args (cond-> (type-hint args args-sigs)
                           (type-hint? ret-type-ann clj?) (vary-meta assoc :tag ret-type-ann))
                    args-check-form (->> args-sigs
                                         (filter :spec)
                                         (map (fn [{:keys [spec arg]}]
                                                [spec arg]))
                                         (apply concat)
                                         (apply list `check!))
                    prepost (cond-> (when has-prepost?
                                      maybe-prepost)
                              ;; maybe there was no :pre. Ensure it's a vector:
                              true                             (update :pre vec)
                              ;; maybe there was no :post. Ensure it's a vector:
                              true                             (update :post vec)
                              (-> args-check-form count (> 1)) (update :pre conj args-check-form)
                              ret-spec                         (update :post conj (list `check! ret-spec '%))
                              inner-ret-spec                   (update :post conj (list `check! inner-ret-spec '%))
                              ;; ret-spec and inner-ret-spec may be identical
                              true                             (update :post (comp vec distinct)))
                    args-with-proper-tag-hints (strip-extraneous-type-hints args)]
                (apply list args-with-proper-tag-hints prepost body))))))

(defn parse [name tail]
  (let [pred (some-fn string? map?)
        docstring-and-meta (->> (take-while pred tail) (remove nil?))
        tail (drop-while pred tail)
        tail (if (-> tail first list?)
               tail
               (list tail))]
    {:tails              (fntails name tail)
     :docstring-and-meta docstring-and-meta}))

(def ^:dynamic *clj?* nil)

(defn tag=
  [& xs]
  {:pre [(some? *clj?*)
         (pos? (count xs))
         (every? (fn [x]
                   (or (symbol? x)
                       (class? x)))
                 xs)]}
  (if-not *clj?*
    (apply = xs)
    (->> xs
         (map (fn [x]
                (cond
                  (class? x)  x
                  (symbol? x) #?(:clj  (resolve x)
                                 :cljs (assert false))
                  true        (assert false))))
         (apply =))))

(defn tail-tag [tail]
  (->> tail
       (filter vector?)
       (first)
       (meta)
       (:tag)))

(defn ret-ann-from-tails [tails clj?]
  {:pre [(some? clj?)]}
  (binding [*clj?* clj?]
    (let [tags (->> tails
                    (keep tail-tag))]
      (when (or (-> tags count #{1})
                (and (-> tags count pos?)
                     (apply tag= tags)))
        (first tags)))))

(defn consistent-tagging? [ann tails clj?]
  {:pre [(some? clj?)]}
  (binding [*clj?* clj?]
    (let [tags (keep tail-tag tails)
          all (cond-> tags
                ann (conj ann))]
      (if-not (seq all)
        true
        (apply tag= all)))))

(defn impl
  [clj? [name & tail :as args]]
  {:pre [(check! some?             clj?
                 ::specs/defn-args args)]}
  (let [{ret-spec :spec
         ret-ann  :type-annotation} (-> name meta (extract-specs-from-metadata clj?) first)
        {:keys [tails docstring-and-meta]} (parse name tail)
        tails (add-prepost tails ret-spec clj?)
        tails-ann (ret-ann-from-tails tails clj?)
        name-ann (-> name
                     meta
                     (extract-specs-from-metadata clj?)
                     first
                     :type-annotation
                     (or tails-ann))
        _ (assert (consistent-tagging? name-ann tails clj?)
                  "Type hints/specs must have the same type across arities, and between arities and the defn's name metadata.")
        name (if (type-hint? name-ann clj?)
               (vary-meta name assoc :tag name-ann)
               (vary-meta name dissoc :tag))]
    (apply list `clojure.core/defn (cons name (concat docstring-and-meta tails)))))
