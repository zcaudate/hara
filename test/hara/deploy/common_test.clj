(ns hara.deploy.common-test
  (:use hara.test)
  (:require [hara.deploy.common :refer :all]
            [hara.deploy.analyser :as analyser]))

^{:refer hara.deploy.common/-file-info :added "3.0"}
(fact "extendable function for `file-info`")

^{:refer hara.deploy.common/file-info :added "3.0"}
(fact "returns the exports and imports of a given file"
  
  (file-info "src/hara/deploy/common.clj")
  => '{:exports #{[:clj hara.deploy.common]
                  [:class hara.deploy.common.FileInfo]},
       :imports #{[:clj hara.io.file]}})

^{:refer hara.deploy.common/all-packages :added "3.0"}
(fact "lists all packages to be deployed"

  (-> (all-packages {:root "."})
      keys
      (sort))^:hidden
  ;;(hara.code hara.core hara.core.component ... hara.platform.mail hara.protocol)
  => sequential?)
