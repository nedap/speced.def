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
         (let [{ex-data-spec#        :spec
                ex-data-spec-object# :spec-object
                ex-data-quoted-spec# :quoted-spec
                :as                  ex-data-object#} (ex-data e#)
               legacy?# (not (contains? ex-data-object# :spec-object))
               spec# (if legacy?#
                       ex-data-spec#
                       ex-data-spec-object#)
               inferred-specs# (->> (extract-specs-from-metadata {:tag ~spec-sym} ~clj?)
                                    (map :spec)
                                    set)]

           ;; rethrow if no spec failure is found
           (when-not spec#
             (throw e#))

           (if (or (if legacy?#
                     (= ~spec-sym spec#)
                     (or (= ex-data-quoted-spec# ~spec-sym)
                         (= ex-data-spec-object# ~spec-sym)))
                   (contains? inferred-specs# (if legacy?#
                                                spec#
                                                ex-data-quoted-spec#)))
             (~reporter {:type :pass, :message ~msg :expected '~spec-sym, :actual nil})
             (~reporter {:type :fail, :message ~msg :expected '~spec-sym, :actual spec#}))
           e#)))))
