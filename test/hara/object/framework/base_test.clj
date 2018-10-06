(ns hara.object.framework.base-test
  (:use hara.test)
  (:require [hara.object.framework.read :as read]
            [hara.object.framework.write :as write]
            [hara.protocol.object :as object]
            [hara.object.query :as reflect]))

(defmethod object/-meta-write test.DogBuilder
  [_]
  {:empty (fn [] (test.DogBuilder.))
   :methods (write/write-setters test.DogBuilder)})

(defmethod object/-meta-read test.DogBuilder
  [_]
  {:to-map (read/read-fields test.DogBuilder)})

(defmethod object/-meta-read test.Dog
  [_]
  {:methods (read/read-getters test.Dog)})

(defmethod object/-meta-write test.Dog
  [_]
  {:from-map (fn [m] (-> m
                         (write/from-map test.DogBuilder)
                         (.build)))})

(defmethod object/-meta-read test.Cat
  [_]
  {:methods (read/read-getters test.Cat)})

(defmethod object/-meta-write test.Cat
  [_]
  {:from-map (fn [m] (test.Cat. (:name m)))
   :methods (write/write-fields test.Cat)})

(defmethod object/-meta-write test.Pet
  [_]
  {:from-map (fn [m] (case (:species m)
                       "dog" (write/from-map m test.Dog)
                       "cat" (write/from-map m test.Cat)))})

(defmethod object/-meta-read test.Pet
  [_]
  {:methods (read/read-getters test.Pet)})

(defmethod object/-meta-write test.PersonBuilder
  [_]
  {:empty (fn [] (test.PersonBuilder.))
   :methods (write/write-setters test.PersonBuilder)})

(defmethod object/-meta-read test.PersonBuilder
  [_]
  {:methods (read/read-fields test.PersonBuilder)})

(defmethod object/-meta-write test.Person
  [_]
  {:from-map (fn [m] (-> m
                         (write/from-map test.PersonBuilder)
                         (.build)))})

(defmethod object/-meta-read test.Person
  [_]
  {:methods (read/read-getters test.Person)})


(comment
  (-> {:name "chris" :age 30 :pets [{:name "slurp" :species "dog"}]}
      (write/from-map Person)
      (read/to-data))
  (.hashCode test.Pet)
  (./javac '[test])
  ((:from-map (write/meta-write test.Pet))
   {:name "slurp" :species "dog"})
  
  (-> {:name "chris" :age 30 :pets []}
      (write/from-map Person)
      (read/to-data)))
