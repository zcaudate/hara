(ns hara.core.base.primitive-test
  (:use hara.test)
  (:require [hara.core.base.primitive :refer :all]))

^{:refer hara.core.base.primitive/create-lookup :added "3.0"}
(fact "creates a path lookup given a record"

  (create-lookup {:byte {:name "byte" :size 1}
                  :long {:name "long" :size 4}})
  => {"byte" [:byte :name]
      1 [:byte :size]
      "long" [:long :name]
      4 [:long :size]})

^{:refer hara.core.base.primitive/primitive-type :added "3.0"}
(fact "Converts primitive values across their different representations. The choices are:
   :raw       - The string in the jdk (i.e. `Z` for Boolean, `C` for Character)
   :symbol    - The symbol that hara.object.query uses for matching (i.e. boolean, char, int)
   :string    - The string that hara.object.query uses for matching
   :class     - The primitive class representation of the primitive
   :container - The containing class representation for the primitive type"

  (primitive-type Boolean/TYPE :symbol)
  => 'boolean

  (primitive-type "Z" :symbol)
  => 'boolean

  (primitive-type "int" :symbol)
  => 'int

  (primitive-type Character :string)
  => "char"

  (primitive-type "V" :class)
  => Void/TYPE

  (primitive-type 'long :container)
  => Long

  (primitive-type 'long :type)
  => :long)

(comment
  (./code:import))

