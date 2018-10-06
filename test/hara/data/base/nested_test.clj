(ns hara.data.base.nested-test
  (:use hara.test)
  (:require [hara.data.base.nested :refer :all]
            [clojure.string :as string]))

^{:refer hara.data.base.nested/keys-nested :added "3.0"}
(fact "The set of all nested keys in a map"

  (keys-nested {:a {:b 1 :c {:d 1}}})
  => #{:a :b :c :d})

^{:refer hara.data.base.nested/key-paths :added "3.0"}
(fact "The set of all paths in a map, governed by a max level of nesting"

  (key-paths {:a {:b 1} :c {:d 1}})
  => (contains [[:c :d] [:a :b]] :in-any-order)

  (key-paths {:a {:b 1} :c {:d 1}} 1)
  => (contains [[:c] [:a]] :in-any-order))

^{:refer hara.data.base.nested/update-keys-in :added "3.0"}
(fact "updates all keys in a map with given function"

  (update-keys-in {:x {["a" "b"] 1 ["c" "d"] 2}} [:x] string/join)
  => {:x {"ab" 1 "cd" 2}}

  (update-keys-in {:a {:c 1} :b {:d 2}} 2 name)
  => {:b {"d" 2}, :a {"c" 1}})

^{:refer hara.data.base.nested/update-vals-in :added "3.0"}
(fact "updates all values in a map with given function"

  (update-vals-in {:a 1 :b 2} [] inc)
  => {:a 2 :b 3}

  (update-vals-in {:a {:c 1} :b 2} [:a] inc)
  => {:a {:c 2} :b 2}

  (update-vals-in {:a {:c 1} :b {:d 2}} 2 inc)
  => {:a {:c 2} :b {:d 3}}

  (update-vals-in {:a 1 :b 2} 1 inc)
  => {:a 2, :b 3})

^{:refer hara.data.base.nested/merge-nested :added "3.0"}
(fact "Merges nested values from left to right."

  (merge-nested {:a {:b {:c 3}}} {:a {:b 3}})
  => {:a {:b 3}}

  (merge-nested {:a {:b {:c 1 :d 2}}}
                {:a {:b {:c 3}}})
  => {:a {:b {:c 3 :d 2}}})

^{:refer hara.data.base.nested/merge-nil-nested :added "3.0"}
(fact "Merges nested values from left to right, provided the merged value does not exist"

  (merge-nil-nested {:a {:b 2}} {:a {:c 2}})
  => {:a {:b 2 :c 2}}

  (merge-nil-nested {:b {:c :old}} {:b {:c :new}})
  => {:b {:c :old}})

^{:refer hara.data.base.nested/dissoc-nested :added "3.0"}
(fact "Returns `m` without all nested keys in `ks`."

  (dissoc-nested {:a {:b 1 :c {:b 1}}} [:b])
  => {:a {:c {}}})

^{:refer hara.data.base.nested/unique-nested :added "3.0"}
(fact "All nested values in `m1` that are unique to those in `m2`."

  (unique-nested {:a {:b 1}}
                 {:a {:b 1 :c 1}})
  => {}

  (unique-nested {:a {:b 1 :c 1}}
                 {:a {:b 1}})
  => {:a {:c 1}})

^{:refer hara.data.base.nested/clean-nested :added "3.0"}
(fact "Returns a associative with nils and empty hash-maps removed."

  (clean-nested {:a {:b {:c {}}}})
  => {}

  (clean-nested {:a {:b {:c {} :d 1 :e nil}}})
  => {:a {:b {:d 1}}})