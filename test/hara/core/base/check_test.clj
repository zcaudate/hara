(ns hara.core.base.check-test
  (:use hara.test)
  (:require [hara.core.base.check :refer :all])
  (:refer-clojure :exclude [boolean? uri? double? bytes? uuid?]))

^{:refer hara.core.base.check/boolean? :added "3.0"}
(fact "Returns `true` if `x` is of type `java.lang.Boolean`."

  (boolean? true)   => true
  (boolean? false)  => true)

^{:refer hara.core.base.check/hash-map? :added "3.0"}
(fact "Returns `true` if `x` implements `clojure.lang.APersistentMap`."

  (hash-map? {})    => true
  (hash-map? [])    => false)

^{:refer hara.core.base.check/lazy-seq? :added "3.0"}
(fact "Returns `true` if `x` implements `clojure.lang.LazySeq`."

  (lazy-seq? (map inc [1 2 3]))  => true
  (lazy-seq? ())    => false)

^{:refer hara.core.base.check/byte? :added "3.0"}
(fact "Returns `true` if `x` is of type `java.lang.Byte`"

  (byte? (byte 1)) => true)

^{:refer hara.core.base.check/short? :added "3.0"}
(fact "Returns `true` if `x` is of type `java.lang.Short`"

  (short? (short 1)) => true)

^{:refer hara.core.base.check/long? :added "3.0"}
(fact "Returns `true` if `x` is of type `java.lang.Long`."

  (long? 1)          => true
  (long? 1N)         => false)

^{:refer hara.core.base.check/bigint? :added "3.0"}
(fact "Returns `true` if `x` is of type `clojure.lang.BigInt`."

  (bigint? 1N)       => true
  (bigint? 1)        =>  false)

^{:refer hara.core.base.check/double? :added "3.0"}
(fact "Returns `true` if `x` is of type `java.lang.Double`."

  (double? 1)            => false
  (double? (double 1))   => true)

^{:refer hara.core.base.check/bigdec? :added "3.0"}
(fact "Returns `true` if `x` is of type `java.math.BigDecimal`."

  (bigdec? 1M)       => true
  (bigdec? 1.0)      => false)

^{:refer hara.core.base.check/instant? :added "3.0"}
(fact "Returns `true` if `x` is of type `java.util.Date`."

  (instant? (java.util.Date.)) => true)

^{:refer hara.core.base.check/uuid? :added "3.0"}
(fact "Returns `true` if `x` is of type `java.util.UUID`."

  (uuid? (java.util.UUID/randomUUID)) => true)

^{:refer hara.core.base.check/uri? :added "3.0"}
(fact "Returns `true` if `x` is of type `java.net.URI`."

  (uri? (java.net.URI. "http://www.google.com")) => true)

^{:refer hara.core.base.check/url? :added "3.0"}
(fact "Returns `true` if `x` is of type `java.net.URL`."

  (url? (java.net.URL. "file:/Users/chris/Development")) => true)

^{:refer hara.core.base.check/regexp? :added "3.0"}
(fact "Returns `true` if `x` implements `clojure.lang.IPersistentMap`."

  (regexp? #"\d+") => true)

^{:refer hara.core.base.check/bytes? :added "3.0"}
(fact "Returns `true` if `x` is a primitive `byte` array."

  (bytes? (byte-array 8)) => true)

^{:refer hara.core.base.check/atom? :added "3.0"}
(fact "Returns `true` if `x` is of type `clojure.lang.Atom`."

  (atom? (atom nil)) => true)

^{:refer hara.core.base.check/ref? :added "3.0"}
(fact "Returns `true` if `x` is of type `clojure.lang.Ref`."

  (ref? (ref nil)) => true)

^{:refer hara.core.base.check/agent? :added "3.0"}
(fact "Returns `true` if `x` is of type `clojure.lang.Agent`."

  (agent? (agent nil)) => true)

^{:refer hara.core.base.check/iref? :added "3.0"}
(fact "Returns `true` if `x` is of type `clojure.lang.IRef`."

  (iref? (atom 0))  => true
  (iref? (ref 0))   => true
  (iref? (agent 0)) => true
  (iref? (promise)) => false
  (iref? (future))  => false)

^{:refer hara.core.base.check/ideref? :added "3.0"}
(fact "Returns `true` if `x` is of type `java.lang.IDeref`."

  (ideref? (atom 0))  => true
  (ideref? (promise)) => true
  (ideref? (future))  => true)

^{:refer hara.core.base.check/promise? :added "3.0"}
(fact "Returns `true` is `x` is a promise"

  (promise? (promise)) => true
  (promise? (future))  => false)

^{:refer hara.core.base.check/thread? :added "3.0"}
(fact "Returns `true` is `x` is a thread"

  (thread? (Thread/currentThread)) => true)

^{:refer hara.core.base.check/iobj? :added "3.0"}
(fact "checks if a component is instance of clojure.lang.IObj"

  (iobj? 1) => false

  (iobj? {}) => true)

^{:refer hara.core.base.check/type-checker :added "3.0"}
(fact "Returns the checking function associated with `k`"

  (type-checker :string) => #'clojure.core/string?

  (require '[hara.core.base.check :refer [bytes?]])
  (type-checker :bytes)  => #'hara.core.base.check/bytes?)

^{:refer hara.core.base.check/comparable? :added "3.0"}
(fact "Returns `true` if `x` and `y` both implements `java.lang.Comparable`."
  
  (comparable? 1 1) => true)

^{:refer hara.core.base.check/edn? :added "3.0"}
(fact "checks if an entry is valid edn"

  (edn? 1) => true

  (edn? {}) => true

  (edn? (java.util.Date.))
  => false)
