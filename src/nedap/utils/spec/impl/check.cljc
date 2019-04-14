(ns nedap.utils.spec.impl.check
  (:require
   #?(:clj [clojure.spec.alpha :as spec] :cljs [cljs.spec.alpha :as spec])
   [expound.alpha :as expound])
  #?(:cljs (:require-macros [nedap.utils.spec.impl.check])))

(defmacro check!
  [& args]
  (let [cljs (find-ns 'cljs.analyzer)
        valid (if cljs
                'cljs.spec.valid?
                'clojure.spec.valid?)
        explain (if cljs
                  'cljs.spec.explain-str
                  'clojure.spec.explain-str)]
    `(do
       (doseq [[spec# x# spec-quoted# x-quoted#] ~(mapv (fn [[a b]]
                                                          [a
                                                           b
                                                           (list 'quote a)
                                                           (list 'quote b)])
                                                        (partition 2 args))]
         (or (~valid spec# x#)
             (do
               (cond-> (expound.alpha/expound-str spec# x#)
                 (not= x# x-quoted#)       (clojure.string/replace-first "should satisfy"
                                                                         (str "evaluated from\n\n  "
                                                                              (pr-str x-quoted#)
                                                                              "\n\nshould satisfy"))
                 (not= spec# spec-quoted#) (clojure.string/replace-first "-------------------------"
                                                                         (str "evaluated from\n\n  "
                                                                              (pr-str spec-quoted#)
                                                                              "\n\n-------------------------"))
                 true                      println)
               (throw (ex-info "Validation failed" {:spec         spec-quoted#
                                                    :faulty-value x-quoted#
                                                    :explanation  (~explain spec# x#)})))))
       true))
  true)
