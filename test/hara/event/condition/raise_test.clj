(ns hara.event.condition.raise-test
  (:use hara.test)
  (:require [hara.event.condition.raise :refer :all]))

^{:refer hara.event.condition.raise/default-unhandled-fn :added "3.0"}
(fact "raises an unhandled exception")

^{:refer hara.event.condition.raise/raise-catch :added "3.0"}
(fact "raises a catch exception")

^{:refer hara.event.condition.raise/raise-choose :added "3.0"}
(fact "raises a choose exception")

^{:refer hara.event.condition.raise/raise-fail :added "3.0"}
(fact "raises a fail exception")

^{:refer hara.event.condition.raise/raise-loop :added "3.0"}
(fact "makes sure that the issue has been handled by all managers")

(comment
  (hara.code/import))