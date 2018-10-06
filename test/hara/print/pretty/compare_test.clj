(ns hara.print.pretty.compare-test
  (:use hara.test)
  (:require [hara.print.pretty.compare :refer :all])
  (:refer-clojure :exclude [compare]))

^{:refer hara.print.pretty.compare/type-priority :added "3.0"}
(fact "creates a compareed list of items in an uncompareed collection"

  (type-priority 1) => 3

  (type-priority :hello) => 6

  (type-priority {}) => 11)

^{:refer hara.print.pretty.compare/compare-seqs :added "3.0"}
(fact "compares two sequences"
  
  (compare-seqs compare [1 2 3] [4 5 6])
  => -1)

^{:refer hara.print.pretty.compare/compare :added "3.0"}
(fact "compares any two values"

  (compare 1 :hello)
  => -1

  (compare  {:a 1} 3)
  => 1)
