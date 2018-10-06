(ns hara.object.framework.access-test
  (:use hara.test)
  (:require [hara.object.framework.access :refer :all :as access]
            [hara.object.framework.write :as write]
            [hara.object.framework.base-test])
  (:refer-clojure :exclude [get set get-in keys]))

(hara.object.framework/map-like
 test.Cat
 {:tag "cat"
  :read :all
  :write :all})

^{:refer hara.object.framework.access/get-with-keyword :added "3.0"}
(fact "access the fields of an object with keyword"

  (get-with-keyword {:a 1} :a)
  => 1

  (get-with-keyword (test.Cat. "spike")
                    :name)
  => "spike")

^{:refer hara.object.framework.access/get-with-array :added "3.0"}
(fact "access the fields of an object with an array of keywords"

  (get-with-array {:a 1} [:a])
  => {:a 1}

  (get-with-array (test.Cat. "spike")
                  [:name])
  => {:name "spike"})

^{:refer hara.object.framework.access/get :added "3.0"}
(fact "accessor with either keyword or array lookup"

  (access/get (test.Cat. "spike") :name)
  => "spike")

^{:refer hara.object.framework.access/get-in :added "3.0"}
(fact "accesses the nested object using specifiedb path"

  (access/get-in (test.Cat. "spike") [:name]))

^{:refer hara.object.framework.access/keys :added "3.0"}
(fact "gets all keys of an object"

  (access/keys (test.Cat. "spike"))
  => (contains [:name]))

^{:refer hara.object.framework.access/set-with-keyword :added "3.0"}
(fact "sets the fields of an object with keyword"

  (-> (doto (test.Cat. "spike")
        (set-with-keyword :name "fluffy"))
      (access/get :name))
  => "fluffy")

^{:refer hara.object.framework.access/set :added "3.0"}
(fact "sets the fields of an object with a map"

  (-> (doto (test.Cat. "spike")
        (access/set {:name "fluffy"}))
      (access/get :name))
  => "fluffy")

(comment
  (.hashCode (type (test.Cat. "spike")))
  
  (-> (test.Cat. "spike") type write/meta-write :methods :name)
  (hara.code/import)
  
  (write/meta-write (type )))
