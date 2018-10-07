(ns hara.security.base.cipher-test
  (:use hara.test)
  (:require [hara.security.base.cipher :refer :all]
            [hara.core.base.encode :as encode]))

^{:refer hara.security.base.cipher/init-cipher :added "3.0"}
(comment "initializes cipher according to options")

^{:refer hara.security.base.cipher/operate :added "3.0"}
(comment "base function for encrypt and decrypt")

^{:refer hara.security.base.cipher/encrypt :added "3.0"}
(fact "encrypts a byte array using a key"

  (-> (encrypt (.getBytes "hello world")
               {:type "AES",
                :mode :secret,
                :format "RAW",
                :encoded "euHlt5sHWhRpbKZHjrwrrQ=="})
      (encode/to-hex))
  => "30491ab4427e45909f3d2f5d600b0f93")

^{:refer hara.security.base.cipher/decrypt :added "3.0"}
(fact "decrypts a byte array using a key"

  (-> (decrypt (encode/from-hex  "30491ab4427e45909f3d2f5d600b0f93")
               {:type "AES",
                :mode :secret,
                :format "RAW",
                :encoded "euHlt5sHWhRpbKZHjrwrrQ=="})
      (String.))
  => "hello world")