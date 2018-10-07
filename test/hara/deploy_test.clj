(ns hara.deploy-test
  (:use hara.test)
  (:require [hara.deploy :refer :all]
            [hara.io.project :as project]))

^{:refer hara.deploy/clean :added "3.0"}
(comment "cleans the interim directory of packages"

  (clean :all))

^{:refer hara.deploy/package :added "3.0"}
(comment "packages files in the interim directory"

  (package '[hara]))

^{:refer hara.deploy/install :added "3.0"}
(comment "installs packages to the local `.m2` repository"

  (install '[hara]))

^{:refer hara.deploy/install-secure :added "3.0"}
(comment "installs signed packages to the local `.m2` repository")

^{:refer hara.deploy/deploy :added "3.0"}
(comment "deploys packages to a maven repository"

  (deploy '[spirit]
          {:repository {:id "hara"
                        :url "https://maven.hara.io"
                        :authentication {:username "hara"
                                         :password "hara"}}}))


(comment
  (hara.deploy/deploy '[hara]
                      {:repository {:id "hara"
                                    :url "https://maven.hara.io"
                                    :authentication {:username "hara"
                                                     :password "hara"}}})
  
  (deploy-current)
  (hara.code/import)
  (deploy (project/project "../hara/project.clj"))
  (deploy (project/project)))

(comment

  (install-secure '[spirit])

  (package '[hara.io.qrcode])
  
  (get (hara.deploy.analyser/create-plan (project/project))
       'hara.io.qrcode)
  
  (install '[spirit])
  
  (install 'spirit.core)

  (deploy '[spirit.core]
          {:repository {:id "hara"
                        :url "https://maven.hara.io"
                        :authentication {:username "hara"
                                         :password "hara"}}})
  
  (deploy 'hara.core {:repository *hara-repo*})
  
  (use 'hara.deploy) (deploy  {:repository *hara-repo*}))
