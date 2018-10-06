(ns hara.state.base.impl
  (:require [hara.protocol.state :as protocol.state]
            [hara.core.base.check :as check])
  (:import (clojure.lang Agent
                         Atom
                         IAtom
                         IDeref
                         IPending
                         Ref
                         Var
                         Volatile)))

;;  ------------------
;;       GETTERS
;;  ------------------

(extend-type IDeref
  protocol.state/IStateGet
  (protocol.state/-get-state [obj _]
    (.deref obj)))

;;  ------------------
;;       SETTERS 
;;  ------------------

(extend-type clojure.lang.Agent
  protocol.state/IStateSet
  (-empty-state [obj _]
    (protocol.state/-set-state obj nil nil))

  (-set-state [obj _ v]
    (send obj (fn [_] v)))

  (-update-state [obj f args _]
    (apply send obj f args)))

(defmethod protocol.state/-create-state Agent
  [_ data _]
  (agent data))

(defmethod protocol.state/-container-state :agent
  [_]
  Agent)

(extend-type clojure.lang.IAtom
  protocol.state/IStateSet
  (-empty-state [obj _]
    (protocol.state/-set-state obj nil nil))

  (-set-state [obj v _]
    (reset! obj v))

  (-update-state [obj f args _]
    (apply swap! obj f args)))

(defmethod protocol.state/-create-state Atom
  [_ data _]
  (atom data))

(defmethod protocol.state/-container-state :atom
  [_]
  Atom)

(extend-type IPending
  protocol.state/IStateGet
  (-get-state [obj _]
    (if (.isRealized obj)
      (deref obj)))
  
  protocol.state/IStateSet
  (-set-state [obj v _]
    (cond (.isRealized obj)
          (throw (ex-info "Already realised." {:input obj}))

          (check/promise? obj)
          (deliver obj v)

          :else
          (throw (ex-info "Cannot set state." {:input obj}))))

  (-update-state [obj f args _]
    (cond (.isRealized obj)
          (throw (ex-info "Already realised." {:input obj}))

          (check/promise? obj)
          (deliver obj (apply f nil args))

          :else
          (throw (ex-info "Cannot set state." {:input obj})))))

(defmethod protocol.state/-create-state IPending
  [_ data _]
  (promise data))

(defmethod protocol.state/-container-state :promise
  [_]
  IPending)

(extend-type clojure.lang.Ref
  protocol.state/IStateSet
  (-empty-state [obj _]
    (protocol.state/-set-state obj nil nil))

  (-set-state [obj v _]
    (dosync (ref-set obj v)))

  (-update-state [obj f args _]
    (dosync (apply alter obj f args))))

(defmethod protocol.state/-create-state Ref
  [_ data _]
  (ref data))

(defmethod protocol.state/-container-state :ref
  [_]
  Ref)

(extend-type clojure.lang.Var
  protocol.state/IStateSet
  (-empty-state [obj _]
    (protocol.state/-set-state obj nil nil))

  (-set-state [obj v _]
    (alter-var-root obj (constantly v)))

  (-update-state [obj f args _]
    (apply alter-var-root obj f args)))

(defmethod protocol.state/-create-state Var
  [_ data {:keys [ns name]
           :or {ns *ns*
                name (gensym)}}]
  (Var/intern ns name data))

(defmethod protocol.state/-container-state :var
  [_]
  Var)

(extend-type Volatile
  protocol.state/IStateSet
  (-empty-state [obj _]
    (protocol.state/-set-state obj nil nil))

  (-set-state [obj v _]
    (vreset! obj v))

  (-update-state [obj f args _]
    (.reset obj (apply f (.deref obj) args))))

(defmethod protocol.state/-create-state Volatile
  [_ data _]
  (volatile! data))

(defmethod protocol.state/-container-state :volatile
  [_]
  Volatile)
