(ns hara.security.base.key
  (:require [hara.core.encode :as encode]
            [hara.security.base.provider :as provider])
  (:import (java.io Writer)
           (java.security Key KeyFactory KeyPair KeyPairGenerator PrivateKey PublicKey SecureRandom)
           (java.security.spec AlgorithmParameterSpec PKCS8EncodedKeySpec X509EncodedKeySpec)
           (javax.crypto KeyGenerator)
           (javax.crypto.spec SecretKeySpec)))

(defn init-key-generator
  "initializes a `KeyGenerator` object"
  {:added "3.0"}
  [^KeyGenerator gen
   {:keys [^int length params random] :as opts}]
  (cond (and params random)
        (.init gen
               ^AlgorithmParameterSpec params
               ^SecureRandom random)

        params
        (.init gen ^AlgorithmParameterSpec params)

        (and length random)
        (.init gen length ^SecureRandom random)

        random
        (.init gen ^SecureRandom random)

        :else
        (.init gen length)))

(defn generate-key
  "generates a key according to algorithm
 
   (generate-key)
   => (\"AES\" \"ARCFOUR\" \"Blowfish\" \"DES\" \"DESede\"
             \"HmacMD5\" \"HmacSHA1\" \"HmacSHA224\" \"HmacSHA256\"
             \"HmacSHA384\" \"HmacSHA512\" ...)
 
   (generate-key \"AES\" {:length 128})
   ;;=> #key {:type \"AES\",
   ;;         :mode :secret,
   ;;         :format \"RAW\",
   ;;         :encoded \"AQgv8l+vJNfnEWuhHs55wg==\"}
 
   (generate-key \"HmacSHA224\" {:length 40})
   ;;=> #key {:type \"HmacSHA224\",
   ;;         :mode :secret,
   ;;         :format \"RAW\",
   ;;         :encoded \"0qQkmic=\"}
 "
  {:added "3.0"}
  ([] (provider/key-generator))
  ([algo {:keys [provider] :as opts}]
   {:pre [(:length opts)]}
   (-> ^KeyGenerator (provider/key-generator algo provider)
       (doto (init-key-generator opts))
       (.generateKey))))

(defn init-key-pair-generator
  "initializes a `KeyPairGenerator` object"
  {:added "3.0"}
  [^KeyPairGenerator gen
   {:keys [^int length params random] :as opts}]
  (cond (and params random)
        (.initialize gen
                     ^AlgorithmParameterSpec params
                     ^SecureRandom random)

        params
        (.initialize gen ^AlgorithmParameterSpec params)

        (and length random)
        (.initialize gen
                     length
                     ^SecureRandom random)

        :else
        (.initialize gen length)))

(defn generate-key-pair
  "creates a public and private key pair
 
   (generate-key-pair)
   => (\"DSA\" \"DiffieHellman\" \"EC\" \"RSA\")
 
   (generate-key-pair \"RSA\" {:length 512})
   ;;=> [#key {:type \"RSA\",
   ;;          :mode :public,
   ;;          :format \"X.509\",
   ;;          :encoded \"....\" }
   ;;    #key {:type \"RSA\",
   ;;          :mode :private,
   ;;          :format \"PKCS#8\",
   ;;          :encoded \"...\"}]
 "
  {:added "3.0"}
  ([]
   (provider/key-pair-generator))
  ([type {:keys [provider] :as opts}]
   {:pre [(:length opts)]}
   (-> ^KeyPairGenerator (provider/key-pair-generator type provider)
       (doto (init-key-pair-generator opts))
       (.generateKeyPair)
       ((juxt #(.getPublic ^KeyPair %)
              #(.getPrivate ^KeyPair %))))))

(defn key-mode
  "returns the mode of a key
 
   (->> (generate-key-pair \"RSA\" {:length 512})
        (map key-mode))
   => [:public :private]"
  {:added "3.0"}
  [k]
  (cond (map? k)
        (:mode k)

        (instance? PublicKey k)
        :public

        (instance? PrivateKey k)
        :private

        :else
        :secret))

(defn key-type
  "returns the type of a key
 
   (key-type (generate-key \"AES\" {:length 128}))
   => \"AES\""
  {:added "3.0"}
  [k]
  (cond (map? k)
        (:type k)

        :else
        (.getAlgorithm ^Key k)))

(defn key->map
  "returns a map representation of a key
 
   (key->map (generate-key \"AES\" {:length 128}))
   => (contains {:type \"AES\",
                 :mode :secret,
                 :format \"RAW\",
                 :encoded string?})"
  {:added "3.0"}
  [^Key k]
  (cond (map? k) k

        :else
        {:type    (.getAlgorithm k)
         :mode    (key-mode k)
         :format  (.getFormat k)
         :encoded (encode/to-base64 (.getEncoded k))}))

(defn to-bytes
  "transforms input to a byte array"
  {:added "3.0"}
  [input]
  (cond (instance? (Class/forName "[B") input)
        input

        :else
        (encode/from-base64 input)))

(defmulti map->key
  "transforms a map into a key
 
   (map->key {:type \"AES\",
              :mode :secret,
              :format \"RAW\",
              :encoded \"euHlt5sHWhRpbKZHjrwrrQ==\"})
   => java.security.Key"
  {:added "3.0"}
  (fn [{:keys [mode]}] mode))

(defmethod map->key :default
  [{:keys [type encoded]}]
  (SecretKeySpec. (to-bytes encoded) type))

(defmethod map->key :public
  [{:keys [type format encoded]}]
  (let [factory ^KeyFactory (provider/key-factory type)
        spec (X509EncodedKeySpec. (to-bytes encoded))]
    (.generatePublic factory spec)))

(defmethod map->key :private
  [{:keys [type format encoded]}]
  (let [factory  ^KeyFactory (provider/key-factory type)
        spec (PKCS8EncodedKeySpec. (to-bytes encoded))]
    (.generatePrivate factory spec)))

(defn ->key
  "idempotent function converting input into a key
 
   (-> {:type \"AES\",
        :mode :secret,
        :format \"RAW\",
        :encoded \"euHlt5sHWhRpbKZHjrwrrQ==\"}
       (->key)
       (->key))
   => java.security.Key"
  {:added "3.0"}
  [k]
  (cond (map? k)
        (map->key k)

        :else k))

(defmethod print-method Key
  [v ^Writer w]
  (.write w (str "#key " (key->map v))))

(defmethod print-method PublicKey
  [v ^Writer w]
  (.write w (str "#key " (key->map v))))

(defmethod print-method PrivateKey
  [v ^Writer w]
  (.write w (str "#key " (key->map v))))
