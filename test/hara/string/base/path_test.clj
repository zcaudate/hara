(ns hara.string.base.path-test
  (:use hara.test)
  (:require [hara.string.base.path :as path]
            [hara.string :as string]))

^{:refer hara.string.base.path/make-pattern :added "3.0"}
(fact "creates a regex pattern from a string"

  (path/make-pattern-raw ".")
  => (re-pattern "\\."))

^{:refer hara.string.base.path/path-join :added "3.0"}
(fact "joins a sequence of elements into a path separated value"

  (path/path-join ["a" "b" "c"])
  => "a/b/c"

  (string/path-join '[:a :b :c] "-")
  => :a-b-c
  
  (string/path-join '[a b c] '-)
  => 'a-b-c)

^{:refer hara.string.base.path/path-split :added "3.0"}
(fact "splits a sequence of elements into a path separated value"

  (path/path-split "a/b/c/d")
  => '["a" "b" "c" "d"]
  
  (path/path-split "a.b.c.d" ".")
  => ["a" "b" "c" "d"]
  
  (string/path-split :hello/world)
  => [:hello :world]
  
  (string/path-split :hello.world ".")
  => [:hello :world])

^{:refer hara.string.base.path/path-ns-array :added "3.0"}
(fact "returns the path vector of the string"
  
  (path/path-ns-array "a/b/c/d")
  => ["a" "b" "c"]

  (string/path-ns-array (keyword "a/b/c/d"))
  => [:a :b :c])

^{:refer hara.string.base.path/path-ns :added "3.0"}
(fact "returns the path namespace of the string"

  (path/path-ns "a/b/c/d")
  => "a/b/c"

  (string/path-ns :a.b.c ".")
  => :a.b)

^{:refer hara.string.base.path/path-root :added "3.0"}
(fact "returns the path root of the string"

  (path/path-root "a/b/c/d")
  => "a"

  (string/path-root 'a.b.c ".")
  => 'a)

^{:refer hara.string.base.path/path-stem-array :added "3.0"}
(fact "returns the path stem vector of the string"

  (path/path-stem-array "a/b/c/d")
  =>  ["b" "c" "d"]

  (string/path-stem-array 'a.b.c.d ".")
  => '[b c d])

^{:refer hara.string.base.path/path-stem :added "3.0"}
(fact "returns the path stem of the string"

  (path/path-stem "a/b/c/d")
  => "b/c/d"

  (string/path-stem 'a.b.c.d ".")
  => 'b.c.d)

^{:refer hara.string.base.path/path-val :added "3.0"}
(fact "returns the val of the string"

  (path/path-val "a/b/c/d")
  => "d"

  (string/path-val 'a.b.c.d ".")
  => 'd)

^{:refer hara.string.base.path/path-nth :added "3.0"}
(fact "check for the val of the string"

  (path/path-nth "a/b/c/d" 2)
  => "c")

^{:refer hara.string.base.path/path-sub-array :added "3.0"}
(fact "returns a sub array of the path within the string"

  (path/path-sub-array "a/b/c/d" 1 2)
  => ["b" "c"]

  (string/path-sub-array (symbol "a/b/c/d") 1 2)
  => '[b c])

^{:refer hara.string.base.path/path-sub :added "3.0"}
(fact "returns a subsection of the path within the string"

  (path/path-sub "a/b/c/d" 1 2)
  => "b/c"

  (string/path-sub (symbol "a/b/c/d") 1 2)
  => 'b/c)

^{:refer hara.string.base.path/path-count :added "3.0"}
(fact "counts the number of elements in a given path"

  (path/path-count "a/b/c")
  => 3
  
  (string/path-count *ns*)
  => 4)
