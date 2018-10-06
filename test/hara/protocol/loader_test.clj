(ns hara.protocol.loader-test
  (:use hara.test)
  (:require [hara.protocol.loader :refer :all]))

^{:refer hara.protocol.loader/-load-class :added "3.0"}
(fact "loads a class from various sources")

^{:refer hara.protocol.loader/-rep :added "3.0"}
(fact "multimethod definition for coercing to a rep")

^{:refer hara.protocol.loader/-artifact :added "3.0"}
(fact "multimethod definition for coercing to an artifact type")
