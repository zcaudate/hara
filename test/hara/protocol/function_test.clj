(ns hara.protocol.function-test
  (:use hara.test)
  (:require [hara.protocol.function :refer :all]))

^{:refer hara.protocol.function/-invoke-intern :added "3.0"}
(fact "extendable function for loading invoke form constructors"

  (-invoke-intern :fn '-hello- nil '([x] x))^:hidden
  => '(do (def -hello- (clojure.core/fn -hello- [x] x))
         (clojure.core/doto (var -hello-)
           (clojure.core/alter-meta! clojure.core/merge nil {:arglists (quote ([x]))}))))

^{:refer hara.protocol.function/-invoke-package :added "3.0"}
(fact "extendable function for loading invoke-intern types")
  
^{:refer hara.protocol.function/-fn-body :added "3.0"}
(fact "multimethod for defining anonymous function body"

  (-fn-body :clojure '([x] x))
  => '(clojure.core/fn [x] x))

^{:refer hara.protocol.function/-fn-package :added "3.0"}
(fact "extendable function for loading fn-body types")
