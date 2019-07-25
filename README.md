# nedap.utils.spec

Utilities for [clojure.spec](https://github.com/clojure/spec.alpha).

## Installation

#### Coordinates

```clojure
[com.nedap.staffing-solutions/utils.spec "1.0.0-alpha2"]
```

> Note that self-hosted ClojureScript (e.g. Lumo) is unsupported at the moment.

#### Production setup

* In JVM Clojure, set [`*assert*`](https://github.com/technomancy/leiningen/blob/9981ae9086a352caf13a42bff4a7e43faa850452/sample.project.clj#L286) to `false`.

* In ClojureScript, set [`:elide-asserts`](https://clojurescript.org/reference/compiler-options#elide-asserts) to `true`.

## Documentation

Please browse the public namespaces, which are documented, speced and tested.

It is part of this library's philosophy to avoid creating external documentation. We believe specs, particularly with our `def-with-doc` helper, can suffice, preventing duplication/drift and non-speced/non-self-documenting code.

That assumes that adopters are willing to jump to the source (particularly to the public parts, and occasionally to the tests which serve as a large corpus of examples), and preferrably have handy tooling that is able to do such jumping.

## License

Copyright © Nedap

This program and the accompanying materials are made available under the terms of the Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0.
