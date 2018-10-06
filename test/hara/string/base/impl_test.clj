(ns hara.string.base.impl-test
  (:use hara.test)
  (:require [hara.string.base.impl :refer :all]))

^{:refer hara.string.base.impl/from-string :added "3.0"}
(fact "converts a string to an object"

  (from-string "a" clojure.lang.Symbol)
  => 'a

  (from-string "hara.string" clojure.lang.Namespace)
  => (find-ns 'hara.string))

^{:refer hara.string.base.impl/to-string :added "3.0"}
(fact "converts an object to a string"

  (to-string :hello/world)
  => "hello/world"

  (to-string *ns*)
  => "hara.string.base.impl-test")

^{:refer hara.string.base.impl/path-separator :added "3.0"}
(fact "returns the default path separator for an object"

  (path-separator clojure.lang.Namespace)
  => "."

  (path-separator clojure.lang.Keyword)
  => "/")

^{:refer hara.string.base.impl/wrap-op :added "3.0"}
(fact "wraps a string function such that it can take any string-like input"

  ((wrap-op str false) :hello 'hello)
  => :hellohello

  ((wrap-op str true) :hello 'hello)
  => "hellohello")

^{:refer hara.string.base.impl/wrap-compare :added "3.0"}
(fact "wraps a function so that it can compare any two string-like inputs"

  ((wrap-compare =) :hello 'hello)
  => true)
