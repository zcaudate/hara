(ns hara.print.pretty.dispatch-test
  (:use hara.test)
  (:require [hara.print.pretty.dispatch :refer :all]
            [hara.print.pretty :as printer]))

^{:refer hara.print.pretty.dispatch/chained-lookup :added "3.0"}
(fact "chains two or more lookups together"

  (chained-lookup
   (inheritance-lookup printer/clojure-handlers)
   (inheritance-lookup printer/java-handlers)))

^{:refer hara.print.pretty.dispatch/inheritance-lookup :added "3.0"}
(fact "checks if items inherit from the handlers"

  ((inheritance-lookup printer/clojure-handlers)
   clojure.lang.Atom)
  => fn?

  ((inheritance-lookup printer/clojure-handlers)
   String)
  => nil)
