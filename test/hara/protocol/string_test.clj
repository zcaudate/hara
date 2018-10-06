(ns hara.protocol.string-test
  (:use hara.test)
  (:require [hara.protocol.string :refer :all]))

^{:refer hara.protocol.string/-from-string :added "3.0"}
(comment "common method for extending string-like objects")

^{:refer hara.protocol.string/-path-separator :added "3.0"}
(fact "common method for finding the path separator for a given data type")
