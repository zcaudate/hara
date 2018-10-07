(ns hara.security.base.verify-test
  (:use hara.test)
  (:require [hara.security.base.verify :refer :all]
            [hara.security.base.key :as key]
            [hara.core.base.encode :as encode]
            [clojure.string :as string]))

^{:refer hara.security.base.verify/digest :added "3.0"}
(fact "creates a digest out of a byte array"

  (digest)
  => (contains ["MD2" "MD5" "SHA" "SHA-224"
                "SHA-256" "SHA-384" "SHA-512"] :in-any-order :gaps-ok)

  (-> (digest (.getBytes "hello world")
              "SHA")
      (encode/to-hex))
  => "2aae6c35c94fcfb415dbe95f408b9ce91ee846ed")

^{:refer hara.security.base.verify/init-hmac :added "3.0"}
(comment "initializes the hmac algorithm")

^{:refer hara.security.base.verify/hmac :added "3.0"}
(fact "creates a key encrypted digest"

  (-> (hmac (.getBytes "hello world")
            {:type "HmacSHA1",
             :mode :secret,
             :format "RAW",
             :encoded "wQ0lyydDSEFRKviwv/2BoWVQDpj8hbUiUXytuWj7Yv8="})
      (encode/to-hex))
  => "a6f9e08fad62f63a35c6fd320f4249c9ad3079dc")

^{:refer hara.security.base.verify/sign :added "3.0"}
(fact "creates a signature using a private key"

  (sign)
  ;; => (contains ["MD2withRSA" "MD5andSHA1withRSA" "MD5withRSA"
  ;;               "NONEwithDSA" "NONEwithECDSA" "SHA1withDSA"
  ;;               "SHA1withECDSA" "SHA1withRSA" "SHA224withDSA"
  ;;               "SHA224withECDSA" "SHA224withRSA" "SHA256withDSA"
  ;;               "SHA256withECDSA" "SHA256withRSA" "SHA384withECDSA"
  ;;               "SHA384withRSA" "SHA512withECDSA" "SHA512withRSA"]
  ;;              :in-any-order)

  (-> (sign
       (.getBytes "hello world")
       {:type "RSA",
        :mode :private,
        :format "PKCS#8",
        :encoded
        (apply str
               ["MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAmrOdqA5ZGMJ6"
                "55meZCnj44B65ZUXnAscXu7GJcNQO91Z7B9NmWX/P59BBUC/yJ6s/ugEffhP"
                "wCYJt013GkV6tQIDAQABAkBJxzV+C3G0XDOvNlUSoeO8AO8bhJIg6i+amrdH"
                "FTGzimwp/eyOGZlXpHcaK57kSBK4npXgfWCFFLNuvAtCrQ91AiEA0McEFHMS"
                "MTVU/78kDYSsJ+lty6izxkONp/XZ4+T6BDsCIQC9sWmBYAFDfiHvLnv2NT7O"
                "08LR+UnNuDalduukc649zwIhAKXHEadHRA/M4GR/Gxqc2bKLeUJ4/98TrvzK"
                "jCyYmioXAiAiOg2wY1M3C14yGvARB6ByjzD61AEmFlP93Qw9mwXYbwIhALR/"
                "Uv4DvJJbR7mpRXcRCo9Me1wawdCndM5ZyF7Hvpu4"])}
       {:algorithm "MD5withRSA"})
      (encode/to-hex))
  => (apply str ["5ba863c3e24c73f09d50749698ae82406490c"
                 "edc4566810461480e37da661754b7bf33cc6b"
                 "bf0f48646304c8994202d2fd7094e7420049f"
                 "eaa512c8cd72d7000"]))

^{:refer hara.security.base.verify/verify :added "3.0"}
(fact "verifies a signature using a public key"

  (verify (.getBytes "hello world")
          (->> ["5ba863c3e24c73f09d50749698ae82406490c"
                "edc4566810461480e37da661754b7bf33cc6b"
                "bf0f48646304c8994202d2fd7094e7420049f"
                "eaa512c8cd72d7000"]
               (apply str)
               (encode/from-hex))
          {:type "RSA",
           :mode :public,
           :format "X.509",
           :encoded
           (apply str
                  ["MFwwDQYJKoZIhvcNAQEBBQADSwAwSA"
                   "JBAJqznagOWRjCeueZnmQp4+OAeuWV"
                   "F5wLHF7uxiXDUDvdWewfTZll/z+fQQ"
                   "VAv8ierP7oBH34T8AmCbdNdxpFerUC"
                   "AwEAAQ=="])}
          {:algorithm "MD5withRSA"})
  => true)

(comment
  (./import))
