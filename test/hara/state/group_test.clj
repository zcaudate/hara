(ns hara.state.group-test
  (:use hara.test)
  (:require [hara.state.group :refer :all]))

(defrecord Person [])

(defmethod print-method
  Person
  [v ^java.io.Writer w]
  (.write w (str "#person" (into {} v))))

(defgroup people
  {:tag :people
   :constructor map->Person
   :items [{:name :andy}
           {:name :bob}]})

(defitem people {:name :chris})

^{:refer hara.state.group/group? :added "3.0"}
(fact "checks to see if an element is a group"
  (group? people)
  => true)

^{:refer hara.state.group/list-items :added "3.0"}
(fact "returns a list of keys to items in the group"

  (list-items people)
  => [:andy :bob :chris])

^{:refer hara.state.group/find-item :added "3.0"}
(fact "finds an item based on the given key"
  (find-item people :andy)
  => {:name :andy})

^{:refer hara.state.group/add-item :added "3.0"}
(fact "adds an item to the group"
  (-> (add-item people {:name :chris})
      (list-items))
  => [:andy :bob :chris])

^{:refer hara.state.group/remove-item :added "3.0"}
(fact "removes items based on the key"
  (-> (remove-item people :chris)
      (list-items))
  => [:andy :bob])

^{:refer hara.state.group/append-items :added "3.0"}
(fact "appends a set of data to the group"
  (-> (append-items people [{:name :dave} {:name :erin}])
      (list-items))
  => [:andy :bob :dave :erin]
  ^:hidden
  (-> people
      (remove-item :dave)
      (remove-item :erin)))

^{:refer hara.state.group/install-items :added "3.0"}
(fact "reads a set of data from a resource and loads it into the group"
  (-> (install-items people (java.io.StringReader.
                             "[{:name :dave} {:name :erin}]"))
      (list-items))
  => [:andy :bob :dave :erin]
  ^:hidden
  (-> people
      (remove-item :dave)
      (remove-item :erin)))

^{:refer hara.state.group/group :added "3.0"}
(fact "creates a group from a map"
  (group {:tag :hello})
  => #(-> % :tag (= :hello)))

^{:refer hara.state.group/defgroup :added "3.0"}
(fact "creates a group of items"
  (defgroup people
    {:tag :people
     :constructor map->Person
     :items [{:name :andy}
             {:name :bob}]})
  => (comp group? deref))

^{:refer hara.state.group/defitem :added "3.0"}
(fact "adds an item to the group"
  (-> (defitem people {:name :chris})
      deref
      list-items)
  => [:andy :bob :chris]
  ^:hidden
  (remove-item people :chris))
