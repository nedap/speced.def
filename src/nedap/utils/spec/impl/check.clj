(ns nedap.utils.spec.impl.check
  (:require
   [clojure.spec.alpha :as spec]
   [expound.alpha :as expound]))

(defmacro check!
  [& args]
  `(do
     (doseq [[spec# x# spec-quoted# x-quoted#] ~(mapv (fn [[a b]]
                                                        [a
                                                         b
                                                         (list 'quote a)
                                                         (list 'quote b)])
                                                      (partition 2 args))]
       (or (clojure.spec.alpha/valid? spec# x#)
           (do
             (cond-> (expound.alpha/expound-str spec# x#)
               (not= x# x-quoted#) (clojure.string/replace-first "should satisfy"
                                                                 (str "evaluated from\n\n  "
                                                                      (pr-str x-quoted#)
                                                                      "\n\nshould satisfy"))
               (not= spec# spec-quoted#) (clojure.string/replace-first "-------------------------"
                                                                       (str "evaluated from\n\n  "
                                                                            (pr-str spec-quoted#)
                                                                            "\n\n-------------------------"))
               true println)
             (throw (ex-info "Validation failed" {:spec spec-quoted#
                                                  :faulty-value x-quoted#
                                                  :explanation (clojure.spec.alpha/explain-str spec# x#)})))))
     true))
