(ns hara.test.checker.util-test
  (:use hara.test)
  (:require [hara.test.checker.util :refer :all]
            [hara.test.checker.base :as checker]))

^{:refer hara.test.checker.util/contains-exact :added "3.0"}
(fact "checks if a sequence matches exactly"
  (contains-exact [0 1 2 3] (map checker/->checker [1 2 3]))
  => true

  (contains-exact [0 1 2 3] (map checker/->checker [1 3]))
  => false)

^{:refer hara.test.checker.util/contains-with-gaps :added "3.0"}
(fact "checks if a sequence matches the pattern, with gaps allowed"
  (contains-with-gaps [0 1 2 3] (map checker/->checker [1 2 3]))
  => true

  (contains-with-gaps [0 1 2 3] (map checker/->checker [1 3]))
  => true

  (contains-with-gaps [0 1 2 3] (map checker/->checker [2 0]))
  => false)

^{:refer hara.test.checker.util/perm-check :added "3.0"}
(fact "decide if a given vector of perms are appropriately matched"
  (perm-check [#{0 1 2} #{2} #{0 2}] #{0 1 2})
  => true

  (perm-check [#{2} #{0 1 2} #{2}] #{0 1 2})
  => false

  (perm-check [#{1} #{1 0} #{0 2 1} #{1 0} #{0 2 1}] #{0 1 2})
  => true)

^{:refer hara.test.checker.util/perm-build :added "3.0"}
(fact "builds a perm out of a sequence and checks"
  (perm-build [0 1 2 3] (map checker/->checker [1 3]))
  => [#{} #{0} #{} #{1}]

  (perm-build [0 1 2 3] (map checker/->checker [odd? 3 number?]))
  => [#{2} #{0 2} #{2} #{0 1 2}])

^{:refer hara.test.checker.util/contains-any-order :added "3.0"}
(fact "checks if a sequence matches the pattern, with any order allowed"
  (contains-any-order [0 1 2 3] (map checker/->checker [2 1 3]))
  => true

  (contains-any-order [0 1 2 3] (map checker/->checker [2 0 3]))
  => false)

^{:refer hara.test.checker.util/contains-all :added "3.0"}
(fact "checks if a sequence matches any of the checks"
  (contains-all [0 1 2 3] (map checker/->checker [2 1 3]))
  => true

  (contains-all [0 1 2 3] (map checker/->checker [2 0 3]))
  => true

  (contains-all [0 1 2 3] (map checker/->checker [0 0]))
  => false)