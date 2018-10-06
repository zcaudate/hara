(ns hara.function.base.macro-test
  (:use hara.test)
  (:require [hara.function.base.macro :refer :all]))

^{:refer hara.function.base.macro/create-args :added "3.0"}
(fact "caches the result of a function"

  (create-args '[[x] (inc x) nil nil])
  => '("" {} [x] (inc x))^:hidden


  (create-args '["doc" {:a 1} [x] (inc x)])
  => '("doc" {:a 1} [x] (inc x))
  
  (create-args '["doc" [x] (inc x) nil])
  => '("doc" {} [x] (inc x))
  
  (create-args '[{:a 1} [x] (inc x) nil])
  => '("" {:a 1} [x] (inc x)))

^{:refer hara.function.base.macro/create-def-form :added "3.0"}
(fact "removes a cached result"
  
  (create-def-form 'hello "doc" {:added "1.3"} '[x] '(inc x))
  '(do (def hello (inc x))
       (clojure.core/doto (var hello)
         (clojure.core/alter-meta! clojure.core/merge
                                   {:added "1.3"}
                                   {:arglists (quote ([x])), :doc "doc"}))))

^{:refer hara.function.base.macro/defcompose :added "3.0"}
(fact "used instead of `def` for functional composition"

  (defcompose -add-10-
    [x & more]
    (partial + 10))

  (-add-10- 10) => 20)

^{:refer hara.function.base.macro/lookup :added "3.0"}
(fact "creates a lookup function based on a map lookup"

  (def -opts-
    {:in  (fn [s] (-> s (.toLowerCase) keyword))
     :out name
     :not-found :no-reference})
  
  (def -lookup-
    (lookup {:kunming :china
             :melbourne :australia}
            -opts-))
  
  (-lookup- "MeLBoURne") => "australia")

^{:refer hara.function.base.macro/deflookup :added "3.0"}
(fact "defines a map based lookup"

  (deflookup -country-
    [city]
    {:kunming :china
     :melbourne :australia})

  (-country- :kunming) => :china^:hidden
  
  (deflookup -country-
    [city]
    {:kunming :china
     :melbourne :australia}
    -opts-)
  
  (-country- "LA") => "no-reference")
