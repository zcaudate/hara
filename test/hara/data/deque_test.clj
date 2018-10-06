(ns hara.data.deque-test
  (:use hara.test)
  (:require [hara.data.deque :refer :all])
  (:refer-clojure :exclude [concat]))

^{:refer hara.data.deque/pop-left :added "3.0"}
(fact "pops an element from the left"

  (pop-left [1 2 3 4])
  => [2 3 4])

^{:refer hara.data.deque/peek-left :added "3.0"}
(fact "peeks at the first element on the left"

  (peek-left [1 2 3 4])
  => 1)

^{:refer hara.data.deque/conj-right :added "3.0"}
(fact "appends elements on the right"

  (conj-right [1] 2 3 4)
  => [1 2 3 4])

^{:refer hara.data.deque/conj-left :added "3.0"}
(fact "appends elements on the left"

  (conj-left [4] 3 2 1)
  => [1 2 3 4])

^{:refer hara.data.deque/conj-both :added "3.0"}
(fact "appends elements on either side"

  (conj-both 1 [2] 3)
  => [1 2 3])

^{:refer hara.data.deque/update-left :added "3.0"}
(fact "updates the leftmost element"

  (update-left [1 2 3] dec)
  => [0 2 3])

^{:refer hara.data.deque/update-right :added "3.0"}
(fact "updates the rightmost element"

  (update-right [1 2 3] inc)
  => [1 2 4])

