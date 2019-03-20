(ns nedap.utils.spec.impl.defprotocol
  (:refer-clojure :exclude [defprotocol])
  (:require
   [clojure.spec.alpha :as spec]
   [nedap.utils.spec.impl.check :refer [check!]]
   [nedap.utils.spec.impl.parsing :refer [extract-specs-from-metadata]]
   [nedap.utils.spec.impl.type-hinting :refer :all]))

(spec/def ::args (spec/coll-of symbol? :kind vector :min-count 1))

(spec/def ::method (spec/and list?
                             (spec/cat :name symbol?
                                       :args ::args
                                       :docstring string?)))

(defn emit-method [[method-name args docstring :as method]]
  {:pre [(check! ::method method)]}
  (let [ret-metadata (merge (meta method-name)
                            (meta args))
        {ret-spec :spec
         ^Class
         ret-ann  :type-annotation} (-> ret-metadata extract-specs-from-metadata first)
        args-sigs (map (fn [arg arg-meta]
                         (merge {:arg arg}
                                (->> arg-meta extract-specs-from-metadata first)))
                       args
                       (map meta args))
        args-specs (->> args-sigs
                        (filter :spec)
                        (map (fn [{:keys [spec arg]}]
                               [spec arg]))
                        (apply concat)
                        (apply list `check!)
                        vector)
        prepost (cond-> {:pre args-specs}
                  ret-spec (assoc :post [(list `check! ret-spec '%)]))
        tag (some->> ret-ann .getName symbol)
        tag? (some-> tag type-hint?)
        impl (cond-> (->> method-name (str "--") symbol)
               tag (vary-meta assoc :tag tag))
        method-name (cond-> method-name
                      tag?       (vary-meta assoc :tag (list 'quote tag))
                      (not tag?) (vary-meta dissoc :tag))
        args-with-proper-tag-hints (strip-extraneous-type-hints args)]
    {:declare `(declare ~impl)
     :impl    `(defn ~method-name ~docstring ~args-with-proper-tag-hints ~prepost (~impl ~@args))
     :method  `(~impl ~args-with-proper-tag-hints ~docstring)}))

(defmacro defprotocol [name docstring & methods]
  {:pre [(check! symbol? name
                 string? docstring
                 ;; (`methods` are already checked in emit-method)
                 )]}
  (let [{:keys [impls methods declares] :as x} (->> methods
                                                    (map emit-method)
                                                    (reduce (fn [acc {:keys [impl method declare]}]
                                                              (-> acc
                                                                  (update :impls conj impl)
                                                                  (update :declares conj declare)
                                                                  (update :methods conj method)))
                                                            {:impls    []
                                                             :declares []
                                                             :methods  []}))]

    `(do
       ~@declares
       (clojure.core/defprotocol ~name
         ~docstring
         :extend-via-metadata true
         ~@methods)
       ~@impls
       ;; matches the clojure.core behavior:
       ~(list 'quote name))))
