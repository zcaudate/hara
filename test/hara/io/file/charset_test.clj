(ns hara.io.file.charset-test
  (:use hara.test)
  (:require [hara.io.file.charset :refer :all]))

^{:refer hara.io.file.charset/charset-default :added "3.0"}
(fact "returns the default charset"

  (charset-default)
  => "UTF-8")

^{:refer hara.io.file.charset/charset-list :added "3.0"}
(comment "returns the list of available charset"

  (charset-list)
  => ("Big5" "Big5-HKSCS" ... "x-windows-iso2022jp"))

^{:refer hara.io.file.charset/charset :added "3.0"}
(comment "constructs a charset object from a string"
  (charset "UTF-8")
  => java.nio.charset.Charset)
