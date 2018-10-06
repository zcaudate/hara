(ns hara.object.element.util-test
  (:use hara.test)
  (:require [hara.object.element.util :refer :all]
            [hara.object.query :as query]))

^{:refer hara.object.element.util/box-arg :added "3.0"}
(fact "Converts primitives to their correct data types"
  (box-arg Float/TYPE 2)
  => 2.0

  (box-arg Integer/TYPE 2.001)
  => 2

  (type (box-arg Short/TYPE 1.0))
  => java.lang.Short)

^{:refer hara.object.element.util/set-field :added "3.0"}
(fact "base function to set the field value of a particular object")

^{:refer hara.object.element.util/param-arg-match :added "3.0"}
(fact "Checks if the second argument can be used as the first argument"
  (param-arg-match Double/TYPE Float/TYPE)
  => true

  (param-arg-match Float/TYPE Double/TYPE)
  => true

  (param-arg-match Integer/TYPE Float/TYPE)
  => false

  (param-arg-match Byte/TYPE Long/TYPE)
  => false

  (param-arg-match Long/TYPE Byte/TYPE)
  => true

  (param-arg-match Long/TYPE Long)
  => true

  (param-arg-match Long Byte)
  => false

  (param-arg-match clojure.lang.PersistentHashMap java.util.Map)
  => false

  (param-arg-match java.util.Map clojure.lang.PersistentHashMap)
  => true)

^{:refer hara.object.element.util/param-float-match :added "3.0"}
(fact "matches floats to integer inputs"

  (param-float-match Float/TYPE Long/TYPE)
  => true

  (param-float-match Float/TYPE Long)
  => true

  (param-float-match Float Integer)
  => true

  (param-float-match Float Integer/TYPE)
  => true)

^{:refer hara.object.element.util/is-congruent :added "3.0"}
(fact "makes sure the argument types match with the param types"

  (is-congruent [Integer/TYPE String]
                [Long String])
  => true)

^{:refer hara.object.element.util/throw-arg-exception :added "3.0"}
(fact "helper macro for box-args to throw readable messages")

^{:refer hara.object.element.util/box-args :added "3.0"}
(fact "makes the parameters of the arguments conform to the function signature"

  (-> (query/query-class String ["charAt" :#])
      (box-args ["0123" 1]))
  => ["0123" 1])

^{:refer hara.object.element.util/format-element-method :added "3.0"}
(fact "readable string representation of an element"

  (-> (query/query-class String ["charAt" :#])
      (format-element-method))
  => "[charAt :: (java.lang.String, int) -> char]")

^{:refer hara.object.element.util/element-params-method :added "3.0"}
(fact "arglist parameters for an element"

  (-> (query/query-class String ["charAt" :#])
      (element-params-method))
  => '[java.lang.String int])

^{:refer hara.object.element.util/checks!primitives :added "3.0"}
(fact "param-arg-match basics"
  (.isPrimitive Integer/TYPE)
  => true

  (.isAssignableFrom Integer/TYPE Long/TYPE)
  => false

  (.isAssignableFrom Long/TYPE Integer/TYPE)
  => false

  (.isAssignableFrom java.util.Map clojure.lang.PersistentHashMap)
  => true

  (.isAssignableFrom clojure.lang.PersistentHashMap java.util.Map)
  => false)
