(ns hara.protocol.mail-test
  (:use hara.test)
  (:require [hara.protocol.mail :refer :all]))

^{:refer hara.protocol.mail/-create-mailer :added "3.0"}
(fact "creates a mailer for use with components")

^{:refer hara.protocol.mail/-create-mailbox :added "3.0"}
(fact "creates a mailbox for use with components")