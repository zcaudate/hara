(ns hara.object.element.modifier-test
  (:use hara.test)
  (:require [hara.object.element.modifier :refer :all]))

^{:refer hara.object.element.modifier/int-to-modifiers :added "3.0"}
(fact "converts the modifier integer into human readable represenation"

  (int-to-modifiers 2r001100)
  => #{:protected :static}

  (int-to-modifiers 128 :field)
  => #{:transient}

  (int-to-modifiers 128 :method)
  => #{:varargs})

^{:refer hara.object.element.modifier/modifiers-to-int :added "3.0"}
(fact "converts the human readable represenation of modifiers into an int"

  (modifiers-to-int #{:protected :static})
  => 12

  (modifiers-to-int #{:transient :field})
  => 128)