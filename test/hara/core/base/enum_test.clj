(ns hara.core.base.enum-test
  (:use hara.test)
  (:require [hara.core.base.enum :refer :all])
  (:import java.lang.annotation.ElementType))

^{:refer hara.core.base.enum/enum? :added "3.0"}
(fact "check to see if class is an enum type"

  (enum? java.lang.annotation.ElementType) => true

  (enum? String) => false)

^{:refer hara.core.base.enum/enum-values :added "3.0"}
(fact "returns all values of an enum type"

  (->> (enum-values ElementType)
       (map str))
  => (contains ["TYPE" "FIELD" "METHOD" "PARAMETER" "CONSTRUCTOR"]
               :in-any-order :gaps-ok))

^{:refer hara.core.base.enum/create-enum :added "3.0"}
(fact "creates an enum value from a string"

  (create-enum "TYPE" ElementType)
  => ElementType/TYPE)

^{:refer hara.core.base.enum/enum-map :added "3.0"}
(fact "cached map of enum values"

  (enum-map ElementType)^:hidden
  => (satisfies [:annotation-type
                 :constructor
                 :field
                 :local-variable
                 :method
                 :module
                 :package
                 :parameter
                 :type
                 :type-parameter
                 :type-use]
                (comp vec sort keys)))

^{:refer hara.core.base.enum/to-enum :added "3.0"}
(fact "gets an enum value given a symbol"

  (to-enum "TYPE" ElementType)
  => ElementType/TYPE

  (to-enum :field ElementType)
  => ElementType/FIELD)
