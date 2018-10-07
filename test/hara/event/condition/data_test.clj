(ns hara.event.condition.data-test
  (:use hara.test)
  (:require [hara.event.condition.data :refer :all]))

^{:refer hara.event.condition.data/issue :added "3.0"}
(fact "creates a new issue"

  (issue {:a 1} "hello" {} nil)
  => (contains {:id keyword?
                :data {:a 1},
                :msg "hello",
                :options {},
                :optmap {},
                :default nil}))

^{:refer hara.event.condition.data/catch-condition :added "3.0"}
(fact "creates a catch condition"

  (catch-condition :<TARGET> "some value"))

^{:refer hara.event.condition.data/choose-condition :added "3.0"}
(fact "creates a choose-condition"

  (choose-condition :<TARGET> :choice-A [1]))

^{:refer hara.event.condition.data/exception :added "3.0"}
(fact "creates an exception"

  (exception (issue {:a 1} "hello" {} nil)))

(comment
  (hara.code/import))