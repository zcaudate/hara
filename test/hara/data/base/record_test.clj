(ns hara.data.base.record-test
  (:use hara.test)
  (:require [hara.data.base.record :as record]))

^{:refer hara.data.base.record/empty :added "3.0"}
(fact "creates an empty record from an existing one"

  (defrecord Database [host port])

  (record/empty (Database. "localhost" 8080))
  => (just {:host nil :port nil}))