(ns nedap.utils.spec.impl.check
  (:require
   [clojure.spec.alpha :as spec]
   [expound.alpha :as expound]))

(defmacro check!
  [& args]
  `(do
     (doseq [[spec# x# x-quoted#] ~(mapv (fn [[a b]]
                                           [a b (list 'quote b)])
                                         (partition 2 args))]
       (or (clojure.spec.alpha/valid? spec# x#)
           (do
             (-> (expound.alpha/expound-str spec# x#)
                 (clojure.string/replace-first "should satisfy"
                                               (str "evaluated from\n\n  " (pr-str x-quoted#) "\n\nshould satisfy"))
                 println)
             (throw (ex-info "Validation failed" {:explanation (clojure.spec.alpha/explain-str spec# x#)})))))
     true))
