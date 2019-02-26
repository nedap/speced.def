(ns nedap.utils.spec.impl.defprotocol
  (:refer-clojure :exclude [defprotocol])
  (:require
   [clojure.spec.alpha :as spec]
   [nedap.utils.spec.impl.check :refer [check!]]
   [nedap.utils.spec.impl.parsing :refer [extract-specs-from-metadata]]))

(spec/def ::args (spec/coll-of symbol? :kind vector :min-count 1))

(spec/def ::method (spec/and list?
                             (spec/cat :name symbol?
                                       :args ::args
                                       :docstring string?)))

(defn emit-method [[method-name args docstring :as method]]
  {:pre [(check! ::method method)]}
  (let [{ret-spec :spec
         ret-ann :type-annotation} (->> method-name meta extract-specs-from-metadata first)
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
        prepost {:pre args-specs :post [(list `check! ret-spec '%)]}
        tag (some->> ret-ann .getName symbol)
        impl (cond-> (->> method-name (str "--") symbol)
               tag (vary-meta assoc :tag tag))
        method-name (cond-> method-name
                      tag (vary-meta assoc :tag (list 'quote tag)))]
    {:declare `(declare ~impl)
     :impl `(defn ~method-name ~docstring ~args ~prepost (~impl ~@args))
     :method `(~impl ~args ~docstring)}))

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
                                                            {:impls []
                                                             :declares []
                                                             :methods []}))]

    `(do
       ~@declares
       (clojure.core/defprotocol ~name
         ~docstring
         :extend-via-metadata true
         ~@methods)
       ~@impls
       ;; matches the clojure.core behavior:
       ~(list 'quote name))))
