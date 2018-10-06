(ns hara.protocol.time-test
  (:use hara.test)
  (:require [hara.protocol.time :refer :all]))

^{:refer hara.protocol.time/-time-meta :added "3.0"}
(comment "accesses the meta properties of a class")

^{:refer hara.protocol.time/-from-long :added "3.0"}
(comment "creates a time representation from a long")

^{:refer hara.protocol.time/-now :added "3.0"}
(comment "creates a representation of the current time")

^{:refer hara.protocol.time/-from-length :added "3.0"}
(comment "creates a representation of a duration")

^{:refer hara.protocol.time/-formatter :added "3.0"}
(comment "create a representation of a formatter")

^{:refer hara.protocol.time/-format :added "3.0"}
(comment "a general function to format what time looks like")

^{:refer hara.protocol.time/-parser :added "3.0"}
(comment "creates a parser for parsing strings")

^{:refer hara.protocol.time/-parse :added "3.0"}
(comment "generic parse function for a string representation")