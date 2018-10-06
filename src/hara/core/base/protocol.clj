(ns hara.core.base.protocol
  (:require [hara.core.base.inheritance :as inheritance]
            [hara.core.base.error :as error]))

(defn protocol-interface
  "returns the java interface for a given protocol
 
   (protocol-interface state/IStateGet)
   => hara.protocol.state.IStateGet"
  {:added "3.0"}
  [protocol]
  (-> protocol :on-interface))

(defn protocol-methods
  "returns the methods provided by the protocol
 
   (protocol-methods state/IStateSet)
   => '[-clone-state -empty-state -set-state -update-state]"
  {:added "3.0"}
  [protocol]
  (->> protocol :sigs vals (map :name) sort vec))

(defn protocol-signatures
  "returns the method signatures provided by the protocol
 
   (protocol-signatures state/IStateSet)
   => '{-update-state  {:arglists ([obj f args opts]) :doc nil}
        -set-state     {:arglists ([obj v opts]) :doc nil}
        -empty-state   {:arglists ([obj opts])   :doc nil}
        -clone-state   {:arglists ([obj opts])   :doc nil}}"
  {:added "3.0"}
  [protocol]
  (->> (:sigs protocol) 
       (reduce (fn [out [_ m]]
                 (assoc out (:name m) (dissoc m :name)))
               {})))

(defn protocol-impls
  "returns types that implement the protocol
   
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
                :in-any-order :gaps-ok)"
  {:added "3.0"}
  [protocol]
  (-> protocol :impls keys set))

(defn protocol?
  "checks whether an object is a protocol
 
   (protocol? state/IStateGet)
   => true"
  {:added "3.0"}
  [obj]
  (boolean (and (instance? clojure.lang.PersistentArrayMap obj)
                (every? #(contains? obj %) [:on :on-interface :var])
                (-> obj :on str Class/forName error/suppress)
                (protocol-interface obj))))

(defn implements?
  "checks whether a type has implemented a protocol
 
   (implements? state/IStateGet clojure.lang.Atom)
   => true
 
   (implements? state/IStateGet
                clojure.lang.Atom
                \"-get-state\")
   => true"
  {:added "3.0"}
  ([{:keys [impls] :as protocol} type]
   (boolean (or (get impls type)
                (inheritance/inherits? (protocol-interface protocol)
                                       type)
                (map first impls)
                (filter #(inheritance/inherits? % type))
                seq)))
  ([protocol type method]
   (let [method (keyword (str method))
         impls (:impls protocol)]
     (boolean (or (get-in impls [type method])
                  (inheritance/inherits? (protocol-interface protocol)
                                         type)
                  (->> impls
                       (filter (fn [[_ methods]] (get methods method)))
                       (map first)
                       (filter #(inheritance/inherits? % type))
                       seq))))))

(defn protocol-remove
  "removes a protocol
 
   (defprotocol -A-
     (-dostuff [_]))
 
   (do (extend-protocol -A-
         String
         (-dostuff [_]))
       
        (implements? -A- String \"-dostuff\"))
   => true
 
   (do (protocol-remove -A- String)
       (implements? -A- String \"-dostuff\"))
   => false"
  {:added "3.0"}
  [protocol atype]
  (-reset-methods (alter-var-root (:var protocol) update-in [:impls] dissoc atype)))
