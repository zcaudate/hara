(ns hara.protocol.object-test
  (:use hara.test)
  (:require [hara.protocol.object :refer :all]))

^{:refer hara.protocol.object/-meta-read :added "3.0"}
(comment "accesses class meta information for reading from object")

^{:refer hara.protocol.object/-meta-write :added "3.0"}
(comment "accesses class meta information for writing to the object")