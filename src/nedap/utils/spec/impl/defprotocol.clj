(ns nedap.utils.spec.impl.defprotocol
  (:refer-clojure :exclude [defprotocol])
  (:require
   [clojure.spec.alpha :as spec]
   [nedap.utils.spec.impl.check :refer [check!]]
   [nedap.utils.spec.impl.parsing :refer [extract-specs-from-metadata]]
   [nedap.utils.spec.impl.type-hinting :refer :all]))

(spec/def ::method-name symbol?)
(spec/def ::docstring string?)
(spec/def ::args (spec/coll-of symbol? :kind vector :min-count 1))

(spec/def ::method (spec/and list?
                             (spec/cat :name ::method-name
                                       :args (spec/+ ::args)
                                       :docstring ::docstring)))

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
    {:method-name          method-name
     :protocol-method-name impl
     :docstring            docstring
     :declare              `(declare ~impl)
     :impl-tail            (list args-with-proper-tag-hints prepost (apply list impl args))
     :proto-tail           args-with-proper-tag-hints}))

(defn extract-signatures [method]
  {:pre [(check! ::method method)]}
  (let [name (first method)
        docstring (last method)
        argvs (remove #{name docstring} method)]
    (->> argvs
         (map (fn [argv]
                (list name argv docstring))))))

(defn append-to-list [base x]
  (apply concat (list base (list x))))

(defn consolidate-group
  "Builds the info for a single protocol method, out of a 'group', namely N signatures of the same method."
  [group]
  (let [{:keys [method-name docstring protocol-method-name]} (first group)
        reduced (->> group
                     (reduce (fn [acc {:keys [method-name docstring impl-tail declare proto-tail]}]
                               (-> acc
                                   (update :fn append-to-list impl-tail)
                                   (update :proto-decl append-to-list proto-tail)
                                   (update :declares conj declare)))
                             {:declares        #{}
                              :fn              (list 'defn method-name docstring)
                              :proto-decl      (list protocol-method-name)
                              :proto-docstring docstring}))]
    (-> reduced
        (assoc :impls [(:fn reduced)])
        (assoc :methods [(:proto-decl reduced)])
        (update-in [:methods 0] append-to-list (:proto-docstring reduced))
        (dissoc :fn :proto-decl))))

(defn impl [name docstring methods]
  (let [{:keys [impls methods declares] :as x} (->> methods
                                                    (mapcat extract-signatures)
                                                    (map emit-method)
                                                    (group-by :method-name)
                                                    (vals)
                                                    (map consolidate-group)
                                                    (apply merge-with into))]

    `(do
       ~@declares
       (clojure.core/defprotocol ~name
         ~docstring
         :extend-via-metadata true
         ~@methods)
       ~@impls
       ;; matches the clojure.core behavior:
       ~(list 'quote name))))

(defmacro defprotocol [name docstring & methods]
  {:pre [(check! symbol? name
                 string? docstring
                 ;; (`methods` are already checked in emit-method)
                 )]}
  (impl name docstring methods))
