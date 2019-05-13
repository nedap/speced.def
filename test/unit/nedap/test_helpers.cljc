(ns unit.nedap.test-helpers
  (:require
   [nedap.utils.speced :as speced]))

(speced/defn every-and-at-least-one? [^ifn? pred, ^coll? coll]
  (and (seq coll)
       (every? pred coll)))
