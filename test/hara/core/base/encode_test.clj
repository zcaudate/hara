(ns hara.core.base.encode-test
  (:use hara.test)
  (:require [hara.core.base.encode :refer :all]))

^{:refer hara.core.base.encode/hex-chars :added "3.0"}
(fact "turns a byte into two chars"

  (hex-chars 255)
  => [\f \f]

  (hex-chars 42)
  => [\2 \a])

^{:refer hara.core.base.encode/to-hex-chars :added "3.0"}
(fact "turns a byte array into a hex char array")

^{:refer hara.core.base.encode/to-hex :added "3.0"}
(fact "turns a byte array into hex string"

  (to-hex (.getBytes "hello"))
  => "68656c6c6f")

^{:refer hara.core.base.encode/from-hex-chars :added "3.0"}
(fact "turns two hex characters into a byte value"

  (byte (from-hex-chars \2 \a))
  => 42)

^{:refer hara.core.base.encode/from-hex :added "3.0"}
(fact "turns a hex string into a sequence of bytes"

  (String. (from-hex "68656c6c6f"))
  => "hello")
  
^{:refer hara.core.base.encode/to-base64-bytes :added "3.0"}
(fact "turns a byte array into a base64 encoding"

  (-> (.getBytes "hello")
      (to-base64-bytes)
      (String.))
  => "aGVsbG8=")

^{:refer hara.core.base.encode/to-base64 :added "3.0"}
(fact "turns a byte array into a base64 encoded string"

  (-> (.getBytes "hello")
      (to-base64))
  => "aGVsbG8=")

^{:refer hara.core.base.encode/from-base64 :added "3.0"}
(fact "turns a base64 encoded string into a byte array"

  (-> (from-base64 "aGVsbG8=")
      (String.))
  => "hello")