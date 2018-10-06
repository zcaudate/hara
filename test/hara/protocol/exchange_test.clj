(ns hara.protocol.exchange-test
  (:use hara.test)
  (:require [hara.protocol.exchange :refer :all]))

^{:refer hara.protocol.exchange/-create :added "3.0"}
(fact "creates an exchange for use with components")