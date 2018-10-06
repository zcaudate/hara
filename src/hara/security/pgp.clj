(ns hara.security.pgp
  (:require [hara.string :as string]
            [hara.core.encode :as encode]
            [hara.io.file :as fs]
            [hara.object.query :as reflect])
  (:import (java.security Security)
           (org.bouncycastle.openpgp PGPObjectFactory
                                     PGPPublicKey
                                     PGPPublicKeyRing
                                     PGPPrivateKey
                                     PGPSecretKey
                                     PGPSignature
                                     PGPSignatureGenerator
                                     PGPUtil)
           (org.bouncycastle.bcpg CRC24
                                  BCPGInputStream)
           (org.bouncycastle.jce.provider BouncyCastleProvider)
           (org.bouncycastle.openpgp.jcajce JcaPGPObjectFactory)
           (org.bouncycastle.openpgp.operator.jcajce JcePBESecretKeyDecryptorBuilder
                                                     JcaKeyFingerprintCalculator
                                                     JcaPGPKeyConverter)
           (org.bouncycastle.openpgp.operator.bc BcKeyFingerprintCalculator
                                                 BcPublicKeyDataDecryptorFactory
                                                 BcPGPContentSignerBuilder
                                                 BcPGPContentVerifierBuilderProvider)
           (org.bouncycastle.openpgp.bc BcPGPPublicKeyRingCollection
                                        BcPGPSecretKeyRingCollection)))

(defonce +bouncy-castle+
  (Security/addProvider (BouncyCastleProvider.)))

(defn load-public-keyring
  "loads a public keyring
 
   (load-public-keyring package/GNUPG-PUBLIC)"
  {:added "3.0"}
  [input]
  (-> (fs/input-stream input)
      (BcPGPPublicKeyRingCollection.)))

(defn load-secret-keyring
  "loads a secret keyring
 
   (load-secret-keyring package/GNUPG-SECRET)"
  {:added "3.0"}
  [input]
  (-> (fs/input-stream input)
      (BcPGPSecretKeyRingCollection.)))

