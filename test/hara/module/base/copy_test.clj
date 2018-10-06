(ns hara.module.base.copy-test
  (:use hara.test)
  (:require [hara.module.base.copy :refer :all]))

^{:refer hara.module.base.copy/source-name :added "3.0"}
(fact "determines the source name of a pair of single"

  (source-name 'hello) => 'hello

  (source-name '(sink < source)) => 'source)

^{:refer hara.module.base.copy/sink-name :added "3.0"}
(fact "determines the sink name of a pair of single"

  (sink-name 'hello) => 'hello

  (sink-name '(sink < source)) => 'sink)

^{:refer hara.module.base.copy/copy-single :added "3.0"}
(fact "Imports a single var from to a given namespace"

  (copy-single *ns* 'ifl #'clojure.core/if-let)
  => anything ; #'hara.module-test/ifl
  (eval '(ifl [a 1] (inc a))) => 2)

^{:refer hara.module.base.copy/unmap-vars :added "3.0"}
(fact "unmaps a set of vars from a given namespace"

  (unmap-vars *ns* ['ifl])
  => '[ifl])

^{:refer hara.module.base.copy/copy-vars :added "3.0"}
(fact "copies vars from one namespace to another"

  (copy-vars 'hara.core.base.check '[bytes? atom?] *ns* copy-single)
  => [#'bytes?
      #'atom?])

^{:refer hara.module.base.copy/copy :added "3.0"}
(fact "copies a set of vars from multiple locations"

  (copy '[[hara.core.base.check bytes? (bytes?2 < bytes?)]
          [hara.core.base.util T F]])
  => [#'bytes? #'bytes?2 #'T #'F])
