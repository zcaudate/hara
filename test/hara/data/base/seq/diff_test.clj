(ns hara.data.base.seq.diff-test
  (:use hara.test)
  (:require [hara.data.base.seq.diff :refer :all]))

^{:refer hara.data.base.seq.diff/diff :added "3.0"}
(fact "creates a diff of two sequences"

  (diff [1 2 3 4 5]
        [1 2 :a 4 5])
  => [2 [[:- 2 1] [:+ 2 [:a]]]]
  
  (diff [1 2 3 4 5]
        [1 :a 3 2 5])
  => [4 [[:- 1 1]
         [:+ 1 [:a]]
         [:- 3 1]
         [:+ 3 [2]]]])

^{:refer hara.data.base.seq.diff/patch :added "3.0"}
(fact "uses a diff to reconcile two sequences"

  (patch [1 2 3 4 5]
         [4 [[:- 1 1]
             [:+ 1 [:a]]
             [:- 3 1]
             [:+ 3 [2]]]])
  => [1 :a 3 2 5])

