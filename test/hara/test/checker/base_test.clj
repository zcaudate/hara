(ns hara.test.checker.base-test
  (:use [hara.test :exclude [satisfies anything approx throws exactly]])
  (:require [hara.test.checker.base :refer :all]
            [hara.test.common :as common]))

^{:refer hara.test.checker.base/verify :added "3.0"}
(fact "verifies a value with it's associated check"

  (verify (satisfies 2) 1)
  => (contains-in {:type :success
                   :data false
                   :checker {:tag :satisfies
                             :doc string?
                             :expect 2}
                   :actual 1
                   :from :verify})

  (verify (->checker #(/ % 0)) 1)
  => (contains {:type :exception
                :data java.lang.ArithmeticException
                :from :verify}))

^{:refer hara.test.checker.base/succeeded? :added "3.0"}
(fact "determines if the results of a check have succeeded"

  (-> (satisfies Long)
      (verify 1)
      succeeded?)
  => true

  (-> (satisfies even?)
      (verify 1)
      succeeded?)
  => false)

^{:refer hara.test.checker.base/throws :added "3.0"}
(fact "checker that determines if an exception has been thrown"

  ((throws Exception "Hello There")
   (common/map->Result
    {:type :exception
     :data (Exception. "Hello There")}))
  => true)

^{:refer hara.test.checker.base/exactly :added "3.0"}
(fact "checker that allows exact verifications"

  ((exactly 1) 1) => true

  ((exactly Long) 1) => false

  ((exactly number?) 1) => false)

^{:refer hara.test.checker.base/approx :added "3.0"}
(fact "checker that allows approximate verifications"

  ((approx 1) 1.000001) => true

  ((approx 1) 1.1) => false

  ((approx 1 0.0000001) 1.001) => false)

^{:refer hara.test.checker.base/satisfies :added "3.0"}
(fact "checker that allows loose verifications"

  ((satisfies 1) 1) => true

  ((satisfies Long) 1) => true

  ((satisfies number?) 1) => true

  ((satisfies #{1 2 3}) 1) => true

  ((satisfies [1 2 3]) 1) => false

  ((satisfies number?) "e") => false

  ((satisfies #"hello") #"hello") => true)

^{:refer hara.test.checker.base/anything :added "3.0"}
(fact "a checker that returns true for any value"

  (anything nil) => true

  (anything [:hello :world]) => true)

^{:refer hara.test.checker.base/->checker :added "3.0"}
(fact "creates a 'satisfies' checker if not already a checker"

  ((->checker 1) 1) => true

  ((->checker (exactly 1)) 1) => true)

(comment

  (hara.code/import))
