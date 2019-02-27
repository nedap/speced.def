(ns nedap.utils.speced
  "Speced variations of clojure.core `def` forms.

  Please `:require` this namepace with the `speced` alias: `[nedap.utils.speced :as speced]`.

  That way, you will invoke e.g. `speced/defprotocol` which is clean and clear."
  (:refer-clojure :exclude [defn defprotocol])
  (:require
   [clojure.core.specs.alpha :as specs]
   [nedap.utils.spec.api :refer [check!]]
   [nedap.utils.spec.impl.def-with-doc]
   [nedap.utils.spec.impl.defn :as impl.defn]
   [nedap.utils.spec.impl.defprotocol :as impl.defprotocol]))

(defmacro def-with-doc
  "Performs a plain `clojure.spec.alpha/def` with the given arguments.
  The docstring argument is omitted. Its purpose is to show up for both human readers, and tooling."
  [spec-name docstring spec]
  {:pre [(check! qualified-keyword? spec-name
                 string? docstring
                 some? spec)]}
  `(nedap.utils.spec.impl.def-with-doc/def-with-doc ~spec-name ~docstring ~spec))

(defmacro defprotocol
  "Emits a spec-backed defprotocol, which uses `nedap.utils.spec.api/check!` at runtime
  to verify that specs of return values and arguments satify the (optional) specs passed as metadata.

  Has the exact same signature as `clojure.core/defprotocol`, with the constraint that docstrings are mandatory.

  Each method name, and each argument, observes spec metadata as per the `:nedap.utils.spec.specs/spec-metadata` spec.

  The implementation is backed by Clojure's `:pre`/`:post`, therefore runtime-checking behavior is controlled with `*assert*``.

  When implementing the protocol, each method must be prefixed with `--`.

  When invoking a protocol method, use the original names, without `--` prefix."
  {:style/indent [1 :defn]
   :style.cljfmt/indent [[:block 1] [:inner 1]]}
  [name docstring & methods]
  `(nedap.utils.spec.impl.defprotocol/defprotocol ~name ~docstring ~@methods))

(defmacro defn
  "Emits a spec-backed defn, which uses `nedap.utils.spec.api/check!` at runtime
  to verify that specs of return values and arguments satify the (optional) specs passed as metadata.

  Has the exact same signature as `clojure.core/defn`, with full support for all its variations.

  Each return value position, and each argument, observes spec metadata as per the `:nedap.utils.spec.specs/spec-metadata`` spec.

  The implementation is backed by Clojure's `:pre`/`:post`, therefore runtime-checking behavior is controlled with `*assert*``."
  {:style/indent :defn
   :style.cljfmt/indent [[:inner 0]]}
  [& args]
  {:pre [(check! ::specs/defn-args args)]}
  (impl.defn/impl args))
