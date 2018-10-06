(ns hara.module.base.source-test
  (:use hara.test)
  (:require [hara.module.base.source :refer :all]))

^{:refer hara.module.base.source/resource-path :added "3.0"}
(fact "converts a namespace to a resource path"

  (resource-path 'hara.test)
  => "hara/test.clj")

^{:refer hara.module.base.source/resource-url :added "3.0"}
(fact "returns a resource url"

  (resource-url "hara/test.clj")
  => java.net.URL)

^{:refer hara.module.base.source/ns-metadata :added "3.0"}
(fact "returns the metadata associated with a given namespace"

  (ns-metadata 'hara.test)
  => '{print-options {:arglists ([] [opts])},
       -main {:arglists ([& args])},
       run-errored {:arglists ([])}})
