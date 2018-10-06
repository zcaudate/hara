(ns hara.object.element
  (:require [clojure.walk :as walk]
            [hara.core.base.inheritance :as inheritance]
            [hara.print :as print]
            [hara.module :as module]
            [hara.function :refer [definvoke]]
            [hara.object.element.common :as common]
            [hara.object.element.class :deps true]
            [hara.object.element.impl.constructor :deps true]
            [hara.object.element.impl.field :deps true]
            [hara.object.element.impl.method :deps true]
            [hara.object.element.impl.multi :deps true]
            [hara.object.element.impl.type :as type])
  (:refer-clojure :exclude [instance?]))

(definvoke to-element
  [:memoize {:arglists '([obj])
             :function common/-to-element}])

(defn element-params
  ([elem]
   (common/-element-params elem)))

(defn class-info
  "Lists class information
 
   (element/class-info String)
   => (contains {:name \"java.lang.String\"
                 :hash anything
                 :modifiers #{:instance :class :public :final}})"
  {:added "3.0"}
  [obj]
  (select-keys (type/seed :class (common/context-class obj))
               [:name :hash :modifiers]))

(defn class-hierarchy
  "Lists the class and interface hierarchy for the class
 
   (element/class-hierarchy String)
   => [java.lang.String
       [java.lang.Object
        #{java.io.Serializable
          java.lang.Comparable
          java.lang.CharSequence}]]"
  {:added "3.0"}
  [obj]
  (let [t (common/context-class obj)]
    (vec (cons t (inheritance/ancestor-tree t)))))

(defn constructor?
  "checks if if an element is a constructor
 
   (-> (.getConstructors String)
       (first)
       (element/to-element)
       (element/constructor?))
   => true"
  {:added "3.0"}
  [elem]
  (-> elem :tag (= :constructor)))

(defn method?
  "checks if an element is a method
 
   (-> (.getMethods String)
       (first)
       (element/to-element)
       (element/method?))
   => true"
  {:added "3.0"}
  [elem]
  (-> elem :tag (= :method)))

(defn field?
  "checks if an element is a field
 
   (-> (.getFields String)
       (first)
       (element/to-element)
       (element/field?))
   => true"
  {:added "3.0"}
  [elem]
  (-> elem :tag (= :field)))

(defn static?
  "checks if an element is a static one
 
   (->> (.getMethods String)
        (map element/to-element)
        (filter element/static?)
        first)
   ;;#elem[valueOf :: (int) -> java.lang.String]
   "
  {:added "3.0"}
  [elem]
  (and (-> elem :modifiers :static boolean)
       (not (constructor? elem))))

(defn instance?
  "checks if an element is non static
 
   (->> (.getMethods String)
        (map element/to-element)
        (filter element/instance?)
        first)
   ;;#elem[equals :: (java.lang.String, java.lang.Object) -> boolean]
   "
  {:added "3.0"}
  [elem]
  (-> elem :modifiers :instance boolean))

(defn public?
  "checks if an element is public
 
   (->> (.getMethods String)
        (map element/to-element)
        (filter element/public?)
        first)
   ;;#elem[equals :: (java.lang.String, java.lang.Object) -> boolean]
   "
  {:added "3.0"}
  [elem]
  (-> elem :modifiers :public boolean))

(defn private?
  "checks if an element is private
 
   (->> (.getDeclaredFields String)
        (map element/to-element)
        (filter element/private?)
        first)
   ;;#elem[value :: (java.lang.String) | byte[]]
   "
  {:added "3.0"}
  [elem]
  (-> elem :modifiers :private boolean))

(defn plain?
  "checks if an element is neither public or private"
  {:added "3.0"}
  [elem]
  (-> elem :modifiers :plain boolean))