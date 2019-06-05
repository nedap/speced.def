(ns nedap.utils.spec.impl.spec-assertion
  (:require
   #?(:cljs [cljs.core :refer [ExceptionInfo]])
   [clojure.test :as test]
   [nedap.utils.spec.impl.parsing :refer [infer-spec-from-symbol]])
  #?(:clj (:import (clojure.lang ExceptionInfo))))

(defn spec-assertion-thrown? [msg spec-sym body]
  `(try
     (with-out-str ~@body) ;; silently execute body
     (test/do-report {:type :fail, :message ~msg :expected '~spec-sym, :actual "no spec failure"})
     (catch ExceptionInfo e#
       (let [spec#          (:spec (ex-data e#))
             inferred-spec# (:spec (infer-spec-from-symbol true ~spec-sym))]

         ;; rethrow if no spec failure is found
         (when-not spec#
           (throw e#))

         (if (or (= ~spec-sym spec#)
                 (= inferred-spec# spec#))
           (test/do-report {:type :pass, :message ~msg :expected '~spec-sym, :actual nil})
           (test/do-report {:type :fail, :message ~msg :expected '~spec-sym, :actual spec#}))
         e#))))
