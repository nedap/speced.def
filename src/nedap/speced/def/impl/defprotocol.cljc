(ns nedap.speced.def.impl.defprotocol
  (:refer-clojure :exclude [defprotocol])
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   [clojure.walk :as walk]
   [nedap.speced.def.impl.analysis :refer [process-name-and-tails]]
   [nedap.speced.def.impl.parsing :refer [extract-specs-from-metadata]]
   [nedap.speced.def.impl.type-hinting :refer [ann->symbol ensure-proper-type-hints primitive? type-hint type-hint?]]
   [nedap.utils.spec.impl.check :refer [check!]])
  #?(:cljs (:require-macros [nedap.speced.def.impl.defprotocol]))
  #?(:clj (:import (clojure.lang IMeta))))

(def assert-not-primitively-hinted-message
  "Primitive type hints for protocols are unsupported. See https://dev.clojure.org/jira/browse/CLJ-1548")

(defn assert-not-primitively-hinted! [x clj?]
  {:pre [(boolean? clj?)]}
  (assert (not (-> x meta :tag (primitive? clj?)))
          assert-not-primitively-hinted-message)
  true)

(spec/def ::method-name symbol?)
(spec/def ::docstring string?)
(spec/def ::arg (spec/and symbol?
                          (complement #{'&})))
(spec/def ::args (spec/coll-of ::arg :kind vector :min-count 1))

(spec/def ::method (spec/and list?
                             (spec/cat :name      ::method-name
                                       :args      (spec/+ ::args)
                                       :docstring ::docstring)))

(defn emit-method [clj? [method-name args docstring :as method]]
  {:pre [(check! true
           ::method method)]}
  (assert-not-primitively-hinted! method-name clj?)
  (->> args (walk/postwalk (fn [x]
                             (when (instance? IMeta x)
                               (assert-not-primitively-hinted! x clj?))
                             x)))
  (let [ret-metadata (merge (meta method-name)
                            (meta args))
        {ret-spec :spec
         ^Class
         ret-ann  :type-annotation} (-> ret-metadata (extract-specs-from-metadata clj?) first)
        args-sigs (map (fn [arg arg-meta]
                         (merge {:arg arg}
                                (-> arg-meta (extract-specs-from-metadata clj?) first)))
                       args
                       (map meta args))
        tag (if clj?
              (ann->symbol ret-ann)
              ret-ann)
        tag? (some-> tag (type-hint? clj?))
        impl (cond-> (->> method-name (str "--") symbol)
               tag (vary-meta assoc :tag tag))
        new-method-name (cond-> method-name
                          tag?       (vary-meta assoc :tag (list 'quote tag))
                          (not tag?) (vary-meta dissoc :tag))
        type-hinted-args (type-hint args args-sigs)
        args-with-proper-tag-hints (ensure-proper-type-hints clj? type-hinted-args)
        impl-tail (let [v (process-name-and-tails {:name method-name
                                                   :tail [args (apply list impl type-hinted-args)]
                                                   :clj? clj?})]
                    (assert (-> v :tails count #{1}))
                    (-> v :tails first vec (assoc 0 args-with-proper-tag-hints) (seq)))]
    {:method-name          new-method-name
     :protocol-method-name impl
     :docstring            docstring
     :impl-tail            impl-tail
     :proto-tail           args-with-proper-tag-hints}))

(defn extract-signatures [method]
  {:pre [(check! true
           ::method method)]}
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
  [clj? group]
  (let [{:keys [method-name docstring protocol-method-name]} (first group)
        reduced (->> group
                     (reduce (fn [acc {:keys [method-name docstring impl-tail proto-tail]}]
                               (-> acc
                                   (update :fn append-to-list impl-tail)
                                   (update :proto-decl append-to-list proto-tail)))
                             {:fn              (list (if clj?
                                                       'clojure.core/defn
                                                       'cljs.core/defn)
                                                     method-name
                                                     docstring)
                              :proto-decl      (list protocol-method-name)
                              :proto-docstring docstring}))]
    (-> reduced
        (assoc :impls [(:fn reduced)])
        (assoc :methods [(:proto-decl reduced)])
        (update-in [:methods 0] append-to-list (:proto-docstring reduced))
        (dissoc :fn :proto-decl :proto-docstring))))

#?(:clj
   (defmacro defprotocol [name docstring & methods]
     {:pre [(check! true
              symbol? name
              string? docstring
              ;; (`methods` are already checked in emit-method)
              )]}
     (let [clj? (-> &env :ns nil?)
           impl (fn [name docstring methods]
                  (let [{:keys [impls methods] :as x} (->> methods
                                                           (mapcat extract-signatures)
                                                           (map (partial emit-method clj?))
                                                           (group-by :method-name)
                                                           (vals)
                                                           (map (partial consolidate-group clj?))
                                                           (apply merge-with into))
                        v `(do
                             (clojure.core/defprotocol ~name
                               ~docstring
                               :extend-via-metadata true
                               ~@methods)
                             ~@impls
                             ;; matches the clojure.core behavior:
                             ~(list 'quote name))]
                    ;; hack around mistery https://dev.clojure.org/jira/browse/CLJS-3072 :
                    (if clj?
                      v
                      (read-string (pr-str v)))))]
       (impl name docstring methods))))
