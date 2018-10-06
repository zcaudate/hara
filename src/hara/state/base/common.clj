(ns hara.state.base.common
  (:require [hara.protocol.state :as protocol.state]
            [hara.state.base.impl :deps true]
            [hara.core.base.error :as error])
  (:refer-clojure :exclude [get set empty update type]))

(defn create
  "creates a state of a particular type
 
   ((juxt deref type) (state/create clojure.lang.Atom 1))
   => [1 clojure.lang.Atom]"
  {:added "3.0"}
  ([class data]
   (create class data nil))
  ([class data opts]
   (protocol.state/-create-state class data opts)))

(defn container
  "returns the class of container given a label
 
   (state/container :atom)
   => clojure.lang.Atom
 
   (state/container :volatile)
   => clojure.lang.Volatile"
  {:added "3.0"}
  [key]
  (protocol.state/-container-state key))

(defn list-types
  "lists all types of state-like objects
 
   (state/list-types)
   ;; (:ref :promise :agent :cache :var :volatile :atom)
   => seq?"
  {:added "3.0"}
  []
  (keys (.getMethodTable protocol.state/-container-state)))

(defn clone
  "clones the state object
 
   (-> (state/container :volatile)
       (state/create 2)
       (state/clone))
   => volatile?"
  {:added "3.0"}
  ([obj]
   (clone obj nil))
  ([obj opts]
   (or (error/suppress (protocol.state/-clone-state (atom {}) {}))
       (let [data (protocol.state/-get-state obj opts)]
         (create (clojure.core/type obj) data opts)))))

(defn copy
  "copies the value of one state to another
 
   (def -a- (ref nil))
   (state/copy (atom 1)
               -a-)
   @-a- => 1"
  {:added "3.0"}
  ([source sink]
   (copy source sink nil))
  ([source sink opts]
   (let [data (protocol.state/-get-state source opts)]
     (protocol.state/-set-state sink data opts)
     source)))

(defn get
  "Like deref but is extensible through the IStateGet protocol
 
   (state/get (atom 1)) => 1
 
   (state/get (ref 1)) => 1"
  {:added "3.0"}
  ([obj] (get obj nil))
  ([obj opts]
   (protocol.state/-get-state obj opts)))

(defn set
  "Like reset! but is extensible through the IStateSet protocol
 
   (let [a (atom nil)]
     (state/set a 1)
     @a) => 1"
  {:added "3.0"}
  ([obj v] (set obj v nil))
  ([obj v opts]
   (protocol.state/-set-state obj v opts)
   obj))

(defn empty
  "empties the state, extensible through the IStateSet protocol
   (let [a (atom 1)]
     (state/empty a)
     @a) => nil"
  {:added "3.0"}
  ([obj] (empty obj nil))
  ([obj opts]
   (protocol.state/-empty-state obj opts)
   obj))

(defn update
  "Like swap! but is extensible through the IStateSet protocol
 
   (let [a (atom 0)]
     (state/update a + 1)
     @a) => 1
 
   "
  {:added "3.0"}
  ([obj f]
   (protocol.state/-update-state obj f [] nil)
   obj)
  ([obj f & args]
   (protocol.state/-update-state obj f args nil)
   obj))

(defn update-apply
  "Like swap! but is extensible through the IStateSet protocol
 
   (let [a (atom 0)]
     (state/update-apply a + [1 2 3])
     @a) => 6
 
   "
  {:added "3.0"}
  ([obj f args]
   (update-apply obj f args nil)
   obj)
  ([obj f args opts]
   (protocol.state/-update-state obj f args opts)
   obj))
