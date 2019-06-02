# nedap.utils.spec [![CircleCI](https://circleci.com/gh/nedap/utils.spec.svg?style=svg&circle-token=5895f9f338cb751d2c2e8a24844d82e21228190e)](https://circleci.com/gh/nedap/utils.spec)

Utilities for [clojure.spec](https://github.com/clojure/spec.alpha).

## Installation

```clojure
[com.nedap.staffing-solutions/utils.spec "0.7.0"]
```

> Remember to set [`*assert*`](https://github.com/technomancy/leiningen/blob/9981ae9086a352caf13a42bff4a7e43faa850452/sample.project.clj#L286) to `false` in your production environment!

## Synopsis

Despite the name, this is not a library with disparate spec-related functions (maybe, accordingly, it will be renamed to `speced.def`).

Rather, it is focused in 'speced' forms of `defn`, `defprotocol` ([and](https://github.com/nedap/utils.spec/issues/34) soon [others](https://github.com/nedap/utils.spec/issues/25)) using the **same exact syntax** than clojure.core's.

That is achieved via metadata:

```clojure

(spec/def ::int int?)

;; You can pass functions or keywords indifferently, as metadata:
(speced/defn ^string? inc-and-serialize [^::int n]
  (-> n inc str))
```

...the snippet above compiles to something akin to:

```clojure
(defn inc-and-serialize [n]
  {:pre [(int? n)]
   :post [(string? %)]}
  (-> n inc str))
```

> [Expound](https://github.com/bhb/expound) is used for error reporting, so the actual emitted code is a bit more substantial.

**utils.spec**'s philosophy is to bypass [instrumentation](https://clojure.org/guides/spec#_instrumentation_and_testing) altogether. Clojure's precondition system is simple and reliable, and can be cleanly [toggled](https://github.com/technomancy/leiningen/blob/18a316e1c116295555a77ce77a0d8f5971bc16f7/sample.project.clj#L286) for dev/prod environments via the `clojure.core/*assert*` variable.

> In a future, we might provide a way to build your own `defn`, tweaking subjective aspects like instrumentation, while preserving all other features at no cost.

## Highlights

* 1:1 syntax mapping to clojure.core's
  * No IDE pains, nothing new to learn, trivial upgradeability from old `defn`s
  * Things like N-arities are supported.
* Multiple metadata-based 'syntaxes'
  * [Descriptions/examples](https://github.com/nedap/utils.spec/blob/master/src/nedap/utils/spec/specs.clj)
  * All of them clean: no ns pollution, no overly-concise names
* Type hints become specs
  *  e.g.`^Boolean x` is analog to `^boolean? x`
    * emits safe _and_ efficient code, addressing an old complaint about Clojure's type hinting mechanism ("it doesn't enforce types").
    * You can `^::speced/nilable ^Boolean x` if something can be nil.
* `speced/defprotocol` is also supported
  * Same syntax and advantages as `speced/defn`

## Status and roadmap

* Battle-tested across a variety of projects by now
  * Rough edges polished at this point, each bugfix being thoroughly unit-tested.
* No ClojureScript support currently
  * Will be there one day
* Richer features will be added 
  * Particularly those related to [destructuring](https://github.com/nedap/utils.spec/issues?utf8=%E2%9C%93&q=is%3Aissue+is%3Aopen+destructuring): it will be incredibly concise to infer specs out of ns-qualifed destructurings
  * You may add `speced/` to existing defns, and benefit from automatic increased safety.
  * Also emitting type hints [out of plain specs](https://github.com/nedap/utils.spec/issues/39) will be quite a game changer.

Refer to the [issue tracker](https://github.com/nedap/utils.spec/issues) for getting an idea of the current bugs (quite minor) and wishlist.

## ns organisation

There are exactly 3 namespaces meant for public consumption:

* `nedap.utils.speced`: 'speced' forms of defprotocol, defn, etc.
* `nedap.utils.spec.api`: various utility functions, most notably `check!`.
* `nedap.utils.spec.predicates`: selected, generic predicates that you might find handy when specing things.

They are deliberately thin so you can browse them comfortably.

## Documentation

Please browse the public namespaces, which are documented, speced and tested.

## License

Copyright © Nedap

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
