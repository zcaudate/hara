(ns hara.object.element.impl.hierarchy-test
  (:use hara.test)
  (:require [hara.object.element.impl.hierarchy :refer :all]))

(def -without-method-
  (-> clojure.lang.PersistentArrayMap
      (.getDeclaredMethod "without"
                          (into-array [Object]))))

^{:refer hara.object.element.impl.hierarchy/has-method :added "3.0"}
(fact "Checks to see if any given method exists in a particular class"

  (has-method -without-method-
              String)
  => nil^:hidden

  (has-method -without-method-
              clojure.lang.PersistentArrayMap)
  => clojure.lang.PersistentArrayMap)

^{:refer hara.object.element.impl.hierarchy/methods-with-same-name-and-count :added "3.0"}
(fact "methods-with-same-name-and-count"

  (methods-with-same-name-and-count -without-method- clojure.lang.IPersistentMap)
  ;; (#<Method clojure.lang.IPersistentMap.without(java.lang.Object)>)
  =>  #(-> % count (= 1))^:hidden

  (methods-with-same-name-and-count
   (.getDeclaredMethod String "charAt"
                       (into-array Class [Integer/TYPE]))
   CharSequence)
  =>
  #(-> % count (= 1))  ;; (#<Method java.lang.CharSequence.charAt(int)>)
)

^{:refer hara.object.element.impl.hierarchy/has-overridden-method :added "3.0"}
(fact "Checks to see that the method can be "

  (has-overridden-method -without-method- String)
  => nil

  (has-overridden-method -without-method- clojure.lang.IPersistentMap)
  => clojure.lang.IPersistentMap)

^{:refer hara.object.element.impl.hierarchy/origins :added "3.0"}
(fact "Lists all the classes tha contain a particular method"

  (origins -without-method-)
  => [clojure.lang.IPersistentMap
      clojure.lang.PersistentArrayMap])
