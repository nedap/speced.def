(ns nedap.speced.def.impl.spec-assertion
  (:require
   [clojure.test :as test]
   [nedap.speced.def.impl.parsing :refer [extract-specs-from-metadata]]
   [nedap.utils.spec.api :refer [check!]])
  (:import
   (clojure.lang ExceptionInfo)))

(defn spec-assertion-thrown? [clj? msg spec-sym body]
  {:pre [(check! boolean?    clj?
                 some?       spec-sym
                 sequential? body)]}
  (let [reporter (if clj?
                   `test/do-report
                   'cljs.test/do-report)]
    `(try
       (with-out-str ;; silently execute body
         ~@body)
       (~reporter {:type :fail, :message ~msg :expected '~spec-sym, :actual "no spec failure"})
       (catch ~(if clj?
                 `ExceptionInfo
                 'ExceptionInfo) e#
         (let [spec# (:spec (ex-data e#))
               inferred-specs# (set (map :spec (extract-specs-from-metadata {:tag ~spec-sym} ~clj?)))]

           ;; rethrow if no spec failure is found
           (when-not spec#
             (throw e#))

           (if (or (= ~spec-sym spec#)
                   (contains? inferred-specs# spec#))
             (~reporter {:type :pass, :message ~msg :expected '~spec-sym, :actual nil})
             (~reporter {:type :fail, :message ~msg :expected '~spec-sym, :actual spec#}))
           e#)))))
