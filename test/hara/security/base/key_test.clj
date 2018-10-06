(ns hara.security.base.key-test
  (:use hara.test)
  (:require [hara.security.base.key :refer :all]))

^{:refer hara.security.base.key/init-key-generator :added "3.0"}
(comment "initializes a `KeyGenerator` object")

^{:refer hara.security.base.key/generate-key :added "3.0"}
(comment "generates a key according to algorithm"

  (generate-key)
  => ("AES" "ARCFOUR" "Blowfish" "DES" "DESede"
            "HmacMD5" "HmacSHA1" "HmacSHA224" "HmacSHA256"
            "HmacSHA384" "HmacSHA512" ...)

  (generate-key "AES" {:length 128})
  ;;=> #key {:type "AES",
  ;;         :mode :secret,
  ;;         :format "RAW",
  ;;         :encoded "AQgv8l+vJNfnEWuhHs55wg=="}

  (generate-key "HmacSHA224" {:length 40})
  ;;=> #key {:type "HmacSHA224",
  ;;         :mode :secret,
  ;;         :format "RAW",
  ;;         :encoded "0qQkmic="}
)

^{:refer hara.security.base.key/init-key-pair-generator :added "3.0"}
(comment "initializes a `KeyPairGenerator` object")

^{:refer hara.security.base.key/generate-key-pair :added "3.0"}
(comment "creates a public and private key pair"

  (generate-key-pair)
  => ("DSA" "DiffieHellman" "EC" "RSA")

  (generate-key-pair "RSA" {:length 512})
  ;;=> [#key {:type "RSA",
  ;;          :mode :public,
  ;;          :format "X.509",
  ;;          :encoded "...." }
  ;;    #key {:type "RSA",
  ;;          :mode :private,
  ;;          :format "PKCS#8",
  ;;          :encoded "..."}]
)

^{:refer hara.security.base.key/key-mode :added "3.0"}
(fact "returns the mode of a key"

  (->> (generate-key-pair "RSA" {:length 512})
       (map key-mode))
  => [:public :private])

^{:refer hara.security.base.key/key-type :added "3.0"}
(fact "returns the type of a key"

  (key-type (generate-key "AES" {:length 128}))
  => "AES")

^{:refer hara.security.base.key/key->map :added "3.0"}
(fact "returns a map representation of a key"

  (key->map (generate-key "AES" {:length 128}))
  => (contains {:type "AES",
                :mode :secret,
                :format "RAW",
                :encoded string?}))

^{:refer hara.security.base.key/to-bytes :added "3.0"}
(comment "transforms input to a byte array")

^{:refer hara.security.base.key/map->key :added "3.0"}
(fact "transforms a map into a key"

  (map->key {:type "AES",
             :mode :secret,
             :format "RAW",
             :encoded "euHlt5sHWhRpbKZHjrwrrQ=="})
  => java.security.Key)

^{:refer hara.security.base.key/->key :added "3.0"}
(fact "idempotent function converting input into a key"

  (-> {:type "AES",
       :mode :secret,
       :format "RAW",
       :encoded "euHlt5sHWhRpbKZHjrwrrQ=="}
      (->key)
      (->key))
  => java.security.Key)