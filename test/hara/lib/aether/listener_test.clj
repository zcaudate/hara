(ns hara.lib.aether.listener-test
  (:use hara.test)
  (:require [hara.lib.aether.listener :refer :all]))

^{:refer hara.lib.aether.listener/event->rep :added "3.0"}
(fact "converts the event to a map representation")

^{:refer hara.lib.aether.listener/record :added "3.0"}
(fact "adds an event to the recorder")

^{:refer hara.lib.aether.listener/aggregate :added "3.0"}
(fact "summarises all events that have been processed")

^{:refer hara.lib.aether.listener/process-event :added "3.0"}
(fact "processes a recorded event")
