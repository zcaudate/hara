(ns hara.lib.aether.request-test
  (:use hara.test)
  (:require [hara.lib.aether.request :refer :all]
            [hara.object :as object])
  (:import (org.eclipse.aether.graph Dependency
                                     DependencyNode
                                     DefaultDependencyNode)
           (org.eclipse.aether.repository RemoteRepository
                                          RemoteRepository$Builder
                                          RepositoryPolicy)
           (org.eclipse.aether.collection CollectRequest)
           (org.eclipse.aether.deployment DeployRequest)
           (org.eclipse.aether.installation InstallRequest)
           (org.eclipse.aether.resolution ArtifactRequest
                                          DependencyRequest)))

^{:added "3.0"}
(fact "creates a `Dependency` object from map"

  (object/from-data {:artifact '[hara/hara "2.4.8"]}
                    Dependency)
  ;;=> #dep{:artifact "hara:hara:jar:2.4.8",
  ;;        :exclusions [],
  ;;        :optional false,
  ;;        :scope "",
  ;;        :optional? false}
)

^{:added "3.0"}
(fact "creates a `DependencyNode` object from map"

  (object/from-data {:artifact '[hara/hara "2.4.8"]}
                    DependencyNode)
  ;;=> #dep.node {:children [],
  ;;              :relocations [],
  ;;              :repositories [],
  ;;              :managed-bits 0,
  ;;              :artifact "hara:hara:jar:2.4.8",
  ;;              :aliases [],
  ;;              :request-context "",
  ;;              :data {}}
)

^{:refer hara.lib.aether.request/artifact-request :added "3.0"}
(comment "creates an `ArtifactRequest` object from map"

  (artifact-request
   {:artifact "hara:hara:2.4.8"
    :repositories [{:id "clojars"
                    :authentication {:username "zcaudate"
                                     :password "hello"}
                    :url "https://clojars.org/repo/"}]})
  ;;=> #req.artifact{:artifact "hara:hara:jar:2.4.8",
  ;;                 :repositories [{:id "clojars",
  ;;                                 :url "https://clojars.org/repo/"
  ;;                                 :authentication {:username "zcaudate", :password "hello"}}],
  ;;                 :request-context ""}
)

^{:refer hara.lib.aether.request/collect-request :added "3.0"}
(fact "creates a `CollectRequest` object from map"

  (collect-request
   {:root {:artifact "hara:hara:2.4.8"}
    :repositories [{:id "clojars"
                    :url "https://clojars.org/repo/"}]})
  ;;=> #req.collect{:root {:artifact "hara:hara:jar:2.4.8",
  ;;                       :exclusions [],
  ;;                       :optional false,
  ;;                       :scope "",
  ;;                       :optional? false}
  ;;                :repositories [{:id "clojars",
  ;;                                :url "https://clojars.org/repo/"}]}
)

^{:refer hara.lib.aether.request/dependency-request :added "3.0"}
(fact "creates a `DependencyRequest` object from map"

  (dependency-request
   {:root {:artifact "hara:hara:2.4.8"}
    :repositories [{:id "clojars"
                    :url "https://clojars.org/repo/"}]})
  ;;=> #req.dependency{:root {:artifact "hara:hara:jar:2.4.8",
  ;;                          :exclusions [],
  ;;                          :optional false,
  ;;                          :scope "",
  ;;                          :optional? false}
  ;;                   :repositories [{:id "clojars",
  ;;                                   :url "https://clojars.org/repo/"}]}
)

^{:refer hara.lib.aether.request/deploy-request :added "3.0"}
(fact "creates a `DeployRequest` object from map"

  (deploy-request
   {:artifacts [{:group "hara"
                 :artifact "hara.string"
                 :version "2.4.8"
                 :extension "jar"
                 :file "hara-string.jar"}]
    :repository {:id "clojars"
                 :url "https://clojars.org/repo/"
                 :authentication {:username "zcaudate"
                                  :password "hello"}}})
  ;;=> #req.deploy{:artifacts ["hara:hara.string:jar:2.4.8"]
  ;;               :repository {:id "clojars",
  ;;                            :authentication {:username "zcaudate", :password "hello"}
  ;;                            :url "https://clojars.org/repo/"}}
)

^{:refer hara.lib.aether.request/install-request :added "3.0"}
(fact "creates a `InstallRequest` object from map"

  (install-request
   {:artifacts [{:group "hara"
                 :artifact "hara.string"
                 :version "2.4.8"
                 :extension "jar"
                 :file "hara-string.jar"}
                {:group "hara"
                 :artifact "hara.string"
                 :version "2.4.8"
                 :extension "pom"
                 :file "hara-string.pom"}]})
  ;;=> #req.install{:artifacts ["hara:hara.string:jar:2.4.8"
  ;;                            "hara:hara.string:pom:2.4.8"]
  ;;                :metadata []}
)


^{:refer hara.lib.aether.request/metadata-request :added "3.0"}
(fact "constructs a metadat request"

  (metadata-request
   {:metadata   {:group "hara"
                 :artifact "hara.string"
                 :version "2.4.8"}
    :repository {:id "clojars"
                 :url "https://clojars.org/repo/"
                 :authentication {:username "zcaudate"
                                  :password "hello"}}}))

^{:refer hara.lib.aether.request/range-request :added "3.0"}
(fact "constructs a range request"

  (range-request {:artifact {:group "hara"
                             :artifact "hara.string"
                             :version "2.4.8"}
                  :repositories [{:id "clojars"
                                   :url "https://clojars.org/repo/"
                                   :authentication {:username "zcaudate"
                                                    :password "hello"}}]}))

^{:refer hara.lib.aether.request/version-request :added "3.0"}
(fact "constructs a version request"
  
  (version-request {:artifact {:group "hara"
                               :artifact "hara.string"
                               :version "2.4.8"}
                    :repositories [{:id "clojars"
                                    :url "https://clojars.org/repo/"
                                    :authentication {:username "zcaudate"
                                                     :password "hello"}}]}))
