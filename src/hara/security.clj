(ns hara.security
  (:require [hara.module :as module]
            [hara.security.base.cipher :deps true]
            [hara.security.base.key :deps true]
            [hara.security.base.provider :deps true]
            [hara.security.base.verify :deps true])
  (:import (java.security Security)))

(module/include
 (hara.security.base.cipher encrypt
                       decrypt)

 (hara.security.base.key generate-key
                    generate-key-pair
                    ->key
                    key->map)

 (hara.security.base.provider list-providers
                         list-services
                         cipher
                         key-generator
                         key-pair-generator
                         key-store
                         mac
                         message-digest
                         signature)

 (hara.security.base.verify digest
                       hmac
                       sign
                       verify))
