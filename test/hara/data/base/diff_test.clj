(ns hara.data.base.diff-test
  (:use hara.test)
  (:require [hara.data.base.diff :refer :all]))

^{:refer hara.data.base.diff/diff-changes :added "3.0"}
(fact "Finds changes in nested maps, does not consider new elements"

  (diff-changes {:a 2} {:a 1})
  => {[:a] 2}

  (diff-changes {:a {:b 1 :c 2}} {:a {:b 1 :c 3}})
  => {[:a :c] 2}

  ^:hidden
  (diff-changes {:a 1 :b 2 :c 3} {:a 1 :b 2})
  => {}

  (diff-changes {:a 1 :b 2} {:a 1 :b 2 :c 3})
  => {}

  (diff-changes {:a 1} {:a nil})
  => {[:a] 1}

  (diff-changes {:a nil} {:a 1})
  => {[:a] nil}

  (diff-changes {:a true} {:a false})
  => {[:a] true}

  (diff-changes {:a false} {:a true})
  => {[:a] false})

^{:refer hara.data.base.diff/diff-new :added "3.0"}
(fact "Finds new elements in nested maps, does not consider changes"

  (diff-new {:a 2} {:a 1})
  => {}

  (diff-new {:a {:b 1}} {:a {:c 2}})
  => {[:a :b] 1}

  ^:hidden
  (diff-new {:a {:b 1 :c 2}} {:a {:b 1 :c 3}})
  => {}

  (diff-new {:a 1 :b 2 :c 3} {:a 1 :b 2})
  => {[:c] 3}

  (diff-new {:a 1 :b 2} {:a 1 :b 2 :c 3})
  => {})

^{:refer hara.data.base.diff/diff :added "3.0"}
(fact "Finds the difference between two maps"

  (diff {:a 2} {:a 1})
  => {:+ {} :- {} :> {[:a] 2}}

  (diff {:a {:b 1 :d 3}} {:a {:c 2 :d 4}} true)
  => {:+ {[:a :b] 1}
      :- {[:a :c] 2}
      :> {[:a :d] 3}
      :< {[:a :d] 4}})

^{:refer hara.data.base.diff/merge-or-replace :added "3.0"}
(fact "If both are maps then merge, otherwis replace"

  (merge-or-replace {:a {:b {:c 2}}} {:a {:b {:c 3 :d 4}}})
  => {:a {:b {:c 3 :d 4}}})

^{:refer hara.data.base.diff/changed :added "3.0"}
(fact "Outputs what has changed between the two maps"

  (changed {:a {:b {:c 3 :d 4}}}
           {:a {:b {:c 3}}})
  => {:a {:b {:d 4}}})

^{:refer hara.data.base.diff/patch :added "3.0"}
(fact "Use the diff to convert one map to another in the forward
  direction based upon changes between the two."

  (let [m1  {:a {:b 1 :d 3}}
        m2  {:a {:c 2 :d 4}}
        df  (diff m2 m1)]
    (patch m1 df))
  => {:a {:c 2 :d 4}})

^{:refer hara.data.base.diff/unpatch :added "3.0"}
(fact "Use the diff to convert one map to another in the reverse
  direction based upon changes between the two."

  (let [m1  {:a {:b 1 :d 3}}
        m2  {:a {:c 2 :d 4}}
        df  (diff m2 m1 true)]
    (unpatch m2 df))
  => {:a {:b 1 :d 3}})