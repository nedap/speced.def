(ns nedap.speced.def.impl.defn
  (:require
   #?(:clj [clojure.core.specs.alpha :as specs] :cljs [cljs.core.specs.alpha :as specs])
   [clojure.spec.alpha :as spec]
   [clojure.walk :as walk]
   [nedap.speced.def.impl.parsing :refer [extract-specs-from-metadata fntails]]
   [nedap.speced.def.impl.type-hinting :refer [ensure-proper-type-hint ensure-proper-type-hints primitive? type-hint type-hint?]]
   [nedap.utils.spec.api #?(:clj :refer :cljs :refer-macros) [check!]]))

(defn extract-specs-from-destructurings [clj? args]
  {:pre [(check! boolean? clj?)]}
  (let [result (atom [])]
    (->> args
         (remove symbol?) ;; plain args will be analyzed separately
         (walk/postwalk (fn [x]
                          (let [is-symbol (symbol? x)]
                            (when-not is-symbol
                              (when-let [spec (some-> x
                                                      meta
                                                      (extract-specs-from-metadata clj?)
                                                      (first))]
                                (assert false (str "Only symbols can be attached spec metadata. Found spec: "
                                                   (pr-str spec)
                                                   " in form: "
                                                   (pr-str x)))))
                            (when is-symbol
                              (when-let [spec (some-> x
                                                      meta
                                                      (extract-specs-from-metadata clj?)
                                                      (first)
                                                      (assoc :arg x))]
                                (swap! result conj spec)))
                            x))))
    @result))

(defn maybe-type-hint-destructured-args
  "Returns a copy of `all-args`, substituting spec metadata in symbols belonging to destructurings
  with type hints that the Clojure compiler will understand."
  [clj? non-destructured-args all-args]
  {:pre [(check! boolean? clj?
                 set?     non-destructured-args)]}
  (->> all-args
       (walk/postwalk (fn [x]
                        (cond
                          (not (symbol? x))         x
                          (non-destructured-args x) x
                          true                      (let [metadata-map (meta x)]
                                                      (if-not (seq metadata-map)
                                                        x
                                                        (->> (if-let [ann (-> metadata-map
                                                                              (extract-specs-from-metadata clj?)
                                                                              (first)
                                                                              (:type-annotation))]
                                                               (vary-meta x assoc :tag ann)
                                                               (vary-meta x dissoc :tag))
                                                             (ensure-proper-type-hint clj?)))))))))

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
                    specs-from-destructurings (extract-specs-from-destructurings clj? args)
                    non-destructured-args (->> args (filter symbol?) (set))
                    args (maybe-type-hint-destructured-args clj? non-destructured-args args)
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
                                         (concat specs-from-destructurings)
                                         (filter :spec)
                                         (map (fn [{:keys [spec arg]}]
                                                {:pre [spec arg]}
                                                [spec
                                                 ;; Avoid "Can't type hint a primitive local" error:
                                                 (vary-meta arg dissoc :tag)]))
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
                    args-with-proper-tag-hints (ensure-proper-type-hints clj? args)]
                (apply list args-with-proper-tag-hints prepost body))))))

(defn parse [name tail]
  (let [pred (some-fn string? map?)
        docstring-and-meta (->> (take-while pred tail) (remove nil?))
        tail (drop-while pred tail)
        tail (if (-> tail first list?)
               tail
               (list tail))]
    (assert (->> tail
                 (map meta)
                 (mapcat keys)
                 (not-any? #{:tag}))
            ":tag metadata placed in a wrong position.")
    {:tails              (fntails name tail)
     :docstring-and-meta docstring-and-meta}))

(def ^:dynamic *clj?* nil)

(defn tag=
  [& xs]
  {:pre [(check! boolean?                                              *clj?*
                 pos?                                                  (count xs)
                 (partial every? (fn [x]
                                   (or (symbol? x)
                                       (#?(:clj  class?
                                           :cljs (assert false)) x)))) xs)]}
  (if-not *clj?*
    (apply = xs)
    (->> xs
         (map (fn [x]
                (cond
                  (#?(:clj  class?
                      :cljs (assert false)) x) x
                  (symbol? x)                  #?(:clj  (resolve x)
                                                  :cljs (assert false))
                  true                         (assert false))))
         (apply =))))

