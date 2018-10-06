(ns hara.object.framework.read-test
  (:use hara.test)
  (:require [hara.object.framework.read :as read]
            [hara.object.framework.write :as write]
            [hara.protocol.object :as object]
            [hara.object.query :as reflect]
            [hara.object.framework.base-test])
  (:import [test PersonBuilder Person Dog DogBuilder Cat Pet]))

^{:refer hara.object.framework.read/meta-read :added "3.0"}
(fact "access read-attributes with caching"

  (read/meta-read Pet)
  => (contains-in {:class test.Pet
                   :methods {:name fn?
                             :species fn?}}))

^{:refer hara.object.framework.read/read-fields :added "3.0"}
(fact "fields of an object from reflection"
  (-> (read/read-fields Dog)
      keys)
  => [:name :species])

^{:refer hara.object.framework.read/read-all-fields :added "3.0"}
(fact "all fields of an object from reflection"

  (-> (read/read-all-fields {})
      keys)
  => [:-hash :-hasheq :-meta :array])

^{:refer hara.object.framework.read/create-read-method :added "3.0"}
(fact "creates a method based on a template"
  (read/create-read-method (reflect/query-class Dog ["getName" :#])
                           "get"
                           read/+read-get-opts+
                           nil)
  => (contains-in [:name {:prefix "get", :template fn?}]))

^{:refer hara.object.framework.read/read-getters :added "3.0"}
(fact "returns fields of an object through getter methods"
  (-> (read/read-getters Dog)
      keys)
  => [:name :species])

^{:refer hara.object.framework.read/read-all-getters :added "3.0"}
(fact "returns fields of an object and base classes"
  (-> (read/read-all-getters Dog)
      keys)
  => [:class :name :species])

^{:refer hara.object.framework.read/to-data :added "3.0"}
(fact "creates the object from a string or map"

  (read/to-data "hello")
  => "hello"

  (read/to-data (write/from-map {:name "hello" :species "dog"} Pet))
  => (contains {:name "hello"}))

^{:refer hara.object.framework.read/to-map :added "3.0"}
(fact "creates a map from an object"

  (read/to-map (Cat. "spike"))
  => (contains {:name "spike"}))

(comment
  (hara.code/import))
