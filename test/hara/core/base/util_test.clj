(ns hara.core.base.util-test
  (:use hara.test)
  (:require [hara.core.base.util :refer :all]))

^{:refer hara.core.base.util/T :added "3.0"}
(fact "Returns `true` for any combination of input `args`"

  (T) => true
  (T :hello) => true
  (T 1 2 3) => true)

^{:refer hara.core.base.util/F :added "3.0"}
(fact "Returns `false` for any combination of input `args`"

  (F) => false
  (F :hello) => false
  (F 1 2 3) => false)

^{:refer hara.core.base.util/NIL :added "3.0"}
(fact "Returns `nil` for any combination of input `args`"

  (NIL) => nil
  (NIL :hello) => nil
  (NIL 1 2 3) => nil)

^{:refer hara.core.base.util/queue :added "3.0"}
(fact "Returns a `clojure.lang.PersistentQueue` object."

  (def a (queue 1 2 3 4))
  (pop a) => [2 3 4])

^{:refer hara.core.base.util/uuid :added "3.0"}
(fact "Returns a `java.util.UUID` object"

  (uuid) => #(instance? java.util.UUID %)

  (uuid "00000000-0000-0000-0000-000000000000")
  => #uuid "00000000-0000-0000-0000-000000000000")

^{:refer hara.core.base.util/instant :added "3.0"}
(fact "Returns a `java.util.Date` object"

  (instant) => #(instance? java.util.Date %)

  (instant 0) => #inst "1970-01-01T00:00:00.000-00:00")

^{:refer hara.core.base.util/uri :added "3.0"}
(fact "Returns a `java.net.URI` object"

  (uri "http://www.google.com")
  => #(instance? java.net.URI %))

^{:refer hara.core.base.util/hash-label :added "3.0"}
(fact "Returns a keyword repesentation of the hash-code. For use in
   generating internally unique keys"

  (hash-label 1) => "__1__"
  (hash-label "a" "b" "c") => "__97_98_99__"
  (hash-label "abc") => "__96354__")