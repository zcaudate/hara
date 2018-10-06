(ns hara.protocol.graph-test
  (:use hara.test)
  (:require [hara.protocol.graph :refer :all]))

^{:refer hara.protocol.graph/-create :added "3.0"}
(fact "creates a graph database for use with components")