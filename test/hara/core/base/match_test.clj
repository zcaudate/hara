(ns hara.core.base.match-test
  (:use hara.test)
  (:require [hara.core.base.match :refer :all]))

^{:refer hara.core.base.match/actual-pattern :added "3.0"}
(fact "constructs a pattern used for direct comparison"

  (actual-pattern '_)

  (actual-pattern #{1 2 3}))

^{:refer hara.core.base.match/actual-pattern? :added "3.0"}
(fact "checks if input is an actual pattern"

  (actual-pattern? '_) => false

  (-> (actual-pattern '_)
      actual-pattern?)
  => true)

^{:refer hara.core.base.match/eval-pattern :added "3.0"}
(fact "constructs a pattern that is evaluated before comparison"

  (eval-pattern '(keyword "a"))
  
  (eval-pattern 'symbol?))


^{:refer hara.core.base.match/eval-pattern? :added "3.0"}
(fact "checks if input is an eval pattern"

  (-> (eval-pattern 'symbol?)
      eval-pattern?)
  => true)


^{:refer hara.core.base.match/match-inner :added "3.0"}
(fact "matches the inner contents of a array"

  (match-inner [number? {:a {:b #'symbol?}} '& '_]
               [1 {:a {:b 'o}} 5 67 89 100])
  => true)

(comment
  (hara.code/import 'hara.core.base.match))
