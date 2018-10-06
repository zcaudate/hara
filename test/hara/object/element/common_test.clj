(ns hara.object.element.common-test
  (:use hara.test)
  (:require [hara.object.element.common :refer :all]))

^{:refer hara.object.element.common/context-class :added "3.0"}
(fact "If x is a class, return x otherwise return the class of x"

  (context-class String)
  => String

  (context-class "")
  => String)

^{:refer hara.object.element.common/assignable? :added "3.0"}
(fact "checks whether a class is assignable to another in sequence"
  (assignable? [String] [CharSequence])
  => true

  (assignable? [String Integer Long] [CharSequence Number Number])
  => true

  (assignable? [CharSequence] [String])
  => false)

^{:refer hara.object.element.common/-invoke-element :added "3.0"}
(comment "base method for extending `invoke` for all element types")

^{:refer hara.object.element.common/-to-element :added "3.0"}
(comment "base method for extending creating an element from java.reflect objects")

^{:refer hara.object.element.common/-element-params :added "3.0"}
(comment "base method for extending `:params` entry for all element types")

^{:refer hara.object.element.common/-format-element :added "3.0"}
(comment "base method for extending `toString` entry for all element types")

^{:refer hara.object.element.common/Element :added "3.0"}
(fact "defines an `hara.object.element.Element` instance"

  (->Element {})
  ;; #elem[uninitialised]
  )

^{:refer hara.object.element.common/element :added "3.0"}
(fact "creates a element from a map"

  (element {})
  => hara.object.element.common.Element)

^{:refer hara.object.element.common/element? :added "3.0"}
(fact "checker for the element type"

  (element? (element {}))
  => true)

(comment
  (hara.code/import)

  (hara.object.query/query-class String []))
