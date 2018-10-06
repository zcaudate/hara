(ns hara.data.base.combine-test
  (:use hara.test)
  (:require [hara.data.base.combine :refer :all]))

^{:refer hara.data.base.combine/combine-select :added "3.0"}
(fact "selects an element out of the set that matches sel when it is applied"

  (combine-select #{1 2 3} 2 identity)
  => 2

  (combine-select #{{:id 1 :val 2} {:id 2 :val 2}} {:id 1 :val 1} :id)
  => {:id 1 :val 2})

^{:refer hara.data.base.combine/combine-value :added "3.0"}
(fact "returns a single set, sel is used for item comparison while func
  is used as the combine function"

  (combine-value #{{:id 1 :a 1} {:id 2 :a 2}}
                 {:id 3 :b 3}
                 :id merge)
  => #{{:id 1, :a 1} {:id 2, :a 2} {:id 3, :b 3}}

  (combine-value #{{:id 1 :a 1} {:id 2 :a 2}}
                 {:id 1 :b 3}
                 :id merge)
  => #{{:id 1 :a 1 :b 3} {:id 2 :a 2}})

^{:refer hara.data.base.combine/combine-set :added "3.0"}
(fact "Returns the combined set of `s1` and `s2` using sel for item
  comparison and func as the combine function"

  (combine-set #{{:id 1 :val 0} {:id 2 :a 0}}
               #{{:id 1 :val 1} {:id 2 :val 2}}
               :id merge)
  => #{{:id 1 :val 1} {:id 2 :val 2 :a 0}})

^{:refer hara.data.base.combine/combine-internal :added "3.0"}
(fact "Combines all elements in a single using sel and func"

  (combine-internal #{{:id 1} {:id 2} {:id 1 :val 1} {:id 2 :val 2}}
                    :id merge)
  => #{{:id 1 :val 1} {:id 2 :val 2}})

^{:refer hara.data.base.combine/combine :added "3.0"}
(fact "takes `v1` and `v2`, which can be either
  values or sets of values and merges them into a new set."

  (combine 1 2) => #{1 2}

  (combine #{1} 1) => #{1}

  (combine #{{:id 1} {:id 2}}
           #{{:id 1 :val 1} {:id 2 :val 2}}
           :id merge)
  => #{{:id 1 :val 1} {:id 2 :val 2}})

^{:refer hara.data.base.combine/decombine :added "3.0"}
(fact "takes set or value `v` and returns a set with
  elements matching sel removed"

  (decombine 1 1) => nil

  (decombine 1 2) => 1

  (decombine #{1} 1) => nil

  (decombine #{1 2 3 4} #{1 2}) => #{3 4}

  (decombine #{1 2 3 4} even?) => #{1 3})