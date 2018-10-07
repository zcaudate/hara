(ns hara.module.namespace.resolve-test
  (:use hara.test)
  (:require [hara.module.namespace.resolve :refer :all]))

^{:refer hara.module.namespace.resolve/resolve-ns :added "3.0"}
(fact "resolves the namespace or else returns nil if it does not exist"

  (resolve-ns 'clojure.core) => 'clojure.core

  (resolve-ns 'clojure.core/some) => 'clojure.core

  (resolve-ns 'clojure.hello) => nil)

^{:refer hara.module.namespace.resolve/ns-vars :added "3.0"}
(fact "lists the vars in a particular namespace"

  (ns-vars 'hara.module.namespace.resolve) => '[ns-vars resolve-ns])