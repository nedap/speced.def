# speced.def [![CircleCI](https://circleci.com/gh/nedap/speced.def.svg?style=svg&circle-token=5895f9f338cb751d2c2e8a24844d82e21228190e)](https://circleci.com/gh/nedap/speced.def)

This library provides [spec](https://github.com/clojure/spec.alpha)-backed forms of `defn`, `defprotocol`, `fn`, `let` etc. using the **same exact syntax** as clojure.core's.

That way, you can strengthen your defns with custom specs (expressed as metadata), while avoiding the hassle of instrumentation, and gaining some extra benefits, such as better performance, error reporting, etc.

## Installation

#### Coordinates

```clojure
[com.nedap.staffing-solutions/speced.def "2.1.1"]
```

> Note that self-hosted ClojureScript (e.g. Lumo) is unsupported at the moment.

#### Production setup

* In JVM Clojure, set [`*assert*`](https://github.com/technomancy/leiningen/blob/9981ae9086a352caf13a42bff4a7e43faa850452/sample.project.clj#L286) to `false`.

* In ClojureScript, set [`:elide-asserts`](https://clojurescript.org/reference/compiler-options#elide-asserts) to `true`.

#### `clojure-future-spec` incompatibility

If your project happens to depend (directly or transitively; try e.g. `lein deps :tree`) on [clojure-future-spec](https://github.com/tonsky/clojure-future-spec), this library will fail to load.

You can fix that by adding e.g. `:exclusions [clojure-future-spec]` to whatever dependency was bringing it in.

You also will need to create an empty `clojure.future` ns.

## Synopsis

With `speced.def`, one expresses specs via metadata:

```clojure

(spec/def ::int int?)

;; You can pass functions, keywords, or (primitive) type hints indifferently, as metadata:
(speced/defn ^string? inc-and-serialize [^::int n, ^boolean b]
  (-> n inc str))
```

...the snippet above compiles to something akin to:

```clojure
;; note that both preconditions and type hints are emitted, derived from the specs
(defn inc-and-serialize ^String [n, ^boolean b]
  {:pre [(int? n) (boolean? b)]
   :post [(string? %)]}
  (-> n inc str))
```

> [Expound](https://github.com/bhb/expound) is used for error reporting, so the actual emitted code is a bit more substantial.

You can pass specs as part of any nested destructurings.  

```clojure
(speced/defn destructuring-example [{:keys [^string? a] :as ^::thing all}]
  ;; :pre conditions will be emitted for `a` and `all`
  )
```

**speced.def**'s philosophy is to bypass [instrumentation](https://clojure.org/guides/spec#_instrumentation_and_testing) altogether. Clojure's precondition system is simple and reliable, and can be cleanly [toggled](https://github.com/technomancy/leiningen/blob/18a316e1c116295555a77ce77a0d8f5971bc16f7/sample.project.clj#L286) for dev/prod environments via the `clojure.core/*assert*` variable.

> In a future, we might provide a way to build your own `defn`, tweaking subjective aspects like instrumentation, while preserving all other features at no cost.

## Highlights

* 1:1 syntax mapping to clojure.core's
  * No IDE pains, nothing new to learn, trivial upgradeability from old `defn`s
  * Things like N-arities are supported.
* Multiple metadata-based 'syntaxes'
  * [Descriptions/examples](https://github.com/nedap/speced.def/blob/master/src/nedap/speced/def/specs.cljc)
  * All of them clean: no ns pollution, no overly-concise names
* Type hints become specs
  *  e.g.`^Boolean x` is analog to `^boolean? x`
    * emits safe _and_ efficient code, addressing an old complaint about Clojure's type hinting mechanism ("it doesn't enforce types").
    * You can `^::speced/nilable ^Boolean x` if something can be nil.
* Inline function specs can become type hints
  * e.g. `^string?` will emit a `^String` type hint
  * same for ClojureScript: `^string?` -> `^string`
  * This particularly matters in JVM Clojure. [Refer to the full mapping](https://github.com/nedap/speced.def/blob/8dac678f498fc3a77ab7cc13e5a1b3d965221735/src/nedap/utils/spec/impl/parsing.cljc#L42).
* `speced/defprotocol`, `speced/fn`, `speced/let`, `speced/letfn` are also offered
  * Same syntax and advantages as `speced/defn`
* Richer Expound integration
  * [This idea](https://github.com/bhb/expound/issues/148) is implemented here inline, as long as the Expound issue remains open.
* Full ClojureScript support
  * Did you know Clojure and ClojureScript expect type hinting metadata [at different positions](https://git.io/fjuk7)?
    * **speced.def** abstracts over that, allowing you to write them wherever you please.
* `speced/def-with-doc`: `clojure.spec.alpha/def` with a docstring
  * It will show up in `speced/doc`, along with the spec itself
  * It also will show up in [REBL](https://github.com/cognitect-labs/REBL-distro).

## Status and roadmap

* Battle-tested across a variety of projects by now
  * Rough edges polished at this point, each bugfix being thoroughly unit-tested.
  * ClojureScript support is full (and fully unit-tested), but less battle-tested.
* Richer features will be added 
  * Refer to the [issue tracker](https://github.com/nedap/speced.def/issues) for getting an idea of the project's wishlist.

## ns organisation

There are exactly 2 namespaces meant for public consumption:

* `nedap.speced.def`: 'speced' forms of defprotocol, defn, fn, let, letfn, etc.
* `nedap.speced.def.doc`: a public docstring registry for specs. Can be queried from arbitrary tools, and particularly [REBL](https://github.com/cognitect-labs/REBL-distro).

They are deliberately thin so you can browse them comfortably.

## Documentation

Please browse the public namespaces, which are documented, speced and tested.

It is part of this library's philosophy to avoid creating external documentation. We believe specs, particularly with our `def-with-doc` helper, can suffice, preventing duplication/drift and non-speced/non-self-documenting code.

That assumes that adopters are willing to jump to the source (particularly to the public parts, and occasionally to the tests which serve as a large corpus of examples), and preferrably have handy tooling that is able to do such jumping.

## Testing

Occasionally, you might want to test that invoking certain `speced/` functionality throws a certain spec failure.

For that, `clojure.test/is`/`cljs.test/is` are extended with the `spec-assertion-thrown?` assertion. You can find example usages in our `unit.nedap.speced.def.spec-assertion` testing ns. 

> Note that clojure.test cannot be extended with complete cleanliness, so in a way requiring `speced.def` pollutes the global environment with the `spec-assertion-thrown?` assertion.
>
> In a future, we might add a way to disable this side-effect.

## [Contributing](https://github.com/nedap/speced.def/blob/master/.github/contributing.md)

## License

Copyright © Nedap
This program and the accompanying materials are made available under the terms of the [Eclipse Public License 2.0](https://www.eclipse.org/legal/epl-2.0)
