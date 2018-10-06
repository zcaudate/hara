(ns hara.protocol.print-test
  (:use hara.test)
  (:require [hara.protocol.print :refer :all]))

^{:refer hara.protocol.print/-serialize-node :added "3.0"}
(fact "an extendable method for defining serializing tags")

^{:refer hara.protocol.print/-document :added "3.0"}
(fact "representation of a printable document object")

^{:refer hara.protocol.print/-text :added "3.0"}
(fact "representation of a printable text object")
