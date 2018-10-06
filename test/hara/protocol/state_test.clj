(ns hara.protocol.state-test
  (:use hara.test)
  (:require [hara.protocol.state :refer :all]
            [hara.core.base.check :as check]
            [hara.state.base.impl :as impl]))

^{:refer hara.protocol.state/-create-state :added "3.0"}
(fact "creates a state object"

  (-create-state clojure.lang.Atom {} {})
  => check/atom?)

^{:refer hara.protocol.state/-container-state :added "3.0"}
(fact "returns a type for a label"

  (-container-state :atom)
  => clojure.lang.Atom)
