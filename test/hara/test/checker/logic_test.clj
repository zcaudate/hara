(ns hara.test.checker.logic-test
  (:use [hara.test :exclude [any all is-not]])
  (:require [hara.test.checker.logic :refer :all]
            [hara.test.common :as common]))

^{:refer hara.test.checker.logic/is-not :added "3.0"}
(fact "checker that allows negative composition of checkers"

  (mapv (is-not even?)
        [1 2 3 4 5])
  => [true false true false true])

^{:refer hara.test.checker.logic/any :added "3.0"}
(fact "checker that allows `or` composition of checkers"

  (mapv (any even? 1)
        [1 2 3 4 5])
  => [true true false true false])

^{:refer hara.test.checker.logic/all :added "3.0"}
(fact "checker that allows `and` composition of checkers"

  (mapv (all even? #(< 3 %))
        [1 2 3 4 5])
  => [false false false true false])