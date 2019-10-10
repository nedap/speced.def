(ns nedap.speced.def
  "Speced variations of clojure.core `def` forms.

  Please `:require` this namepace with the `speced` alias: `[nedap.speced.def :as speced]`.

  That way, you will invoke e.g. `speced/defprotocol` which is clean and clear."
  (:refer-clojure :exclude [defn defprotocol doc fn let letfn satisfies?])
  (:require
   #?(:clj [clojure.test :as test])
   #?(:clj [nedap.speced.def.impl.fn :as impl.fn] :cljs [nedap.speced.def.impl.dummy :as impl.fn])
   #?(:clj [clojure.core.specs.alpha :as specs] :cljs [cljs.core.specs.alpha :as specs])
   #?(:clj [nedap.speced.def.impl.letfn :as impl.letfn])
   #?(:clj [nedap.speced.def.impl.let-impl :as let-impl])
   [clojure.spec.alpha :as spec]
   [nedap.speced.def.doc :refer [doc-registry rebl-doc-registry]]
   [nedap.speced.def.impl.def-with-doc]
   [nedap.speced.def.impl.defn :as impl.defn]
   [nedap.speced.def.impl.defprotocol]
   [nedap.speced.def.impl.doc :as impl.doc]
   [nedap.speced.def.impl.satisfies]
   [nedap.speced.def.impl.spec-assertion]
   [nedap.utils.spec.api :refer [check!]])
  #?(:cljs (:require-macros [nedap.speced.def :refer [def-with-doc]])))

#?(:clj
   (defmacro def-with-doc
     "Performs a plain `clojure.spec.alpha/def` with the given arguments.
  The docstring will be registered to `#'doc-registry`, and will be returned by `#'nedap.speced.def/doc`."
     [spec-name docstring spec]
     {:pre [(check! qualified-keyword? spec-name
                    string?            docstring
                    some?              spec)]}
     (clojure.core/let [ref `doc-registry
                        ref2 `rebl-doc-registry]
       `(nedap.speced.def.impl.def-with-doc/def-with-doc ~spec-name ~docstring ~spec ~ref ~ref2))))

#?(:clj
   (defmacro doc
     "Like `clojure.repl/doc`, but also prints a spec's docstring (as per `#'def-with-doc`) if one existed."
     [x]
     (impl.doc/impl x
                    (-> &env :ns nil?)
                    doc-registry)))

#?(:clj
   (defmacro defprotocol
     "Emits a spec-backed defprotocol, which uses `nedap.utils.spec.api/check!` at runtime
  to verify that the return values and arguments satisfy the (optional) specs passed as metadata.

  Has the exact same signature as `clojure.core/defprotocol`, with the constraint that docstrings are mandatory.

  Each method name, and each argument, observes spec metadata as per the `:nedap.speced.def.specs/spec-metadata` spec.

  The implementation is backed by Clojure's `:pre`/`:post`, therefore runtime-checking behavior is controlled with `*assert*`.

  When implementing the protocol, each method must be prefixed with `--`.

  When invoking a protocol method, use the original names, without `--` prefix."
     {:style/indent        [1 :defn]
      :style.cljfmt/indent [[:block 1] [:inner 1]]}
     [name docstring & methods]
     `(nedap.speced.def.impl.defprotocol/defprotocol ~name ~docstring ~@methods)))

#?(:clj
   (defmacro defn
     "Emits a spec-backed defn, which uses `nedap.utils.spec.api/check!` at runtime
  to verify that the return values and arguments satisfy the (optional) specs passed as metadata.

  Has the exact same signature as `clojure.core/defn`, with full support for all its variations.

  Each return value position, and each argument, observes spec metadata as per the `:nedap.speced.def.specs/spec-metadata` spec.

  The implementation is backed by Clojure's `:pre`/`:post`, therefore runtime-checking behavior is controlled with `*assert*`."
     {:style/indent        :defn
      :style.cljfmt/indent [[:inner 0]]}
     [& args]
     {:pre [(check! ::specs/defn-args args)]}
     (impl.defn/impl (-> &env :ns nil?)
                     args)))

#?(:clj
   (defmacro fn
     "Emits a spec-backed fn, which uses `nedap.utils.spec.api/check!` at runtime
  to verify that the return values and arguments satisfy the (optional) specs passed as metadata.

  Has the exact same signature as `clojure.core/fn`, with full support for all its variations.

  Each return value position, and each argument, observes spec metadata as per the `:nedap.speced.def.specs/spec-metadata` spec.

  The implementation is backed by Clojure's `:pre`/`:post`, therefore runtime-checking behavior is controlled with `*assert*`."
     {:style/indent        :defn
      :style.cljfmt/indent [[:inner 0]]}
     [& args]
     {:pre [(check! ::impl.fn/fn args)]}
     (impl.fn/impl (-> &env :ns nil?)
                   args)))

#?(:clj
   (defmacro let
     "Emits a spec-backed `let`, which uses `nedap.utils.spec.api/check!` at runtime
  to verify that any metadata-annotated symbols satisfy the specs denoted by that metadata.

  Has the exact same signature as `#'clojure.core/let`, with full support for arbitrarily nested destructuring.

  Spec metadata follows the `:nedap.speced.def.specs/spec-metadata` 'syntaxes'.

  Runtime-checking behavior is controlled with `#'clojure.core/*assert*`."
     {:style/indent        1
      :style.cljfmt/indent [[:block 1]]}
     [bindings & body]
     {:pre [(check! ::specs/bindings bindings)]}
     (let-impl/impl (-> &env :ns nil?) bindings body)))

#?(:clj
   (defmacro letfn
     "Emits a spec-backed `letfn`, which uses `nedap.utils.spec.api/check!` at runtime
  to verify that any metadata-annotated symbols satisfy the specs denoted by that metadata.

  Has the exact same signature as `#'clojure.core/letfn`, with full support for arbitrarily nested destructuring and multiple arities.

  Spec metadata follows the `:nedap.speced.def.specs/spec-metadata` 'syntaxes'.

  Runtime-checking behavior is controlled with `#'clojure.core/*assert*`."
     {:style/indent        [1 [[:defn]] :form]
      :style.cljfmt/indent [[:block 1] [:inner 2 0]]}
     [fnspecs & body]
     (impl.letfn/impl (-> &env :ns nil?)
                      fnspecs
                      body)))

#?(:clj
   (clojure.core/defn satisfies?
     "Returns true if `x` implements `protocol`.

  Behaves exactly as its clojure.core counterpart, except that it also checks for metadata-based implementations.

  Note that matching clojure.core's behavior also means that `true` will be returned for _partial _metadata-based implementations.

  Works around https://dev.clojure.org/jira/browse/CLJ-2426."
     [protocol x]
     (nedap.speced.def.impl.satisfies/satisfies? protocol x)))

(def-with-doc ::nilable
  "Can be summed to an existing spec (also passed as metadata),
for indicating that that spec is nilable."
  any?)

#?(:clj
   (defmethod test/assert-expr 'spec-assertion-thrown? [msg form]
     ;; (is (spec-assertion-thrown? s expr))
     ;; Asserts that evaluating expr throws an ExceptionInfo related to spec-symbol s.
     ;; Returns the exception thrown.
     (clojure.core/let [spec-sym (second form)
                        body     (nthnext form 2)]
       (nedap.speced.def.impl.spec-assertion/spec-assertion-thrown? msg spec-sym body))))
