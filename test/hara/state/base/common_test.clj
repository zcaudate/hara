(ns hara.state.base.common-test
  (:use hara.test)
  (:require [hara.state.base.common :as state]
            [hara.state.base.impl :as impl]))

^{:refer hara.state.base.common/create :added "3.0"}
(fact "creates a state of a particular type"

  ((juxt deref type) (state/create clojure.lang.Atom 1))
  => [1 clojure.lang.Atom])

^{:refer hara.state.base.common/container :added "3.0"}
(fact "returns the class of container given a label"

  (state/container :atom)
  => clojure.lang.Atom

  (state/container :volatile)
  => clojure.lang.Volatile)

^{:refer hara.state.base.common/list-types :added "3.0"}
(fact "lists all types of state-like objects"

  (state/list-types)
  ;; (:ref :promise :agent :cache :var :volatile :atom)
  => seq?)

^{:refer hara.state.base.common/clone :added "3.0"}
(fact "clones the state object"

  (-> (state/container :volatile)
      (state/create 2)
      (state/clone))
  => volatile?)

^{:refer hara.state.base.common/copy :added "3.0"}
(fact "copies the value of one state to another"

  (def -a- (ref nil))
  (state/copy (atom 1)
              -a-)
  @-a- => 1)

^{:refer hara.state.base.common/get :added "3.0"}
(fact "Like deref but is extensible through the IStateGet protocol"

  (state/get (atom 1)) => 1

  (state/get (ref 1)) => 1)

^{:refer hara.state.base.common/set :added "3.0"}
(fact "Like reset! but is extensible through the IStateSet protocol"

  (let [a (atom nil)]
    (state/set a 1)
    @a) => 1)

^{:refer hara.state.base.common/empty :added "3.0"}
(fact "empties the state, extensible through the IStateSet protocol"
  (let [a (atom 1)]
    (state/empty a)
    @a) => nil)

^{:refer hara.state.base.common/update :added "3.0"}
(fact "Like swap! but is extensible through the IStateSet protocol"

  (let [a (atom 0)]
    (state/update a + 1)
    @a) => 1

  ^:hidden
  (let [a (atom 0)]
    (state/update a inc)
    @a) => 1)

^{:refer hara.state.base.common/update-apply :added "3.0"}
(fact "Like swap! but is extensible through the IStateSet protocol"

  (let [a (atom 0)]
    (state/update-apply a + [1 2 3])
    @a) => 6

  ^:hidden
  (let [a (atom 0)]
    (state/update-apply a inc [])
    @a) => 1)
