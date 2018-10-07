(ns hara.test.checker.collection-test
  (:use [hara.test :only [fact]])
  (:require [hara.test.checker.collection :refer :all]
            [hara.test.checker.base :as base]
            [hara.test.common :as common]
            [hara.event :as event]))

^{:refer hara.test.checker.collection/verify-map :added "3.0"}
(fact "takes two maps and determines if they fit"
  (verify-map {:a (base/satisfies odd?)
               :b (base/satisfies even?)}
              {:a 1 :b 2})
  => true)

^{:refer hara.test.checker.collection/verify-seq :added "3.0"}
(fact "takes two seqs and determines if they fit"
  (verify-seq [(base/satisfies 1) (base/satisfies 2)]
              [2 1]
              #{:in-any-order})
  => true

  (verify-seq [(base/satisfies 1) (base/satisfies 2)]
              [2 3 1]
              #{:in-any-order :gaps-ok})
  => true)

^{:refer hara.test.checker.collection/contains-map :added "3.0"}
(fact "map check helper function for `contains`")

^{:refer hara.test.checker.collection/contains-vector :added "3.0"}
(fact "vector check helper function for `contains`")

^{:refer hara.test.checker.collection/contains-set :added "3.0"}
(fact "set check helper function for `contains`"

  ((contains-set #{1 2 3}) [1 2 3 4 5])
  => true

  ((contains-set #{1 2 4}) [1 2 3 4 5])
  => false)

^{:refer hara.test.checker.collection/contains :added "3.0"}
(fact "checker for maps and vectors"

  ((contains {:a odd? :b even?}) {:a 1 :b 4})
  => true

  ((contains {:a 1 :b even?}) {:a 2 :b 4})
  => false

  ((contains [1 2 3]) [1 2 3 4])
  => true

  ((contains [1 3]) [1 2 3 4])
  => false

  ^:hidden
  ((contains [1 3] :gaps-ok) [1 2 3 4])
  => true

  ((contains [3 1] :gaps-ok) [1 2 3 4])
  => false

  ((contains [3 1] :in-any-order) [1 2 3 4])
  => false

  ((contains [3 1 2] :in-any-order) [1 2 3 4])
  => true

  ((contains [3 1] :in-any-order :gaps-ok) [1 2 3 4])
  => true)

^{:refer hara.test.checker.collection/just-map :added "3.0"}
(fact "map check helper function for `just`")

^{:refer hara.test.checker.collection/just-vector :added "3.0"}
(fact "vector check helper function for `just`")

^{:refer hara.test.checker.collection/just-set :added "3.0"}
(fact "set check helper function for `just`"

  ((just-set #{1 2 3}) [1 2 3])
  => true)

^{:refer hara.test.checker.collection/just :added "3.0"}
(fact "combination checker for both maps and vectors"

  ((just {:a odd? :b even?}) {:a 1 :b 4})
  => true

  ((just {:a 1 :b even?}) {:a 1 :b 2 :c 3})
  => false

  ((just [1 2 3 4]) [1 2 3 4])
  => true

  ((just [1 2 3]) [1 2 3 4])
  => false

  ((just [3 2 4 1] :in-any-order) [1 2 3 4])
  => true)

^{:refer hara.test.checker.collection/contains-in :added "3.0"}
(fact "shorthand for checking nested maps and vectors"

  ((contains-in {:a {:b {:c odd?}}}) {:a {:b {:c 1 :d 2}}})
  => true

  ((contains-in [odd? {:a {:b even?}}]) [3 {:a {:b 4 :c 5}}])
  => true)

^{:refer hara.test.checker.collection/just-in :added "3.0"}
(fact "shorthand for exactly checking nested maps and vectors"

  ((just-in {:a {:b {:c odd?}}}) {:a {:b {:c 1 :d 2}}})
  => false

  ((just-in [odd? {:a {:b even?}}]) [3 {:a {:b 4}}])

  ((just-in [odd? {:a {:b even?}}]) [3 {:a {:b 4}}])
  => true)

^{:refer hara.test.checker.collection/throws-info :added "3.0"}
(fact "checker that determines if an `ex-info` has been thrown"

  ((throws-info {:a "hello" :b "there"})
   (common/evaluate '(throw (ex-info "hello" {:a "hello" :b "there"}))))
  => true)

(comment

  (hara.code/import))