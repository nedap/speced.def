# nedap.utils.spec

Utilities for [clojure.spec](https://github.com/clojure/spec.alpha).

## installation

```clojure
[com.nedap.staffing-solutions/utils.spec "0.5.0"]
````

## ns organisation

There are exactly 3 namespaces meant for public consumption:

* `nedap.utils.spec.api`: various utility functions, most notably `check!` which is meant to be used in conjunction with clojure's `:pre` and `:post`. 
* `nedap.utils.speced`: 'speced' forms of defprotocol, defn, etc.
* `nedap.utils.predicates`: generic predicates that you might find handy when specing things.

They are deliberately thin so you can browse them comfortably.

## Documentation

Please browse the public namespaces, which are documented, speced and tested.

## License

Copyright © Nedap

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
