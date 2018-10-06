(ns hara.security.base.provider-test
  (:use hara.test)
  (:require [hara.security.base.provider :refer :all]))

^{:refer hara.security.base.provider/list-providers :added "3.0"}
(comment "list all security providers"

  (list-providers)
  => ["Apple" "SUN" "SunEC" "SunJCE" "SunJGSS"
      "SunJSSE" "SunPCSC" "SunRsaSign" "SunSASL" "XMLDSig"])

^{:refer hara.security.base.provider/sort-services :added "3.0"}
(comment "filters and sorts the services by type")

^{:refer hara.security.base.provider/list-services :added "3.0"}
(comment "lists all services that are available"

  (list-services)
  => ("AlgorithmParameterGenerator" "AlgorithmParameters" ...)

  (list-services "Cipher")
  => ("AES" "AESWrap" "AESWrap_128" ...)

  (list-services "KeyGenerator" "SunJCE")
  => ("AES" "ARCFOUR" "Blowfish" "DES" "DESede" ...))

^{:refer hara.security.base.provider/cipher :added "3.0"}
(comment "lists or returns available `Cipher` implementations"

  (cipher)
  => ("AES" "AESWrap" "AESWrap_128" ...)

  (cipher "AES")
  => javax.crypto.Cipher)

^{:refer hara.security.base.provider/key-factory :added "3.0"}
(comment "lists or returns available `KeyFactory` implementations"

  (key-factory)
  => ("DSA" "DiffieHellman" "EC" "RSA")

  (key-factory "RSA")
  => java.security.KeyFactory)

^{:refer hara.security.base.provider/key-generator :added "3.0"}
(comment "lists or returns available `KeyGenerator` implementations"

  (key-generator)
  => ("AES" "ARCFOUR" "Blowfish" ...)

  (key-generator "Blowfish")
  => javax.crypto.KeyGenerator)

^{:refer hara.security.base.provider/key-pair-generator :added "3.0"}
(comment "lists or returns available `KeyPairGenerator` implementations"

  (key-pair-generator)
  => ("DSA" "DiffieHellman" "EC" "RSA")

  (key-pair-generator "RSA")
  => java.security.KeyPairGenerator)

^{:refer hara.security.base.provider/key-store :added "3.0"}
(comment "lists or returns available `KeyStore` implementations"

  (key-store)
  => ("CaseExactJKS" "DKS" "JCEKS" "JKS" "KeychainStore" "PKCS12")

  (key-store "JKS")
  => java.security.KeyStore)

^{:refer hara.security.base.provider/mac :added "3.0"}
(comment "lists or returns available `Mac` implementations"

  (mac)
  => ("HmacMD5" "HmacPBESHA1" "HmacSHA1" ...)

  (mac "HmacMD5")
  => javax.crypto.Mac)

^{:refer hara.security.base.provider/message-digest :added "3.0"}
(comment "lists or returns available `MessageDigest` implementations"

  (message-digest)
  => ("MD2" "MD5" "SHA" "SHA-224" "SHA-256" "SHA-384" "SHA-512")

  (message-digest "MD2")
  => java.security.MessageDigest$Delegate)

^{:refer hara.security.base.provider/signature :added "3.0"}
(comment "lists or returns available `Signature` implementations"

  (signature)
  => ("MD2withRSA" "MD5andSHA1withRSA" "MD5withRSA" ...)

  (signature "MD2withRSA")
  => java.security.Signature$Delegate)