(defn tail-tag [tail]
  {:pre [(boolean? *clj?*)]}
  (let [m (->> tail
               (filter vector?)
               (first)
               (meta))]
    (some-> m
            (extract-specs-from-metadata *clj?*)
            (first)
            (:type-annotation))))

(defn ret-ann-from-tails [tails clj?]
  {:pre [(boolean? clj?)]}
  (binding [*clj?* clj?]
    (let [tags (->> tails
                    (keep tail-tag))]
      (when (or (-> tags count #{1})
                (and (-> tags count pos?)
                     (apply tag= tags)))
        (first tags)))))

(defn consistent-tagging? [ann tails clj?]
  {:pre [(boolean? clj?)]}
  (binding [*clj?* clj?]
    (let [tags (keep tail-tag tails)
          all (cond-> tags
                ann (conj ann))]
      (if-not (seq all)
        true
        (apply tag= all)))))

(defn maybe-tag-tails [tag tails]
  {:pre [(check! boolean?                                *clj?*
                 (spec/nilable (fn [x]
                                 (type-hint? x *clj?*))) tag)]}
  (if-not tag
    tails
    (let [tags (-> (->> tails (keep tail-tag) (distinct) (set))
                   (conj tag))
          _ (assert (#{0 1} (count tags))
                    "Type hints/specs must have the same type across arities, and between arities and the defn's name metadata.")
          tag (first tags)]
      (if-not tag
        tails
        (->> tails
             (map (fn [[args & remaining :as all]]
                    (let [args (vary-meta args assoc :tag tag)
                          tail (cons args remaining)]
                      (with-meta tail
                        (meta all))))))))))

(defn process-name [name, name-ann, clj?]
  {:pre [(check! symbol?                                      name
                 (spec/nilable (fn [x]
                                 (type-hint? name-ann clj?))) name-ann
                 boolean?                                     clj?)]}
  (let [name (cond-> name
               (type-hint? name-ann clj?) (vary-meta assoc :tag name-ann))
        name (ensure-proper-type-hint clj? name)
        name (cond-> name
               (and clj?
                    (primitive? name-ann clj?))
               ;; 'int would become #'int after JVM compilation, that's how the compiler works, for defn names,
               ;; because that's not their expected position. Avoid that:
               (vary-meta dissoc :tag))]
    name))

(defn process-name-and-tails [{:keys [name tail clj?] :as options}]
  {:pre [(check! (spec/nilable symbol?) name
                 coll?                  tail
                 boolean?               clj?)]}
  (let [{ret-spec :spec
         name-ann :type-annotation} (-> name meta (extract-specs-from-metadata clj?) first)
        {:keys [tails docstring-and-meta]} (parse name tail)
        tails (add-prepost tails ret-spec clj?)
        tails-ann (ret-ann-from-tails tails clj?)
        name-ann (or name-ann tails-ann)
        name-ann (->> ^{:tag name-ann} {}
                      (ensure-proper-type-hint clj?)
                      (meta)
                      :tag)
        tails (binding [*clj?* clj?]
                (maybe-tag-tails name-ann tails))
        _ (assert (consistent-tagging? name-ann tails clj?)
                  "Type hints/specs must have the same type across arities, and between arities and the defn's name metadata.")
        name (some-> name (process-name name-ann clj?))]
    {:tails tails, :name name, :docstring-and-meta docstring-and-meta}))

(defn impl
  [clj? [name & tail :as args]]
  {:pre [(check! boolean?          clj?
                 ::specs/defn-args args)]}
  (let [{:keys [tails name docstring-and-meta]} (process-name-and-tails {:tail tail
                                                                         :name name
                                                                         :clj? clj?})]
    (apply list `clojure.core/defn (cons name (concat docstring-and-meta tails)))))
