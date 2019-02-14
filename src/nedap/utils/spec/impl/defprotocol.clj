(ns nedap.utils.spec.impl.defprotocol
  (:refer-clojure :exclude [defprotocol])
  (:require
   [clojure.spec.alpha :as spec]
   [nedap.utils.spec.impl.check :refer [check!]]))

(spec/def ::args (spec/coll-of symbol? :kind vector :min-count 1))

(spec/def ::method (spec/and list?
                             (spec/cat :name symbol?
                                       :args ::args
                                       :docstring string?)))

(defn spec-metadata? [[k v]]
  (and (qualified-keyword? k)
       (true? v)))

(defn emit-method [[method-name args docstring :as method]]
  {:pre [(check! ::method method)]}
  (let [ret-spec (->> method-name meta (filter spec-metadata?) (mapv (fn [[k v]]
                                                                       (list `check! k '%))))
        args-specs (->> (map (fn [arg arg-meta]
                               (let [s-m (->> arg-meta (filter spec-metadata?))
                                     _ (assert (check! #{0 1} (count s-m)))]
                                 (when-let [[k v] (first s-m)]
                                   [k arg])))
                             args
                             (map meta args))
                        (filter some?)
                        (apply concat)
                        (apply list `check!)
                        vector)
        _ (check! #{0 1} (count ret-spec))
        prepost {:pre args-specs :post ret-spec}
        impl (->> method-name (str "--") symbol)]
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
       (clojure.core/defprotocol ~name ~docstring ~@methods)
       ~@impls
       ;; matches the clojure.core behavior:
       ~(list 'quote name))))
