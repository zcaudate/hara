(ns hara.protocol.state)

(defmulti -create-state
  "creates a state object
 
   (-create-state clojure.lang.Atom {} {})
   => check/atom?"
  {:added "3.0"}
  (fn [type data opts] type))

(defmulti -container-state
  "returns a type for a label
 
   (-container-state :atom)
   => clojure.lang.Atom"
  {:added "3.0"}
  identity)

(defprotocol IStateGet
  (-get-state [obj opts]))

(defprotocol IStateSet
  (-update-state [obj f args opts])
  (-set-state   [obj v opts])
  (-empty-state [obj opts])
  (-clone-state [obj opts]))
