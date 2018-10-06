(ns hara.core.base.inheritance
  (:require [clojure.set :as set])
  (:refer-clojure :exclude [ancestors]))

(defn ancestor-list
  "Lists the direct ancestors of a class
   (ancestor-list clojure.lang.PersistentHashMap)
   => [clojure.lang.PersistentHashMap
       clojure.lang.APersistentMap
       clojure.lang.AFn
       java.lang.Object]"
  {:added "3.0"}
  ([cls] (ancestor-list cls []))
  ([^java.lang.Class cls output]
   (if (nil? cls)
     output
     (recur (.getSuperclass cls) (conj output cls)))))

(defn all-interfaces
  "Lists all interfaces for a class
 
   (all-interfaces clojure.lang.AFn)
   => #{java.lang.Runnable
        java.util.concurrent.Callable
        clojure.lang.IFn}"
  {:added "3.0"}
  [cls]
  (let [directs (.getInterfaces cls)
        sub (mapcat all-interfaces directs)]
    (set (concat directs sub))))

(defn ancestor-tree
  "Lists the hierarchy of bases and interfaces of a class.
   (ancestor-tree Class)
   => [[java.lang.Object #{java.io.Serializable
                           java.lang.reflect.Type
                           java.lang.reflect.AnnotatedElement
                           java.lang.reflect.GenericDeclaration}]]
   "
  {:added "3.0"}
  ([cls] (ancestor-tree cls []))
  ([^Class cls output]
   (let [base (.getSuperclass cls)]
     (if-not base output
             (recur base
                    (conj output [base (all-interfaces cls)]))))))


(defn all-ancestors
  [cls]
  (let [bases (ancestor-list cls)
        interfaces (mapcat all-interfaces bases)]
    (set (concat bases interfaces))))

(defn inherits?
  "checks if one class inherits from another
 
   (inherits? clojure.lang.ILookup clojure.lang.APersistentMap)
   => true"
  {:added "3.0"}
  [ancestor cls]
  (contains? (all-ancestors cls) ancestor))

(defn best-match
  "finds the best matching interface or class from a list of candidates
 
   (best-match #{Object} Long) => Object
   (best-match #{String} Long) => nil
   (best-match #{Object Number} Long) => Number"
  {:added "3.0"}
  [candidates ^Class cls]
  (or (get candidates cls)
      (->> (apply concat (ancestor-tree cls))
           (map (fn [v]
                  (if (set? v)
                    (first (set/intersection v candidates))
                    (get candidates v))))
           (filter identity)
           first)))
