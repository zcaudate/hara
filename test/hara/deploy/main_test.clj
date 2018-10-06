(ns hara.deploy.main-test
  (:use hara.test)
  (:require [hara.deploy.main :refer :all]
            [hara.deploy.analyser :as analyser]
            [hara.io.project :as project]
            [hara.lib.aether :as aether]))

^{:refer hara.deploy.main/package :added "3.0"}
(fact "given an entry, create a jar/pom package for deployment"

  (package 'hara.function.task
           (-> (analyser/create-plan (project/project))
               (get 'hara.function.task))
           (project/project))
  => (throws)
  )

^{:refer hara.deploy.main/infer :added "3.0"}
(fact "given an entry, infer the jar/pom package for deployment"

  (infer 'hara.function.task
           (-> (analyser/create-plan (project/project))
               (get 'hara.function.task))
           (project/project))
  => (throws)
  #_(contains {:package 'hara.function.task
               :jar string?
               :pom string?}))

^{:refer hara.deploy.main/clean :added "3.0"}
(fact "cleans the interim directory"

  (clean 'hara.function.task
         {}
         {}
         (project/project)))

^{:refer hara.deploy.main/install :added "3.0"}
(comment "installs a package to the local `.m2` repository"

  (install 'hara.function.task
           {}
           {}
           (assoc (project/project)
                  :plan (analyser/create-plan (project/project))
                  :aether (aether/aether))))

^{:refer hara.deploy.main/sign-file :added "1.2"}
(comment "signs a file with gpg"

  (sign-file {:file "project.clj" :extension "clj"}
             {:signing (-> hara.security.pgp.local/LEIN-PROFILE
                           slurp
                           read-string
                           :user
                           :signing
                           :gpg-key)}))

^{:refer hara.deploy.main/add-authentication :added "1.2"}
(comment "decrypts credentials.gpg and inserts the right authentication"

  (add-authentication {:id "clojars"}
                      {}))

^{:refer hara.deploy.main/create-digest :added "1.2"}
(comment "creates a digest given a file and a digest type"

  (create-digest "MD5"
                 "md5"
                 {:file "project.clj"
                  :extension "clj"})
  => {:file "project.clj.md5",
      :extension "clj.md5"})

^{:refer hara.deploy.main/add-digest :added "1.2"}
(comment "adds MD5 and SHA1 digests to all artifacts"

  (add-digest [{:file "project.clj",
                :extension "clj"}])
  => [{:file "project.clj.md5", :extension "clj.md5"}
      {:file "project.clj", :extension "clj"}])

^{:refer hara.deploy.main/install-secure :added "3.0"}
(comment "installs a package to the local `.m2` repository with signing"

  (install-secure 'hara.function.task
                  {}
                  {}
                  (assoc (project/project)
                         :plan (analyser/create-plan (project/project))
                         :aether (aether/aether))))

^{:refer hara.deploy.main/deploy :added "3.0"}
(comment "deploys a package "

  (deploy 'hara.function.task
          {:repository {:id "hara"
                        :url "https://maven.hara.io"
                        :authentication {:username "hara"
                                         :password "hara"}}}
          {}
          (assoc (project/project)
                 :plan (analyser/create-plan (project/project))
                 :aether (aether/aether))))
