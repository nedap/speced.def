(ns nedap.speced.def.impl.fn
  (:require
   [clojure.core.specs.alpha :as specs]
   [clojure.spec.alpha :as spec]
   [nedap.speced.def.impl.defn :as impl.defn]
   [nedap.utils.spec.api :refer [check!]]))

;; Taken from https://git.io/fjuA8 , which is not offered reusably as of today
(spec/def ::fn (spec/cat :fn-name (spec/? simple-symbol?)
                         :fn-tail (spec/alt :arity-1 ::specs/params+body
                                            :arity-n (spec/+ (spec/spec ::specs/params+body)))))

(defn impl
  [clj? args]
  {:pre [(check! boolean? clj?
                 ::fn     args)]}
  (let [maybe-name (let [x (first args)]
                     (when (symbol? x)
                       x))
        tail (if maybe-name
               (rest args)
               args)
        {:keys [tails name docstring-and-meta]} (impl.defn/process-name-and-tails {:tail tail
                                                                                   :name maybe-name
                                                                                   :clj? clj?})]
    (cond->> tails
      name (concat [name])
      true (apply list `fn))))
