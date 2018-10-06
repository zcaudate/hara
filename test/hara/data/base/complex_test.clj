(ns hara.data.base.complex-test
  (:use hara.test)
  (:require [hara.data.base.complex :refer :all]
            [hara.core.base.check :refer [hash-map?]]))

^{:refer hara.data.base.complex/assocs :added "3.0"}
(fact "Similar to `assoc` but conditions of association is specified
  through `sel` (default: `identity`) and well as merging specified
  through `func` (default: `combine`)."
  (assocs {:a #{1}} :a #{2 3 4})
  => {:a #{1 2 3 4}}

  (assocs {:a {:id 1}} :a {:id 1 :val 1} :id merge)
  => {:a {:val 1, :id 1}}

  (assocs {:a #{{:id 1 :val 2}
                {:id 1 :val 3}}} :a {:id 1 :val 4} :id merges)
  => {:a #{{:id 1 :val #{2 3 4}}}})

^{:refer hara.data.base.complex/dissocs :added "3.0"}
(fact "Similar to `dissoc` but allows dissassociation of sets of values from a map."

  (dissocs {:a 1} :a)
  => {}

  (dissocs {:a #{1 2}} [:a #{0 1}])
  => {:a #{2}}

  (dissocs {:a #{1 2}} [:a #{1 2}])
  => {})

^{:refer hara.data.base.complex/gets :added "3.0"}
(fact "Returns the associated values either specified by a key or a key and predicate pair."

  (gets {:a 1} :a) => 1

  (gets {:a #{0 1}} [:a zero?]) => #{0}

  (gets {:a #{{:b 1} {}}} [:a :b]) => #{{:b 1}})

^{:refer hara.data.base.complex/merges :added "3.0"}
(fact "Like `merge` but works across sets and will also
   combine duplicate key/value pairs together into sets of values."

  (merges {:a 1} {:a 2})
  => {:a #{1 2}}

  (merges {:a #{{:id 1 :val 1}}}
          {:a {:id 1 :val 2}}
          :id merges)
  => {:a #{{:id 1 :val #{1 2}}}})

^{:refer hara.data.base.complex/merges-nested :added "3.0"}
(fact "Like `merges` but works on nested maps"

  (merges-nested {:a {:b 1}} {:a {:b 2}})
  => {:a {:b #{1 2}}}

  (merges-nested {:a #{{:foo #{{:bar #{{:baz 1}}}}}}}
                 {:a #{{:foo #{{:bar #{{:baz 2}}}}}}}
                 hash-map?
                 merges-nested)
  => {:a #{{:foo #{{:bar #{{:baz 2}}}
                   {:bar #{{:baz 1}}}}}}})

^{:refer hara.data.base.complex/merges-nested* :added "3.0"}
(fact "Like `merges-nested but can recursively merge nested sets and values"

  (merges-nested* {:a #{{:id 1 :foo
                         #{{:id 2 :bar
                            #{{:id 3 :baz 1}}}}}}}
                  {:a {:id 1 :foo
                       {:id 2 :bar
                        {:id 3 :baz 2}}}}
                  :id)

  => {:a #{{:id 1 :foo
            #{{:id 2 :bar
               #{{:id 3 :baz #{1 2}}}}}}}})

^{:refer hara.data.base.complex/gets-in :added "3.0"}
(fact "Similar in style to `get-in` with operations on sets. returns a set of values."

  (gets-in {:a 1} [:a]) => #{1}

  (gets-in {:a 1} [:b]) => #{}

  (gets-in {:a #{{:b 1} {:b 2}}} [:a :b]) => #{1 2})

^{:refer hara.data.base.complex/assocs-in-filtered :added "3.0"}
(fact "allows a selector to be included for assocs-in"

  (assocs-in-filtered {:a #{{:b {:id 1}} {:b {:id 2}}}}
                      [[:a] [:b [:id 1]] :c] 2)
  => {:a #{{:b {:id 1 :c 2}} {:b {:id 2}}}})

^{:refer hara.data.base.complex/assocs-in :added "3.0"}
(fact "Similar to assoc-in but can move through sets"

  (assocs-in {:a {:b 1}} [:a :b] 2)
  => {:a {:b #{1 2}}}

  (assocs-in {:a #{{:b 1}}} [:a :b] 2)
  => {:a #{{:b #{1 2}}}}

  (assocs-in {:a #{{:b {:id 1}} {:b {:id 2}}}}
             [:a [:b [:id 1]] :c] 2)
  => {:a #{{:b {:id 1 :c 2}} {:b {:id 2}}}})

^{:refer hara.data.base.complex/dissocs-in :added "3.0"}
(fact "Similiar to `dissoc-in` but can move through sets."

  (dissocs-in {:a #{{:b 1 :c 1} {:b 2 :c 2}}}
              [:a :b])
  => {:a #{{:c 1} {:c 2}}}

  (dissocs-in {:a #{{:b #{1 2 3} :c 1}
                    {:b #{1 2 3} :c 2}}}
              [[:a [:c 1]] [:b 1]])
  => {:a #{{:b #{2 3} :c 1} {:b #{1 2 3} :c 2}}})

(comment

  (hara.code/import))