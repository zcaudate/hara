(ns hara.object.element.impl.field-test
  (:use hara.test)
  (:require [hara.object.element.impl.field :as field]
            [hara.object.query :as query]))

^{:refer hara.object.element.impl.field/arg-params :added "3.0"}
(fact "arguments for getters and setters of fields"

  (-> (query/query-class String ["hash" :#])
      (field/arg-params :set))
  => [java.lang.String Integer/TYPE]

  (-> (query/query-class String ["hash" :#])
      (field/arg-params :get))
  => [java.lang.String])

^{:refer hara.object.element.impl.field/throw-arg-exception :added "3.0"}
(comment "helper macro for invoke to throw more readable messages")

^{:refer hara.object.element.impl.field/invoke-static-field :added "3.0"}
(fact "invokes the function on the class static field"

  (-> (query/query-class String ["CASE_INSENSITIVE_ORDER" :#])
      (field/invoke-static-field String))
  => java.lang.String$CaseInsensitiveComparator)

^{:refer hara.object.element.impl.field/invoke-instance-field :added "3.0"}
(fact "invokes the function on the field of an instance"

  (-> (query/query-class String ["hash" :#])
      (field/invoke-instance-field "123"))
  => 48690)

(comment
  (hara.code/import))