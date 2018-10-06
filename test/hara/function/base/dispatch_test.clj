(ns hara.function.base.dispatch-test
  (:use hara.test)
  (:require [hara.function.base.dispatch :refer :all]))

^{:refer hara.function.base.dispatch/invoke :added "3.0"}
(fact "Executes `(f v1 ... vn)` if `f` is not nil"

  (invoke nil 1 2 3) => nil

  (invoke + 1 2 3) => 6)

^{:refer hara.function.base.dispatch/call :added "3.0"}
(fact "like `invoke` but reverses the function and first argument"

  (call 2) => 2
  
  (call 2 + 1 2 3) => 8)
  
^{:refer hara.function.base.dispatch/message :added "3.0"}
(fact "Message dispatch for object orientated type calling convention."

  (def obj {:a 10
            :b 20
            :get-sum (fn [this]
                       (+ (:b this) (:a this)))})

  (message obj :get-sum) => 30)

^{:refer hara.function.base.dispatch/op :added "3.0"}
(fact "loose version of apply. Will adjust the arguments to put into a function"

  (op + 1 2 3 4 5 6) => 21

  (op (fn [x] x) 1 2 3) => 1

  (op (fn [_ y] y) 1 2 3) => 2

  (op (fn [_] nil)) => (throws Exception))
