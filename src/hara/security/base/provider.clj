(ns hara.security.base.provider
  (:import (java.security AlgorithmParameters KeyFactory KeyPairGenerator KeyStore MessageDigest Provider Provider$Service Security Signature)
           (javax.crypto Cipher KeyGenerator Mac)))

(defn list-providers
  "list all security providers
 
   (list-providers)
   => [\"Apple\" \"SUN\" \"SunEC\" \"SunJCE\" \"SunJGSS\"
       \"SunJSSE\" \"SunPCSC\" \"SunRsaSign\" \"SunSASL\" \"XMLDSig\"]"
  {:added "3.0"}
  []
  (->> (Security/getProviders)
       (seq)
       (map #(.getName ^Provider %))
       (sort)))

(defn sort-services
  "filters and sorts the services by type"
  {:added "3.0"}
  [type services]
  (->> (seq services)
       (filter #(= type (.getType ^Provider$Service %)))
       (map #(.getAlgorithm ^Provider$Service %))
       (set)
       (sort)))

(defn list-services
  "lists all services that are available
 
   (list-services)
   => (\"AlgorithmParameterGenerator\" \"AlgorithmParameters\" ...)
 
   (list-services \"Cipher\")
   => (\"AES\" \"AESWrap\" \"AESWrap_128\" ...)
 
   (list-services \"KeyGenerator\" \"SunJCE\")
   => (\"AES\" \"ARCFOUR\" \"Blowfish\" \"DES\" \"DESede\" ...)"
  {:added "3.0"}
  ([]
   (->> (Security/getProviders)
        (mapcat #(.getServices ^Provider %))
        (map #(.getType ^Provider$Service %))
        (set)
        (sort)))
  ([type]
   (list-services type nil))
  ([type provider]
   (->> (if (nil? provider)
          (Security/getProviders)
          [(Security/getProvider provider)])
        (mapcat #(.getServices ^Provider %))
        (sort-services type))))

(defn cipher
  "lists or returns available `Cipher` implementations
 
   (cipher)
   => (\"AES\" \"AESWrap\" \"AESWrap_128\" ...)
 
   (cipher \"AES\")
   => javax.crypto.Cipher"
  {:added "3.0"}
  ([]
   (list-services "Cipher"))
  ([name]
   (cipher name nil))
  ([^String name ^String provider]
   (if (nil? provider)
     (Cipher/getInstance name)
     (Cipher/getInstance name provider))))

(defn key-factory
  "lists or returns available `KeyFactory` implementations
 
   (key-factory)
   => (\"DSA\" \"DiffieHellman\" \"EC\" \"RSA\")
 
   (key-factory \"RSA\")
   => java.security.KeyFactory"
  {:added "3.0"}
  ([]
   (list-services "KeyFactory"))
  ([name]
   (key-factory name nil))
  ([^String name ^String provider]
   (if (nil? provider)
     (KeyFactory/getInstance name)
     (KeyFactory/getInstance name provider))))

(defn key-generator
  "lists or returns available `KeyGenerator` implementations
 
   (key-generator)
   => (\"AES\" \"ARCFOUR\" \"Blowfish\" ...)
 
   (key-generator \"Blowfish\")
   => javax.crypto.KeyGenerator"
  {:added "3.0"}
  ([]
   (list-services "KeyGenerator"))
  ([name]
   (key-generator name nil))
  ([^String name ^String provider]
   (if (nil? provider)
     (KeyGenerator/getInstance name)
     (KeyGenerator/getInstance name provider))))

(defn key-pair-generator
  "lists or returns available `KeyPairGenerator` implementations
 
   (key-pair-generator)
   => (\"DSA\" \"DiffieHellman\" \"EC\" \"RSA\")
 
   (key-pair-generator \"RSA\")
   => java.security.KeyPairGenerator"
  {:added "3.0"}
  ([]
   (list-services "KeyPairGenerator"))
  ([name]
   (key-pair-generator name nil))
  ([^String name ^String provider]
   (if (nil? provider)
     (KeyPairGenerator/getInstance name)
     (KeyPairGenerator/getInstance name provider))))

(defn key-store
  "lists or returns available `KeyStore` implementations
 
   (key-store)
   => (\"CaseExactJKS\" \"DKS\" \"JCEKS\" \"JKS\" \"KeychainStore\" \"PKCS12\")
 
   (key-store \"JKS\")
   => java.security.KeyStore"
  {:added "3.0"}
  ([]
   (list-services "KeyStore"))
  ([name]
   (key-store name nil))
  ([^String name ^String provider]
   (if (nil? provider)
     (KeyStore/getInstance name)
     (KeyStore/getInstance name provider))))

(defn mac
  "lists or returns available `Mac` implementations
 
   (mac)
   => (\"HmacMD5\" \"HmacPBESHA1\" \"HmacSHA1\" ...)
 
   (mac \"HmacMD5\")
   => javax.crypto.Mac"
  {:added "3.0"}
  ([]
   (list-services "Mac"))
  ([name]
   (mac name nil))
  ([^String name ^String provider]
   (if (nil? provider)
     (Mac/getInstance name)
     (Mac/getInstance name provider))))

(defn message-digest
  "lists or returns available `MessageDigest` implementations
 
   (message-digest)
   => (\"MD2\" \"MD5\" \"SHA\" \"SHA-224\" \"SHA-256\" \"SHA-384\" \"SHA-512\")
 
   (message-digest \"MD2\")
   => java.security.MessageDigest$Delegate"
  {:added "3.0"}
  ([]
   (list-services "MessageDigest"))
  ([name]
   (message-digest name nil))
  ([^String name ^String provider]
   (if (nil? provider)
     (MessageDigest/getInstance name)
     (MessageDigest/getInstance name provider))))

(defn signature
  "lists or returns available `Signature` implementations
 
   (signature)
   => (\"MD2withRSA\" \"MD5andSHA1withRSA\" \"MD5withRSA\" ...)
 
   (signature \"MD2withRSA\")
   => java.security.Signature$Delegate"
  {:added "3.0"}
  ([]
   (list-services "Signature"))
  ([name]
   (signature name nil))
  ([^String name ^String provider]
   (if (nil? provider)
     (Signature/getInstance name)
     (Signature/getInstance name provider))))
