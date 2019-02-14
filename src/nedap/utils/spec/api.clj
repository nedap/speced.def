(ns nedap.utils.spec.api
  (:require
   [nedap.utils.spec.impl.check]))

(defmacro check!
  "Asserts validity, explaining the cause otherwise. Apt for :pre conditions.

  `args` is a sequence of spec-val pairs."
  [& args]
  `(nedap.utils.spec.impl.check/check! ~@args))
