(ns nedap.utils.spec.api
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   #?(:clj [clojure.test :as test] :cljs [cljs.test :as test])
   #?(:cljs [cljs.core :refer [ExceptionInfo]])
   [nedap.utils.spec.impl.check]
   [spec-coerce.core :as coerce])
  #?(:cljs (:require-macros [nedap.utils.spec.api :refer [check!]]))
  #?(:clj (:import (clojure.lang ExceptionInfo))))

#?(:clj
   (defmacro check!
     "Asserts validity, explaining the cause otherwise. Apt for :pre conditions.

  `args` is a sequence of spec-val pairs."
     [& args]
     {:pre [(-> args count even?)]}
     `(nedap.utils.spec.impl.check/check! ~@args)))

(defn coerce-map-indicating-invalidity
  "Tries to coerce the map `m` according to spec `spec`.

  If the coercion isn't possible, `::invalid? true` is associated to the map."
  [spec m]
  ;; Very important: specs must be passed as keywords or symbols,
  ;; but never 'inline' as any other kind of objects.
  ;; Else spec-coerce will fail to coerce things.
  {:pre [(check! qualified-ident? spec
                 map?             m)]}
  (let [m (coerce/coerce spec m)]
    (cond-> m
      (not (spec/valid? spec m)) (assoc ::invalid? true))))

; note that cljs.test/assert-expr expects 3 params, clojure.test/assert-expr 2
(defmethod test/assert-expr 'check-violated? [& params]
  ;; (is (check-violated? s expr))
  ;; Asserts that evaluating expr throws an ExceptionInfo related to spec-symbol s.
  (let [msg      (last (butlast params))
        form     (last params)
        spec-sym (second form)
        body     (nthnext form 2)]
    `(try
       (with-out-str ; silence output
         ~@body)
       (test/do-report {:type :fail, :message ~msg
                        :expected '~spec-sym, :actual nil})
       (catch ExceptionInfo e#
         (let [spec# (:spec (ex-data e#))]
           (if (= spec# ~spec-sym)
             (test/do-report {:type :pass, :message ~msg
                              :expected '~spec-sym, :actual nil})
             (test/do-report {:type :fail, :message ~msg
                              :expected '~spec-sym, :actual spec#})))))))
