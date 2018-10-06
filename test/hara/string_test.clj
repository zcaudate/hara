(ns hara.string-test
  (:use hara.test)
  (:require [hara.string :as string])
  (:refer-clojure :exclude [format = subs]))

^{:refer hara.string/split :added "3.0"}
(fact "splits a string given a regex"

  (string/split "a b" #" ") => ["a" "b"]
  (string/split " " #" ") => ["" ""])

^{:refer hara.string/split-lines :added "3.0"}
(fact "splits a string given newlines"

  (string/split-lines "a\nb") => ["a" "b"]
  
  (string/split-lines "\n") => ["" ""])

^{:refer hara.string/from-string :added "3.0"}
(fact "meta information of keywords and symbols"

  (string/from-string "hello/world" clojure.lang.Symbol)
  => 'hello/world

  (string/from-string "hello/world" clojure.lang.Keyword)
  => :hello/world)

^{:refer hara.string/to-string :added "3.0"}
(fact "converts symbols and keywords to string representation"

  (string/to-string 'hello/world)
  => "hello/world"

  (string/to-string :hello/world)
  => "hello/world")

^{:refer hara.string/copy-string-var :added "3.0"}
(fact "creates a function, augmenting it with string conversion properties"

  (string/copy-string-var :op false *ns* '-subs- #'string/subs)
  => #'hara.string-test/-subs-

  (-subs- :hello 3)
  => :lo

  (-subs- :hello 1 4)
  => :ell)

^{:refer hara.string/= :added "3.0"}
(fact "compares two string-like things"

  (string/= :a 'a)
  => true

  (string/= *ns* :hara.string-test)
  => true)

^{:refer hara.string/clojure!core :added "3.0"}
(fact "compares two string-like things"

  (string/subs :hello-world  3 8)
  => :lo-wo

  (string/format :hello%d-world  100)
  => :hello100-world)

^{:refer hara.string/joinr :added "3.0"}
(fact "joins a list together"

  (string/joinr "." [:a :b :c])
  => :a.b.c)
