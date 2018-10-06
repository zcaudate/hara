(ns hara.function.base.arg-test
  (:use hara.test)
  (:require [hara.function.base.arg :refer :all]))

^{:refer hara.function.base.arg/vargs? :added "3.0"}
(fact "checks that function contain variable arguments"

  (vargs? (fn [x])) => false

  (vargs? (fn [x & xs])) => true)

^{:refer hara.function.base.arg/varg-count :added "3.0"}
(fact "counts the number of arguments types before variable arguments"

  (varg-count (fn [x y & xs])) => 2

  (varg-count (fn [x])) => nil)

^{:refer hara.function.base.arg/arg-count :added "3.0"}
(fact "counts the number of non-varidic argument types"

  (arg-count (fn [x])) => [1]

  (arg-count (fn [x & xs])) => []

  (arg-count (fn ([x]) ([x y]))) => [1 2])

^{:refer hara.function.base.arg/arg-check :added "3.0"}
(fact "counts the number of non-varidic argument types"

  (arg-check (fn [x]) 1) => true

  (arg-check (fn [x & xs]) 1) => true

  (arg-check (fn [x & xs]) 0)
  => (throws Exception "Function must accomodate 0 arguments"))
