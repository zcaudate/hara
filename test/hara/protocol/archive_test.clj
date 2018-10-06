(ns hara.protocol.archive-test
  (:use hara.test)
  (:require [hara.protocol.archive :as archive]))

^{:refer hara.protocol.archive/-open :added "3.0"}
(comment "allows the opening of zip and jar files")