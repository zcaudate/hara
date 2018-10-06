(ns hara.security.pgp.local)

(def GNUPG-SECRET
  (str (System/getProperty "user.home") "/.gnupg/secring.gpg"))

(def GNUPG-PUBLIC
  (str (System/getProperty "user.home") "/.gnupg/pubring.gpg"))

(def LEIN-PROFILE
  (str (System/getProperty "user.home") "/.lein/profiles.clj"))

(def LEIN-CREDENTIALS
  (str (System/getProperty "user.home") "/.lein/credentials.clj"))

(def LEIN-CREDENTIALS-GPG
  (str (System/getProperty "user.home") "/.lein/credentials.clj.gpg"))
