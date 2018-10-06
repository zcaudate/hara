(ns hara.object.framework.map-like-test
  (:use hara.test)
  (:require [hara.object.framework.map-like :refer :all]
            [hara.object.framework.write :as write]))

^{:refer hara.object.framework.map-like/key-selection :added "3.0"}
(fact "selects map based on keys"

  (key-selection {:a 1 :b 2} [:a] nil)
  => {:a 1}

  (key-selection {:a 1 :b 2} nil [:a])
  => {:b 2})

^{:refer hara.object.framework.map-like/read-proxy-functions :added "3.0"}
(fact "creates a proxy access through a field in the object"

  (read-proxy-functions {:school [:name :raw]})
  => '{:name (clojure.core/fn [obj]
               (clojure.core/let [proxy (hara.object.framework.access/get obj :school)]
                 (hara.object.framework.access/get proxy :name))),
       :raw (clojure.core/fn [obj]
              (clojure.core/let [proxy (hara.object.framework.access/get obj :school)]
                (hara.object.framework.access/get proxy :raw)))})

^{:refer hara.object.framework.map-like/write-proxy-functions :added "3.0"}
(fact "creates a proxy access through a field in the object"

  (write-proxy-functions {:school [:name :raw]})
  => '{:name (clojure.core/fn [obj v]
               (clojure.core/let [proxy (hara.object.framework.access/get obj :school)]
                 (hara.object.framework.access/set proxy :name v))),
       :raw (clojure.core/fn [obj v]
              (clojure.core/let [proxy (hara.object.framework.access/get obj :school)]
                (hara.object.framework.access/set proxy :raw v)))})

^{:refer hara.object.framework.map-like/extend-map-like :added "3.0"}
(fact "creates an entry for map-like classes"

  (extend-map-like test.DogBuilder
                   {:tag "build.dog"
                    :write {:empty (fn [] (test.DogBuilder.))}
                    :read :fields})

  (extend-map-like test.Dog {:tag "dog"
                             :write  {:methods :fields
                                      :from-map (fn [m] (-> m
                                                            (write/from-map test.DogBuilder)
                                                            (.build)))}
                             :exclude [:species]})
  
  (with-out-str
    (prn (write/from-data {:name "hello"} test.Dog)))
  => "#dog{:name \"hello\"}\n"

  (extend-map-like test.Cat {:tag "cat"
                             :write  {:from-map (fn [m] (test.Cat. (:name m)))}
                             :exclude [:species]})
  
  (extend-map-like test.Pet {:tag "pet"
                             :write {:from-map (fn [m] (case (:species m)
                                                         "dog" (write/from-map m test.Dog)
                                                         "cat" (write/from-map m test.Cat)))}})
  
  (with-out-str
    (prn (write/from-data {:name "hello" :species "cat"} test.Pet)))
  => "#cat{:name \"hello\"}\n")

(comment
  (./javac)
  (hara.code/import))
