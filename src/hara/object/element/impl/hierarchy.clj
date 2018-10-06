(ns hara.object.element.impl.hierarchy
  (:require [hara.core.base.inheritance :as inheritance]
            [hara.object.element.common :as common]))

(defn has-method
  "Checks to see if any given method exists in a particular class
 
   (has-method -without-method-
               String)
   => nil"
  {:added "3.0"}
  [^java.lang.reflect.Method method ^Class class]
  (try (.getDeclaredMethod class
                           (.getName method) (.getParameterTypes method))
       class
       (catch NoSuchMethodException e)))

(defn methods-with-same-name-and-count
  "methods-with-same-name-and-count
 
   (methods-with-same-name-and-count -without-method- clojure.lang.IPersistentMap)
   ;; (#<Method clojure.lang.IPersistentMap.without(java.lang.Object)>)
   =>  #(-> % count (= 1))"
  {:added "3.0"}
  [^java.lang.reflect.Method method ^Class class]
  (let [methods (.getDeclaredMethods class)
        iname (.getName method)
        iparams (.getParameterTypes method)
        inargs (count iparams)
        smethods (filter (fn [^java.lang.reflect.Method x]
                           (and (= iname (.getName x))
                                (= inargs (count (.getParameterTypes x)))))
                         methods)]
    smethods))

(defn has-overridden-method
  "Checks to see that the method can be 
 
   (has-overridden-method -without-method- String)
   => nil
 
   (has-overridden-method -without-method- clojure.lang.IPersistentMap)
   => clojure.lang.IPersistentMap"
  {:added "3.0"}
  [^java.lang.reflect.Method method class]
  (let [smethods (methods-with-same-name-and-count method class)
        iparams (.getParameterTypes method)]
    (if (some (fn [^java.lang.reflect.Method smethod]
                (common/assignable? iparams (.getParameterTypes smethod)))
              smethods)
      class)))

(defn origins
  "Lists all the classes tha contain a particular method
 
   (origins -without-method-)
   => [clojure.lang.IPersistentMap
       clojure.lang.PersistentArrayMap]"
  {:added "3.0"}
  ([^java.lang.reflect.Method method]
   (origins method (inheritance/ancestor-tree (.getDeclaringClass method))))
  ([^java.lang.reflect.Method method bases]
   (origins method bases (list (.getDeclaringClass method))))
  ([method [[super interfaces :as pair] & more] currents]
   (if (nil? pair) currents
       (let [currents (if-let [current (has-overridden-method method super)]
                        (conj currents current)
                        currents)]
         (if-let [current (first (filter #(has-overridden-method method %) interfaces))]
           (conj currents current)
           (recur method more currents))))))
