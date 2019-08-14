(ns nedap.speced.def.impl.defn
  (:require
   #?(:clj [clojure.core.specs.alpha :as specs] :cljs [cljs.core.specs.alpha :as specs])
   [nedap.speced.def.impl.analysis :refer [process-name-and-tails]]
   [nedap.utils.spec.api #?(:clj :refer :cljs :refer-macros) [check!]]))

(defn impl
  [clj? [name & tail :as args]]
  {:pre [(check! boolean?          clj?
                 ::specs/defn-args args)]}
  (let [{:keys [tails name docstring-and-meta]} (process-name-and-tails {:tail tail
                                                                         :name name
                                                                         :clj? clj?})]
    (apply list `clojure.core/defn (cons name (concat docstring-and-meta tails)))))
