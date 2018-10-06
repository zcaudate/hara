(ns hara.state.base.atom-test
  (:use hara.test)
  (:require [hara.state.base.atom :refer :all]))

^{:refer hara.state.base.atom/cursor :added "3.0"}
(fact "adds a cursor to the atom to update on any change"

  (def a (atom {:a {:b 1}}))
  
  (def ca (cursor a [:a :b]))

  (do (swap! ca + 10)
      (swap! a update-in [:a :b] + 100)
      [(deref a) (deref ca)])
  => [{:a {:b 111}} 111])

^{:refer hara.state.base.atom/derived :added "3.0"}
(fact "constructs an atom derived from other atoms"

  (def a (atom 1))
  (def b (atom 10))
  (def c (derived [a b] +))

  (do (swap! a + 1)
      (swap! b + 10)
      [@a @b @c])
  => [2 20 22])
