(ns hara.core.base.class-test
  (:use hara.test)
  (:require [hara.core.base.class :refer :all]))

^{:refer hara.core.base.class/array? :added "3.0"}
(fact "checks if a class is an array class"

  (array? (type (int-array 0)))
  => true)

^{:refer hara.core.base.class/primitive-array? :added "3.0"}
(fact "checks if class is a primitive array"

  (primitive-array? (type (int-array 0)))
  => true

  (primitive-array? (type (into-array [1 2 3])))
  => false)

^{:refer hara.core.base.class/array-component :added "3.0"}
(fact "returns the array element within the array"

  (array-component (type (int-array 0)))
  => Integer/TYPE

  (array-component (type (into-array [1 2 3])))
  => java.lang.Long)

^{:refer hara.core.base.class/interface? :added "3.0"}
(fact "returns `true` if `class` is an interface"

  (interface? java.util.Map) => true

  (interface? Class) => false)

^{:refer hara.core.base.class/abstract? :added "3.0"}
(fact "returns `true` if `class` is an abstract class"

  (abstract? java.util.Map) => true

  (abstract? Class) => false)