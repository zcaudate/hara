(ns hara.string.base.type-test
  (:use hara.test)
  (:require [hara.string.base.type :refer :all]
            [hara.string :as string]))

^{:refer hara.string.base.type/re-sub :added "3.0"}
(fact "substitute a pattern by applying a function"

  (re-sub "aTa" +hump-pattern+ (fn [_] "$"))
  => "$a")

^{:refer hara.string.base.type/separate-humps :added "3.0"}
(fact "separate words that are camel cased"
  
  (separate-humps "aTaTa")
  => "a Ta Ta")

^{:refer hara.string.base.type/camel-type :added "3.0"}
(fact "converts a string-like object to camel case representation"

  (camel-type "hello-world")
  => "helloWorld"

  (string/camel-type 'hello_world)
  => 'helloWorld)

^{:refer hara.string.base.type/capital-type :added "3.0"}
(fact "converts a string-like object to captital case representation"

  (capital-type "hello world")
  => "Hello World"

  (str (string/capital-type :hello-world))
  => ":Hello World")

^{:refer hara.string.base.type/lower-type :added "3.0"}
(fact "converts a string-like object to a lower case representation"

  (lower-type "helloWorld")
  => "hello world"

  (string/lower-type 'hello-world)
  => (symbol "hello world"))

^{:refer hara.string.base.type/pascal-type :added "3.0"}
(fact "converts a string-like object to a pascal case representation"

  (pascal-type "helloWorld")
  => "HelloWorld" 

  (string/pascal-type :hello-world)
  => :HelloWorld)

^{:refer hara.string.base.type/phrase-type :added "3.0"}
(fact "converts a string-like object to snake case representation"

  (phrase-type "hello-world")
  => "Hello world")

^{:refer hara.string.base.type/snake-type :added "3.0"}
(fact "converts a string-like object to snake case representation"

  (snake-type "hello-world")
  => "hello_world"

  (string/snake-type 'helloWorld)
  => 'hello_world)

^{:refer hara.string.base.type/spear-type :added "3.0"}
(fact "converts a string-like object to spear case representation"

  (spear-type "hello_world")
  => "hello-world"

  (string/spear-type 'helloWorld)
  => 'hello-world)

^{:refer hara.string.base.type/upper-type :added "3.0"}
(fact "converts a string-like object to upper case representation"

  (upper-type "hello world")
  => "HELLO WORLD"

  (str (string/upper-type 'hello-world))
  => "HELLO WORLD")

^{:refer hara.string.base.type/typeless= :added "3.0"}
(fact "compares two representations "

  (typeless= "helloWorld" "hello_world")
  => true
  
  (string/typeless= :a-b-c "a b c")
  => true

  (string/typeless= 'getMethod :get-method)
  => true)
