(ns hara.core.debug-test
  (:use hara.test)
  (:require [hara.core.debug :refer :all]
            [clojure.string :as string]))

^{:refer hara.core.debug/say :added "3.0"}
(comment "enables audio debugging on osx"

  (say "hello there"))

^{:refer hara.core.debug/wrap-print :added "3.0"}
(fact "wraps a function in a `println` statement'"

  ((hara.core.debug/wrap-print + (quote (+ 1 2 3)) (quote ->)) 1 2 3)
  => 6

  (with-out-str
    ((hara.core.debug/wrap-print + (quote (+ 1 2 3)) (quote ->)) 1 2 3))
  => "-> (+ 1 2 3) :: 6\n")

^{:refer hara.core.debug/dbg-print :added "3.0"}
(fact "creates the form for debug print"

  (dbg-print '(+ 1 2 3) '->)
  => '((hara.core.debug/wrap-print + (quote (+ 1 2 3)) (quote ->)) 1 2 3))

^{:refer hara.core.debug/dbg-> :added "3.0"}
(fact "prints each stage of the `->` macro"
  (-> (dbg-> {:a 1}
             (assoc :b 2)
             (merge {:c 3}))
      (with-out-str)
      (string/split-lines))
  => ["" ""
      "{:a 1}"
      "-> (assoc :b 2) :: {:a 1, :b 2}"
      "-> (merge {:c 3}) :: {:a 1, :b 2, :c 3}"])

^{:refer hara.core.debug/dbg->> :added "3.0"}
(fact "prints each stage of the `->>` macro"

  (->  (dbg->> (range 5)
               (map inc)
               (take 2))
       (with-out-str)
       (string/split-lines))
  => ["" ""
      "(0 1 2 3 4)"
      "->> (map inc) :: (1 2 3 4 5)"
      "->> (take 2) :: (1 2)"])

^{:refer hara.core.debug/->doto :added "3.0"}
(fact "used to perform side-effects within a `->` macro"

  (-> {:a 1}
      (->doto (update-in [:a] inc) print)
      (assoc :b 2))
  ;; {:a 2}
  => {:a 1, :b 2})

^{:refer hara.core.debug/->>doto :added "3.0"}
(fact "used to perform side effects within a `->>` macro"

  (->> [1 2 3]
       (->>doto (map inc) print)
       (cons 0))
  ;; (2 3 4)
  => [0 1 2 3])

^{:refer hara.core.debug/->prn :added "3.0"}
(fact "used to print within the macro"

  (-> [1 2 3]
      (->prn)
      (conj 4))
  ;; [1 2 3]
  => [1 2 3 4])
