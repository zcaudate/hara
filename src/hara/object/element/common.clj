(ns hara.object.element.common
  (:require [hara.function :refer [defexecutive]]))

(defn context-class
  "If x is a class, return x otherwise return the class of x
 
   (context-class String)
   => String
 
   (context-class \"\")
   => String"
  {:added "3.0"}
  [obj]
  (if (class? obj) obj (type obj)))
  
(defn assignable?
  "checks whether a class is assignable to another in sequence
   (assignable? [String] [CharSequence])
   => true
 
   (assignable? [String Integer Long] [CharSequence Number Number])
   => true
 
   (assignable? [CharSequence] [String])
   => false"
  {:added "3.0"}
  [current base]
  (->> (map (fn [^Class x ^Class y]
              (or (= y x)
                  (.isAssignableFrom y x))) current base)
       (every? identity)))

(defmulti -invoke-element
  "base method for extending `invoke` for all element types"
  {:added "3.0"}
  (fn [x & args] (:tag x)))

(defmulti -to-element
  "base method for extending creating an element from java.reflect objects"
  {:added "3.0"}
  type)

(defmulti -element-params
  "base method for extending `:params` entry for all element types"
  {:added "3.0"}
  :tag)

(defmulti -format-element
  "base method for extending `toString` entry for all element types"
  {:added "3.0"}
  :tag)

(defmethod -format-element :default
  [_]
  (str '[uninitialised]))

(defexecutive Element
  "defines an `hara.object.element.Element` instance
 
   (->Element {})
   ;; #elem[uninitialised]
   "
  {:added "3.0"}
  [body]
  {:tag "elem"
   :this element
   :refresh true
   :display -format-element
   :invoke -invoke-element}
  
  clojure.lang.ILookup
  (valAt [elem k]
         (if (or (nil? k)
                 (= k :all))
           body
           (get body k))))

(defn element
  "creates a element from a map
 
   (element {})
   => hara.object.element.common.Element"
  {:added "3.0"}
  [body]
  (Element. body))

(defn element?
  "checker for the element type
 
   (element? (element {}))
   => true"
  {:added "3.0"}
  [x]
  (instance? Element x))
