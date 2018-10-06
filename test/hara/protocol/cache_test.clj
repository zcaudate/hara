(ns hara.protocol.cache-test
  (:use hara.test)
  (:require [hara.protocol.cache :refer :all]))

^{:refer hara.protocol.cache/-create :added "3.0"}
(fact "creates a cache for use with components")