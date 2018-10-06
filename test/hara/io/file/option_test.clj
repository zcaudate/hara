(ns hara.io.file.option-test
  (:use hara.test)
  (:require [hara.io.file.option :refer :all]
            [hara.core.base.enum :as enum])
  (:import (java.nio.file AccessMode)))

^{:refer hara.io.file.option/to-mode-string :added "3.0"}
(fact "transforms mode numbers to mode strings"

  (to-mode-string "455")
  => "r--r-xr-x"

  (to-mode-string "777")
  => "rwxrwxrwx")

^{:refer hara.io.file.option/to-mode-number :added "3.0"}
(fact "transforms mode numbers to mode strings"

  (to-mode-number "r--r-xr-x")
  => "455"

  (to-mode-number "rwxrwxrwx")
  => "777")

^{:refer hara.io.file.option/to-permissions :added "3.0"}
(fact "transforms mode to permissions"

  (to-permissions "455")  
  => (contains [:owner-read
                :group-read
                :group-execute
                :others-read
                :others-execute] :in-any-order))

^{:refer hara.io.file.option/from-permissions :added "3.0"}
(fact "transforms permissions to mode"

  (from-permissions [:owner-read
                     :group-read
                     :group-execute
                     :others-read
                     :others-execute])
  => "455")

^{:refer hara.io.file.option/option :added "3.0"}
(fact "shows all options for file operations"

  (option)
  => (contains [:atomic-move :create-new
                :skip-siblings :read :continue
                :create :terminate :copy-attributes
                :append :truncate-existing :sync
                :follow-links :delete-on-close :write
                :dsync :replace-existing :sparse
                :nofollow-links :skip-subtree])

  (option :read)
  => java.nio.file.StandardOpenOption/READ)

(comment

  (hara.code/import))
