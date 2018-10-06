(ns hara.core.base.protocol-test
  (:use hara.test)
  (:require [hara.core.base.protocol :refer :all]
            [hara.protocol.state :as state]))


^{:refer hara.core.base.protocol/protocol-interface :added "3.0"}
(fact "returns the java interface for a given protocol"

  (protocol-interface state/IStateGet)
  => hara.protocol.state.IStateGet)

^{:refer hara.core.base.protocol/protocol-methods :added "3.0"}
(fact "returns the methods provided by the protocol"

  (protocol-methods state/IStateSet)
  => '[-clone-state -empty-state -set-state -update-state])

^{:refer hara.core.base.protocol/protocol-signatures :added "3.0"}
(fact "returns the method signatures provided by the protocol"

  (protocol-signatures state/IStateSet)
  => '{-update-state  {:arglists ([obj f args opts]) :doc nil}
       -set-state     {:arglists ([obj v opts]) :doc nil}
       -empty-state   {:arglists ([obj opts])   :doc nil}
       -clone-state   {:arglists ([obj opts])   :doc nil}})

^{:refer hara.core.base.protocol/protocol-impls :added "3.0"}
(fact "returns types that implement the protocol"
  
  (protocol-impls state/IStateSet)
  => (contains [clojure.lang.Agent
                clojure.lang.Ref
                clojure.lang.IAtom
                clojure.lang.Volatile
                clojure.lang.IPending
                clojure.lang.Var]
               :in-any-order :gaps-ok)

  (protocol-impls state/IStateGet)
  => (contains [clojure.lang.IDeref clojure.lang.IPending]
               :in-any-order :gaps-ok))

^{:refer hara.core.base.protocol/protocol? :added "3.0"}
(fact "checks whether an object is a protocol"

  (protocol? state/IStateGet)
  => true)

^{:refer hara.core.base.protocol/implements? :added "3.0"}
(fact "checks whether a type has implemented a protocol"

  (implements? state/IStateGet clojure.lang.Atom)
  => true

  (implements? state/IStateGet
               clojure.lang.Atom
               "-get-state")
  => true)

^{:refer hara.core.base.protocol/protocol-remove :added "3.0"}
(fact "removes a protocol"

  (defprotocol -A-
    (-dostuff [_]))

  (do (extend-protocol -A-
        String
        (-dostuff [_]))
      
       (implements? -A- String "-dostuff"))
  => true

  (do (protocol-remove -A- String)
      (implements? -A- String "-dostuff"))
  => false)
