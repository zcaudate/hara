(ns hara.function.base.native-test
  (:use hara.test)
  (:require [hara.function.base.native :refer :all]
            [hara.function.base.invoke :refer [fn]])
  (:refer-clojure :exclude [fn]))

^{:refer hara.function.base.native/fn-body-args :added "3.0"}
(fact "seperates elements of the function body"

  (fn-body-args 'hello '([x] x))
  => '[hello [x] (x)]

  (fn-body-args '([x] x))
  => '[nil [x] (x)])

^{:refer hara.function.base.native/fn-body-function :added "3.0"}
(fact "creates a body for type `java.util.function.Function`"

  (fn-body-function '([x] x))
  => '(clojure.core/reify java.util.function.Function
        (toString [_] "([x] x)")
        (apply [_ x] x))^:hidden

  (-> [1 2 3 4 5]
      (.stream)
      (.map ^{:type :function}
            (fn [x] (inc x)))
      (.collect (java.util.stream.Collectors/toList)))
  => [2 3 4 5 6])

^{:refer hara.function.base.native/fn-body-predicate :added "3.0"}
(fact "creates a body for type `java.util.function.Predicate`"

  (fn-body-predicate '([x] (odd? x)))
  => '(clojure.core/reify java.util.function.Predicate
        (toString [_] "([x] (odd? x))")
        (test [_ x] (clojure.core/boolean (do (odd? x)))))^:hidden

  (-> [1 2 3 4 5]
      (.stream)
      (.filter ^{:type :predicate}
               (fn [x] (odd? x)))
      (.collect (java.util.stream.Collectors/toList)))
  => [1 3 5])
