(ns nedap.utils.spec.impl.defn
  (:require
   #?(:clj  [clojure.core.specs.alpha :as specs]
      :cljs [cljs.core.specs.alpha :as specs])
   [nedap.utils.spec.api #?(:clj :refer :cljs :refer-macros) [check!]]
   [nedap.utils.spec.impl.parsing :refer [extract-specs-from-metadata fntails]]
   [nedap.utils.spec.impl.type-hinting :refer [type-hint? strip-extraneous-type-hints]]))

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
                    {inner-ret-spec :spec} (-> args meta (extract-specs-from-metadata clj?) first)
                    args-sigs (map (fn [arg arg-meta]
                                     (merge {:arg arg}
                                            (-> arg-meta (extract-specs-from-metadata clj?) first)))
                                   args
                                   (map meta args))
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

(defn impl
  [clj? [name & tail :as args]]
  {:pre [(check! ::specs/defn-args args)]}
  (let [{ret-spec :spec
         ret-ann  :type-annotation} (-> name meta (extract-specs-from-metadata clj?) first)
        {:keys [tails docstring-and-meta]} (parse name tail)
        tails (add-prepost tails ret-spec clj?)
        name-had-tag? (-> name meta keys #{:tag})
        name (if (and name-had-tag?
                      (-> name meta :tag type-hint?))
               name
               (vary-meta name dissoc :tag))]
    (apply list `clojure.core/defn (cons name (concat docstring-and-meta tails)))))
