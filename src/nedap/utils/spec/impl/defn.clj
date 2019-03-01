(ns nedap.utils.spec.impl.defn
  (:require
   [clojure.core.specs.alpha :as specs]
   [nedap.utils.spec.api :refer [check!]]
   [nedap.utils.spec.impl.parsing :refer [extract-specs-from-metadata fntails]]
   [nedap.utils.spec.impl.type-hinting :refer :all]))

(defn add-prepost [tails ret-spec]
  (->> tails
       (map (fn [[args maybe-prepost & maybe-rest-of-body :as tail]]
              (let [rest-of-body? (-> maybe-rest-of-body seq)
                    body (if rest-of-body?
                           (cons maybe-rest-of-body rest-of-body?)
                           [maybe-prepost])
                    {inner-ret-spec :spec} (-> args meta extract-specs-from-metadata first)
                    args-sigs (map (fn [arg arg-meta]
                                     (merge {:arg arg}
                                            (->> arg-meta extract-specs-from-metadata first)))
                                   args
                                   (map meta args))
                    args-check-form (->> args-sigs
                                         (filter :spec)
                                         (map (fn [{:keys [spec arg]}]
                                                [spec arg]))
                                         (apply concat)
                                         (apply list `check!))
                    prepost (cond-> (when-not (= [maybe-prepost] body)
                                      maybe-prepost)
                              true (update :pre vec) ;; maybe there was no :pre. Ensure it's a vector
                              true (update :post vec) ;; maybe there was no :post. Ensure it's a vector
                              (-> args-check-form count (> 1)) (update :pre conj args-check-form)
                              ret-spec (update :post conj (list `check! ret-spec '%))
                              inner-ret-spec (update :post conj (list `check! inner-ret-spec '%))
                              ;; ret-spec and inner-ret-spec may be identical
                              true (update :post (comp vec distinct)))
                    args-with-proper-tag-hints (strip-extraneous-type-hints args)]
                (apply list args-with-proper-tag-hints prepost body))))))

(defn impl
  [[name & tail :as args]]
  {:pre [(check! ::specs/defn-args args)]}
  (let [{ret-spec :spec
         ret-ann :type-annotation} (-> name meta extract-specs-from-metadata first)
        tail (if (-> tail first list?)
               tail
               (list tail))
        tails (fntails name tail)
        tails (add-prepost tails ret-spec)
        name-had-tag? (-> name meta keys #{:tag})
        name (if (and name-had-tag?
                      (-> name meta :tag type-hint?))
               name
               (vary-meta name dissoc :tag))]
    (apply list `clojure.core/defn (cons name tails))))