(defn save-keyring
  "saves a keyring to file
 
   (-> package/GNUPG-SECRET
       (load-secret-keyring)
      (save-keyring \"hello.gpg\"))"
  {:added "3.0"}
  [keyring path]
  (->> (fs/output-stream path)
       (.encode keyring)))

(defn all-public-keys
  "returns all public keys within a keyring
 
   (-> package/GNUPG-PUBLIC
       (load-public-keyring)
      (all-public-keys))"
  {:added "3.0"}
  [rcoll]
  (->> (.getKeyRings rcoll)
       (iterator-seq)
       (map #(->> %
                  (.getPublicKeys)
                  (iterator-seq)))
       (apply concat)))

(defn fingerprint
  "returns the fingerprint of a public key
 
   (-> package/GNUPG-PUBLIC
       (load-public-keyring)
       (all-public-keys)
       (first)
       (fingerprint))
   => \"9B94FD0EA99482F6BC00777313319CB698B9A74D\""
  {:added "3.0"}
  [pub]
  (-> pub
      (.getFingerprint)
      (encode/to-hex)
      (.toUpperCase)))

(defmethod print-method PGPPublicKey
  [v ^java.io.Writer w]
  (.write w (str "#gpg.public[" (fingerprint v) "]")))

(defn get-public-key
  "returns public key given a partial fingerprint
 
   (-> package/GNUPG-PUBLIC
       (load-public-keyring)
      (get-public-key \"9B94FD0E\"))"
  {:added "3.0"}
  [rcoll sig]
  (->> (all-public-keys rcoll)
       (filter #(-> %
                    (fingerprint)
                    (.contains (.toUpperCase sig))))
       (first)))

(defn all-secret-keys
  "returns all secret keys within a keyring
 
   (-> package/GNUPG-SECRET
       (load-secret-keyring)
      (all-secret-keys))"
  {:added "3.0"}
  [rcoll]
  (->> (.getKeyRings rcoll)
       (iterator-seq)
       (map #(->> %
                  (.getSecretKeys)
                  (iterator-seq)))
       (apply concat)))

(defmethod print-method PGPSecretKey
  [v ^java.io.Writer w]
  (.write w (str "#gpg.secret[" (fingerprint (.getPublicKey v)) "]")))

(defn get-secret-key
  "returns secret key given a fingerprint
 
   (-> package/GNUPG-SECRET
       (load-secret-keyring)
      (get-secret-key \"9B94FD0E\"))"
  {:added "3.0"}
  [rcoll sig]
  (cond (string? sig)
        (->> (all-secret-keys rcoll)
             (filter #(-> %
                          (.getPublicKey)
                          (fingerprint)
                          (.contains (.toUpperCase sig))))
             (first))

        :else
        (->> (.getKeyRings rcoll)
             (iterator-seq)
             (keep #(.getSecretKey % sig))
             (first))))

(defmethod print-method PGPPrivateKey
  [v ^java.io.Writer w]
  (.write w (str "#gpg.private[" (.getKeyID v) "]")))

(defn get-keypair
  "returns public and private keys given a fingerprint
 
   (-> package/GNUPG-SECRET
       (load-secret-keyring)
       (get-keypair \"9B94FD0E\"))
   ;;=> [#key.public[9B94FD0EA99482F6BC00777313319CB698B9A74D]
   ;;    #key.private[1383058868639737677]]
 "
  {:added "3.0"}
  [rcoll sig]
  (let [decryptor (-> (JcePBESecretKeyDecryptorBuilder.)
                      (.setProvider "BC")
                      (.build (char-array "")))
        secret-key (get-secret-key rcoll sig)]
    (if secret-key
      [(.getPublicKey secret-key)
       (.extractPrivateKey secret-key decryptor)])))

(defn decrypt
  "returns the decrypted file given a keyring file
 
   (decrypt package/LEIN-CREDENTIALS-GPG
            package/GNUPG-SECRET)"
  {:added "3.0"}
  [encrypted-file keyring-file]
  (let [obj-factory  (-> (fs/input-stream encrypted-file)
                         (PGPUtil/getDecoderStream)
                         (PGPObjectFactory. (BcKeyFingerprintCalculator.)))
        rcoll         (load-secret-keyring keyring-file)
        enc-data     (-> (.nextObject obj-factory)
                         (.getEncryptedDataObjects)
                         (iterator-seq)
                         (first))
        key-id       (.getKeyID enc-data)
        [_ private-key]  (get-keypair rcoll key-id)
        clear-stream (-> (.getDataStream enc-data
                                         (BcPublicKeyDataDecryptorFactory. private-key))
                         (JcaPGPObjectFactory.)
                         (.nextObject)
                         (.getDataStream)
                         (JcaPGPObjectFactory.)
                         (.nextObject)
                         (.getDataStream))]
    (slurp clear-stream)))

(defn generate-signature
  "generates a signature given bytes and a keyring
 
   (generate-signature (fs/read-all-bytes \"project.clj\")
                       (load-secret-keyring hara.security.pgp.local/GNUPG-SECRET)
                      \"98B9A74D\")"
  {:added "3.0"}
  ([bytes [public-key private-key]]
   (let [sig-gen  (-> (BcPGPContentSignerBuilder.
                       (.getAlgorithm public-key)
                       PGPUtil/SHA256)
                      (PGPSignatureGenerator.))
         sig-gen  (doto sig-gen
                    (.init PGPSignature/BINARY_DOCUMENT private-key)
                    (.update bytes))]
     (.generate sig-gen)))
  ([bytes rcoll key-id]
   (generate-signature bytes (get-keypair rcoll key-id))))

(defmethod print-method PGPSignature
  [v ^java.io.Writer w]
  (.write w (str "#gpg.signature [\"" (encode/to-base64 (.getEncoded v)) "\"]")))

(defn crc-24
  "returns the crc24 checksum 
 
   (crc-24 (byte-array [100 100 100 100 100 100]))
   => [\"=6Fko\" [-24 89 40] 15227176]"
  {:added "3.0"}
  [input]
  (let [crc (CRC24.)
        _   (doseq [i (seq input)]
              (.update crc i))
        val (.getValue crc)
        bytes (-> (biginteger val)
                  (.toByteArray)
                  seq)
        bytes (case (count bytes)
                4 (rest bytes)
                3 bytes
                (nth (iterate #(cons 0 %)
                              bytes)
                     (- 3 (count bytes))))]
    [(->> (byte-array bytes)
          (encode/to-base64)
          (str "="))
     bytes
     val]))

(defn write-sig-file
  "writes bytes to a GPG compatible file
 
   (write-sig-file \"project.clj.asc\"
                   (-> (generate-signature (fs/read-all-bytes \"project.clj\")
                                          (load-secret-keyring
                                            hara.security.pgp.local/GNUPG-SECRET)
                                           \"98B9A74D\")
                       (.getEncoded)))"
  {:added "3.0"}
  [sig-file bytes]
  (->> (concat ["-----BEGIN PGP SIGNATURE-----"
                ""]
               (->> bytes
                    (encode/to-base64)
                    (partition-all 64)
                    (map #(apply str %)))
               [(first (crc-24 bytes))
                "-----END PGP SIGNATURE-----"])
       (string/joinr "\n")
       (spit sig-file)))

(defn read-sig-file
  "reads bytes from a GPG compatible file
 
   (read-sig-file \"project.clj.asc\")"
  {:added "3.0"}
  [sig-file]
  (->> (slurp sig-file)
       (string/split-lines)
       (reverse)
       (drop-while (fn [input]
                     (not (and (.startsWith input "=")
                               (= 5 (count input))))))
       (rest)
       (take 6)
       (reverse)
       (string/joinr "")
       (encode/from-base64)))

(defn sign
  "generates a output gpg signature for an input file
 
   (sign \"project.clj\"
         \"project.clj.asc\"
        hara.security.pgp.local/GNUPG-SECRET
         \"98B9A74D\")"
  {:added "3.0"}
  ([input sig-file [public-key private-key :as keypair]]
   (let [input-bytes (fs/read-all-bytes input)
         sig-bytes  (-> (generate-signature input-bytes keypair)
                        (.getEncoded))]
     (write-sig-file sig-file sig-bytes)
     sig-bytes))
  ([input sig-file keyring-file key-id]
   (let [rcoll (load-secret-keyring keyring-file)
         keypair (get-keypair rcoll key-id)]
     (sign input sig-file keypair))))

(defn pgp-signature
  "returns a gpg signature from encoded bytes
 
   (->  (generate-signature (fs/read-all-bytes \"project.clj\")
                            (load-secret-keyring
                            hara.security.pgp.local/GNUPG-SECRET)
                            \"98B9A74D\")
        (.getEncoded)
        (pgp-signature))"
  {:added "3.0"}
  [bytes]
  (let [make-pgp-signature (reflect/query-class PGPSignature ["new" [BCPGInputStream] :#])]
    (-> bytes
        (java.io.ByteArrayInputStream.)
        (BCPGInputStream.)
        (make-pgp-signature))))

(defn verify
  "verifies that the signature works
 
   (verify \"project.clj\"
           \"project.clj.asc\"
           hara.security.pgp.local/GNUPG-SECRET
           \"98B9A74D\")
   => true"
  {:added "3.0"}
  ([input sig-file public-key]
   (let [bytes (fs/read-all-bytes input)
         sig-bytes (read-sig-file sig-file)
         sig (if (zero? (count sig-bytes))
               (throw (Exception. (str "Not a valid signature file: " sig-file)))
               (pgp-signature sig-bytes))]
     (.init sig (BcPGPContentVerifierBuilderProvider.) public-key)
     (.update sig bytes)
     (.verify sig)))
  ([input sig-file keyring-file key-id]
   (let [rcoll (load-secret-keyring keyring-file)
         [public-key _] (get-keypair rcoll key-id)]
     (verify input sig-file public-key))))
