(ns unit.nedap.test-helpers
  (:require
   [nedap.speced.def :as speced]))

(speced/defn every-and-at-least-one? [^ifn? pred, ^coll? coll]
  (and (seq coll)
       (every? pred coll)))
