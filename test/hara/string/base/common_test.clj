(ns hara.string.base.common-test
  (:use hara.test)
  (:require [hara.string.base.common :refer :all]
            [hara.string :as string])
  (:refer-clojure :exclude [reverse replace]))

^{:refer hara.string.base.common/blank? :added "3.0"}
(fact "checks if string is empty or nil"

  (blank? nil)
  => true

  (blank? "")
  => true)

^{:refer hara.string.base.common/split :added "3.0"}
(fact "splits the string into tokens"

  (split "a b c" #" ")
  => ["a" "b" "c"]

  (string/split :a.b.c (re-pattern "\\."))
  => [:a :b :c])

^{:refer hara.string.base.common/split-lines :added "3.0"}
(fact "splits the string into separate lines"

  (split-lines "a\nb\nc")
  => ["a" "b" "c"])

^{:refer hara.string.base.common/join :added "3.0"}
(fact "joins an array using a separator"

  (join ["a" "b" "c"] ".")
  => "a.b.c"

  (join ["a" "b" "c"])
  => "abc"

  (string/join [:a :b :c] "-")
  => :a-b-c)

^{:refer hara.string.base.common/joinr :added "3.0"}
(fact "like `join` but used with `->>` opts"

  (joinr "." ["a" "b" "c"])
  => "a.b.c"

  (string/joinr "." '[a b c])
  => 'a.b.c)

^{:refer hara.string.base.common/upper-case :added "3.0"}
(fact "converts a string object to upper case"

  (upper-case "hello-world")
  => "HELLO-WORLD"

  (string/upper-case :hello-world)
  => :HELLO-WORLD)

^{:refer hara.string.base.common/lower-case :added "3.0"}
(fact "converts a string object to lower case"
  
  (lower-case "Hello.World")
  => "hello.world"

  (string/lower-case 'Hello.World)
  => 'hello.world)

^{:refer hara.string.base.common/capital-case :added "3.0"}
(fact "converts a string object to capital case"

  (capital-case "hello.World")
  => "Hello.world"

  (string/capital-case 'hello.World)
  => 'Hello.world)

^{:refer hara.string.base.common/reverse :added "3.0"}
(fact "reverses the string"

  (reverse "hello")
  => "olleh"

  (string/reverse :hello)
  => :olleh)

^{:refer hara.string.base.common/starts-with? :added "3.0"}
(fact "checks if string starts with another"

  (starts-with? "hello" "hel")
  => true

  (string/starts-with? 'hello 'hel)
  => true)

^{:refer hara.string.base.common/ends-with? :added "3.0"}
(fact "checks if string ends with another"

  (ends-with? "hello" "lo")
  => true

  (string/ends-with? 'hello 'lo)
  => true)

^{:refer hara.string.base.common/includes? :added "3.0"}
(fact "checks if first string contains the second"

  (includes? "hello" "ell")
  => true

  (string/includes? 'hello 'ell)
  => true)

^{:refer hara.string.base.common/trim :added "3.0"}
(fact "trims the string of whitespace"

  (trim "   hello   ")
  => "hello")

^{:refer hara.string.base.common/trim-left :added "3.0"}
(fact "trims the string of whitespace on left"

  (trim-left "   hello   ")
  => "hello   ")

^{:refer hara.string.base.common/trim-right :added "3.0"}
(fact "trims the string of whitespace on right"

  (trim-right "   hello   ")
  => "   hello")

^{:refer hara.string.base.common/trim-newlines :added "3.0"}
(fact "removes newlines from right"

  (trim-newlines  "\n\n    hello   \n\n")
  => "\n\n    hello   ")

^{:refer hara.string.base.common/replace :added "3.0"}
(fact "replace value in string with another"

  (replace "hello" "el" "AL")
  => "hALlo"

  (string/replace :hello "el" "AL")
  => :hALlo)

^{:refer hara.string.base.common/caseless= :added "3.0"}
(fact "compares two values ignoring case"

  (caseless= "heLLo" "HellO")
  => true

  (string/caseless= 'heLLo :HellO)
  => true)
