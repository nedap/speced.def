(ns nedap.utils.speced
  "Speced variations of clojure.core `def` forms.

  Please `:require` this namepace with the `speced` alias: `[nedap.utils.speced :as speced]`.

  That way, you will invoke e.g. `speced/defprotocol` which is clean and clear."
  (:refer-clojure :exclude [defn defprotocol fn satisfies?])
  (:require
   #?(:clj [clojure.test :as test])
   #?(:clj [clojure.core.specs.alpha :as specs] :cljs [cljs.core.specs.alpha :as specs])
   #?(:clj [nedap.utils.spec.impl.fn :as impl.fn] :cljs [nedap.utils.spec.impl.dummy :as impl.fn])
   [clojure.spec.alpha :as spec]
   [nedap.utils.spec.api :refer [check!]]
   [nedap.utils.spec.doc :refer [doc-registry doc-registry-as-symbols]]
   [nedap.utils.spec.impl.def-with-doc]
   [nedap.utils.spec.impl.defn :as impl.defn]
   [nedap.utils.spec.impl.defprotocol]
   [nedap.utils.spec.impl.doc :as impl.doc]
   [nedap.utils.spec.impl.satisfies]
   [nedap.utils.spec.impl.spec-assertion])
  #?(:cljs (:require-macros [nedap.utils.speced :refer [def-with-doc]])))

#?(:clj
   (defmacro def-with-doc
     "Performs a plain `clojure.spec.alpha/def` with the given arguments.
  The docstring will be registered to `#'doc-registry`, and will be returned by `#'doc`."
     [spec-name docstring spec]
     {:pre [(check! qualified-keyword? spec-name
                    string?            docstring
                    some?              spec)]}
     (let [ref `doc-registry
           ref2 `doc-registry-as-symbols]
       `(nedap.utils.spec.impl.def-with-doc/def-with-doc ~spec-name ~docstring ~spec ~ref ~ref2))))

(defmacro doc
  "Like `clojure.repl/doc`, but also prints a spec's docstring (as per `#'def-with-doc`) if one existed."
  [x]
  (impl.doc/impl x
                 (-> &env :ns nil?)
                 doc-registry))

#?(:clj
   (defmacro defprotocol
     "Emits a spec-backed defprotocol, which uses `nedap.utils.spec.api/check!` at runtime
  to verify that specs of return values and arguments satify the (optional) specs passed as metadata.

  Has the exact same signature as `clojure.core/defprotocol`, with the constraint that docstrings are mandatory.

  Each method name, and each argument, observes spec metadata as per the `:nedap.utils.spec.specs/spec-metadata` spec.

  The implementation is backed by Clojure's `:pre`/`:post`, therefore runtime-checking behavior is controlled with `*assert*``.

  When implementing the protocol, each method must be prefixed with `--`.

  When invoking a protocol method, use the original names, without `--` prefix."
     {:style/indent        [1 :defn]
      :style.cljfmt/indent [[:block 1] [:inner 1]]}
     [name docstring & methods]
     `(nedap.utils.spec.impl.defprotocol/defprotocol ~name ~docstring ~@methods)))

#?(:clj
   (defmacro defn
     "Emits a spec-backed defn, which uses `nedap.utils.spec.api/check!` at runtime
  to verify that specs of return values and arguments satify the (optional) specs passed as metadata.

  Has the exact same signature as `clojure.core/defn`, with full support for all its variations.

  Each return value position, and each argument, observes spec metadata as per the `:nedap.utils.spec.specs/spec-metadata`` spec.

  The implementation is backed by Clojure's `:pre`/`:post`, therefore runtime-checking behavior is controlled with `*assert*``."
     {:style/indent        :defn
      :style.cljfmt/indent [[:inner 0]]}
     [& args]
     {:pre [(check! ::specs/defn-args args)]}
     (impl.defn/impl (-> &env :ns nil?)
                     args)))

#?(:clj
   (defmacro fn
     "Emits a spec-backed fn, which uses `nedap.utils.spec.api/check!` at runtime
  to verify that specs of return values and arguments satify the (optional) specs passed as metadata.

  Has the exact same signature as `clojure.core/fn`, with full support for all its variations.

  Each return value position, and each argument, observes spec metadata as per the `:nedap.utils.spec.specs/spec-metadata`` spec.

  The implementation is backed by Clojure's `:pre`/`:post`, therefore runtime-checking behavior is controlled with `*assert*``."
     {:style/indent        :defn
      :style.cljfmt/indent [[:inner 0]]}
     [& args]
     {:pre [(check! ::impl.fn/fn args)]}
     (impl.fn/impl (-> &env :ns nil?)
                   args)))

#?(:clj
   (clojure.core/defn satisfies?
     "Returns true if `x` implements `protocol`.

  Behaves exactly as its clojure.core counterpart, except that it also checks for metadata-based implementations.

  Note that matching clojure.core's behavior also means that `true` will be returned for _partial _metadata-based implementations.

  Works around https://dev.clojure.org/jira/browse/CLJ-2426."
     [protocol x]
     (nedap.utils.spec.impl.satisfies/satisfies? protocol x)))

(def-with-doc ::nilable
  "Can be summed to an existing spec (also passed as metadata),
for indicating that that spec is nilable."
  any?)

#?(:clj
   (defmethod test/assert-expr 'spec-assertion-thrown? [msg form]
     ;; (is (spec-assertion-thrown? s expr))
     ;; Asserts that evaluating expr throws an ExceptionInfo related to spec-symbol s.
     ;; Returns the exception thrown.
     (let [spec-sym (second form)
           body     (nthnext form 2)]
       (nedap.utils.spec.impl.spec-assertion/spec-assertion-thrown? msg spec-sym body))))
