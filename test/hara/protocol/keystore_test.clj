(ns hara.protocol.keystore-test
  (:use hara.test)
  (:require [hara.protocol.keystore :refer :all]))

^{:refer hara.protocol.keystore/-create :added "3.0"}
(fact "creates a keystore for use with components")