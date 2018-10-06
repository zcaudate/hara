(ns hara.core.base.inheritance-test
  (:use hara.test)
  (:require [hara.core.base.inheritance :refer :all]))

^{:refer hara.core.base.inheritance/ancestor-list :added "3.0"}
(fact "Lists the direct ancestors of a class"
  (ancestor-list clojure.lang.PersistentHashMap)
  => [clojure.lang.PersistentHashMap
      clojure.lang.APersistentMap
      clojure.lang.AFn
      java.lang.Object])

^{:refer hara.core.base.inheritance/all-interfaces :added "3.0"}
(fact "Lists all interfaces for a class"

  (all-interfaces clojure.lang.AFn)
  => #{java.lang.Runnable
       java.util.concurrent.Callable
       clojure.lang.IFn})

^{:refer hara.core.base.inheritance/ancestor-tree :added "3.0"}
(fact "Lists the hierarchy of bases and interfaces of a class."
  (ancestor-tree Class)
  => [[java.lang.Object #{java.io.Serializable
                          java.lang.reflect.Type
                          java.lang.reflect.AnnotatedElement
                          java.lang.reflect.GenericDeclaration}]]
  ^:hidden
  (ancestor-tree clojure.lang.PersistentHashMap)
  => [[clojure.lang.APersistentMap #{clojure.lang.IMapIterable
                                     clojure.lang.IMeta
                                     clojure.lang.IKVReduce
                                     clojure.lang.IObj
                                     clojure.lang.IEditableCollection}]
      [clojure.lang.AFn #{java.lang.Iterable
                          clojure.lang.ILookup
                          clojure.lang.MapEquivalence
                          java.io.Serializable
                          clojure.lang.IPersistentMap
                          clojure.lang.IPersistentCollection
                          java.util.Map
                          clojure.lang.Counted
                          clojure.lang.Associative
                          clojure.lang.IHashEq
                          clojure.lang.Seqable}]
      [java.lang.Object #{java.lang.Runnable
                          java.util.concurrent.Callable
                          clojure.lang.IFn}]])

^{:refer hara.core.base.inheritance/all-ancestors :added "3.0"}
(fact "returns all ancestors for a given type, itself included"

  (all-ancestors String)
  => #{java.lang.CharSequence
       java.io.Serializable
       java.lang.Object
       java.lang.String
       java.lang.Comparable})

^{:refer hara.core.base.inheritance/inherits? :added "3.0"}
(fact "checks if one class inherits from another"

  (inherits? clojure.lang.ILookup clojure.lang.APersistentMap)
  => true)

^{:refer hara.core.base.inheritance/best-match :added "3.0"}
(fact "finds the best matching interface or class from a list of candidates"

  (best-match #{Object} Long) => Object
  (best-match #{String} Long) => nil
  (best-match #{Object Number} Long) => Number)
