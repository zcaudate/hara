(ns hara.object.framework.write-test
  (:use hara.test)
  (:require [hara.object.framework.read :as read]
            [hara.object.framework.write :as write]
            [hara.protocol.object :as object]
            [hara.object.query :as reflect]
            [hara.object.framework.base-test])
  (:import [test PersonBuilder Person Dog DogBuilder Cat Pet]))

^{:refer hara.object.framework.write/meta-write :added "3.0"}
(fact "access read-attributes with caching"

  (write/meta-write DogBuilder)
  => (contains {:class test.DogBuilder
                :empty fn?,
                :methods (contains
                          {:name
                           (contains {:type java.lang.String, :fn fn?})})}))

^{:refer hara.object.framework.write/write-fields :added "3.0"}
(fact "write fields of an object from reflection"
  
  (-> (write/write-fields Dog)
      keys)
  => [:name :species])

^{:refer hara.object.framework.write/write-all-fields :added "3.0"}
(fact "all write fields of an object from reflection"
  
  (-> (write/write-all-fields {})
      keys)
  => [:-hash :-hasheq :-meta :array])

^{:refer hara.object.framework.write/create-write-method :added "3.0"}
(fact "create a write method from the template"

  (-> ((-> (write/create-write-method (reflect/query-class Cat ["setName" :#])
                                      "set"
                                      write/+write-template+)
           second
           :fn) (test.Cat. "spike") "fluffy")
      (.getName))
  => "fluffy")

^{:refer hara.object.framework.write/write-setters :added "3.0"}
(fact "write fields of an object through setter methods"
  
  (write/write-setters Dog)
  => {}

  (keys (write/write-setters DogBuilder))
  => [:name])

^{:refer hara.object.framework.write/write-all-setters :added "3.0"}
(fact "write all setters of an object and base classes"
  
  (write/write-all-setters Dog)
  => {}

  (keys (write/write-all-setters DogBuilder))
  => [:name])

^{:refer hara.object.framework.write/from-empty :added "3.0"}
(fact "creates the object from an empty object constructor"
  
  (write/from-empty {:name "chris" :pet "dog"}
                    (fn [] (java.util.Hashtable.))
                    {:name {:type String
                            :fn (fn [obj v]
                                  (.put obj "hello" (keyword v))
                                  obj)}
                     :pet  {:type String
                            :fn (fn [obj v]
                                  (.put obj "pet" (keyword v))
                                  obj)}})
  => {"pet" :dog, "hello" :chris})

^{:refer hara.object.framework.write/from-constructor :added "3.0"}
(fact "creates the object from a constructor"
  
  (-> {:name "spike"}
      (write/from-constructor {:fn (fn [name] (Cat. name))
                               :params [:name]}
                              {}))
  ;;=> #test.Cat{:name "spike", :species "cat"}
)

^{:refer hara.object.framework.write/from-map :added "3.0"}
(fact "creates the object from a map"
  
  (-> {:name "chris" :age 30 :pets [{:name "slurp" :species "dog"}
                                    {:name "happy" :species "cat"}]}
      (write/from-map test.Person)
      (read/to-data))
  => (contains-in
      {:name "chris",
       :age 30,
       :pets [{:name "slurp"}
              {:name "happy"}]}))

^{:refer hara.object.framework.write/from-data :added "3.0"}
(fact "creates the object from data"
  
  (-> (write/from-data ["hello"] (Class/forName "[Ljava.lang.String;"))
      seq)
  => ["hello"])

(comment
  (./import))
