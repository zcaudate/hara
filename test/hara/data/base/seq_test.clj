(ns hara.data.base.seq-test
  (:use hara.test)
  (:require [hara.data.base.seq :refer :all]))

^{:refer hara.data.base.seq/positions :added "3.0"}
(fact "find positions of elements matching the predicate"

  (positions even? [5 5 4 4 3 3 2 2])
  => [2 3 6 7])

^{:refer hara.data.base.seq/remove-index :added "3.0"}
(fact "removes element at the specified index"

  (remove-index [:a :b :c :d] 2)
  => [:a :b :d])

^{:refer hara.data.base.seq/index-of :added "3.0"}
(fact "finds the index of the first matching element in an array"

  (index-of even? [1 2 3 4]) => 1

  (index-of keyword? [1 2 :hello 4]) => 2)

^{:refer hara.data.base.seq/element-of :added "3.0"}
(fact "finds the element within an array"

  (element-of keyword? [1 2 :hello 4])
  => :hello)

^{:refer hara.data.base.seq/flatten-all :added "3.0"}
(fact "flattens all elements the collection"

  (flatten-all [1 2 #{3 {4 5}}])
  => [1 2 3 4 5])
