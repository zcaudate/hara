(ns hara.lib.aether.request
  (:require [hara.object :as object]
            [hara.lib.aether.artifact :as artifact]
            [hara.lib.aether.authentication]
            [hara.lib.aether.dependency]
            [hara.lib.aether.local-repo]
            [hara.lib.aether.remote-repo])
  (:import (org.eclipse.aether.collection CollectRequest)
           (org.eclipse.aether.deployment DeployRequest)
           (org.eclipse.aether.graph Dependency)
           (org.eclipse.aether.installation InstallRequest)
           (org.eclipse.aether.metadata DefaultMetadata)
           (org.eclipse.aether.repository RemoteRepository)
           (org.eclipse.aether.resolution ArtifactRequest
                                          DependencyRequest
                                          MetadataRequest
                                          VersionRangeRequest
                                          VersionRequest)))

(def artifact-map
  {:artifact
   {:type java.lang.Object
    :fn (fn [req artifact]
          (.setArtifact req (artifact/artifact-eclipse artifact)))}})

(def repositories-map
  {:repositories
   {:type java.util.List
    :fn (fn [req repositories]
          (->> repositories
               (mapv (fn [m]
                       (object/from-data m RemoteRepository)))
               (.setRepositories req)))}})

(def install-map
  {:artifacts
   {:type java.util.List
    :fn (fn [req artifacts]
          (->> artifacts
               (mapv (fn [x]
                       (artifact/artifact-eclipse x)))
               (.setArtifacts req)))}

   :metadata
   {:type java.util.List
    :fn (fn [req metadata]
          (->> metadata
               (mapv (fn [x]
                       (object/from-data metadata)))
               (.setMetadata req)))}})

(def collect-map
  {:root-artifact
   {:type java.lang.Object
    :fn (fn [req artifact]
          (.setRootArtifact req (artifact/artifact-eclipse artifact)))}

   :dependencies
   {:type java.util.List
    :fn (fn [req dependencies]
          (->> dependencies
               (mapv (fn [m]
                       (object/from-data m Dependency)))
               (.setDependencies req)))}
   :managed-dependencies
   {:type java.util.List
    :fn (fn [req dependencies]
          (->> dependencies
               (mapv (fn [m]
                       (object/from-data m Dependency)))
               (.setManagedDependencies req)))}})

(object/map-like

 ArtifactRequest
 {:tag "req.artifact"
  :read  :class
  :write {:methods
          (-> (object/write-setters ArtifactRequest)
              (merge artifact-map repositories-map))
          :empty (fn [] (ArtifactRequest.))}}

 CollectRequest
 {:tag "req.collect"
  :read :class
  :write {:methods (-> (object/write-setters CollectRequest)
                       (merge collect-map repositories-map))
          :empty (fn [] (CollectRequest.))}}

 DependencyRequest
 {:tag "req.dependency"
  :read  {:to-map
          (fn [req]
            (object/to-data (.getCollectRequest req)))}
  :write {:from-map
          (fn [m]
            (doto (DependencyRequest.)
              (.setCollectRequest (object/from-data m CollectRequest))))}}

 DeployRequest
 {:tag "req.deploy"
  :read :class
  :write {:methods (-> (object/write-setters DeployRequest)
                       (merge install-map))
          :empty (fn [] (DeployRequest.))}}

 MetadataRequest
 {:tag "req.metadata"
  :read :class
  :write {:methods :class
          :empty (fn [] (MetadataRequest.))}}
 
 InstallRequest
 {:tag "req.install"
  :read :class
  :write {:methods (-> (object/write-setters InstallRequest)
                       (merge install-map))
          :empty (fn [] (InstallRequest.))}}

 VersionRequest
 {:tag "req.version"
  :read :class
  :write {:methods (-> (object/write-setters VersionRequest)
                       (merge artifact-map repositories-map))
          :empty (fn [] (VersionRequest.))}}
 
 VersionRangeRequest
 {:tag "req.range"
  :read :class
  :write {:methods (-> (object/write-setters VersionRangeRequest)
                       (merge artifact-map repositories-map))
          :empty (fn [] (VersionRangeRequest.))}})

(defn artifact-request
  "creates an `ArtifactRequest` object from map
 
   (artifact-request
    {:artifact \"hara:hara:2.4.8\"
     :repositories [{:id \"clojars\"
                     :authentication {:username \"zcaudate\"
                                      :password \"hello\"}
                     :url \"https://clojars.org/repo/\"}]})
   ;;=> #req.artifact{:artifact \"hara:hara:jar:2.4.8\",
   ;;                 :repositories [{:id \"clojars\",
   ;;                                 :url \"https://clojars.org/repo/\"
   ;;                                 :authentication {:username \"zcaudate\", :password \"hello\"}}],
   ;;                 :request-context \"\"}
 "
  {:added "3.0"}
  [{:keys [artifact repositories] :as m}]
  (object/from-data m ArtifactRequest))

(defn collect-request
  "creates a `CollectRequest` object from map
 
   (collect-request
    {:root {:artifact \"hara:hara:2.4.8\"}
     :repositories [{:id \"clojars\"
                     :url \"https://clojars.org/repo/\"}]})
   ;;=> #req.collect{:root {:artifact \"hara:hara:jar:2.4.8\",
   ;;                       :exclusions [],
   ;;                       :optional false,
   ;;                       :scope \"\",
   ;;                       :optional? false}
   ;;                :repositories [{:id \"clojars\",
   ;;                                :url \"https://clojars.org/repo/\"}]}
 "
  {:added "3.0"}
  [{:keys [root repositories] :as m}]
  (object/from-data m CollectRequest))

(defn dependency-request
  "creates a `DependencyRequest` object from map
 
   (dependency-request
    {:root {:artifact \"hara:hara:2.4.8\"}
     :repositories [{:id \"clojars\"
                     :url \"https://clojars.org/repo/\"}]})
   ;;=> #req.dependency{:root {:artifact \"hara:hara:jar:2.4.8\",
   ;;                          :exclusions [],
   ;;                          :optional false,
   ;;                          :scope \"\",
   ;;                          :optional? false}
   ;;                   :repositories [{:id \"clojars\",
   ;;                                   :url \"https://clojars.org/repo/\"}]}
 "
  {:added "3.0"}
  [{:keys [root repositories] :as m}]
  (object/from-data m DependencyRequest))

(defn deploy-request
  "creates a `DeployRequest` object from map
 
   (deploy-request
    {:artifacts [{:group \"hara\"
                  :artifact \"hara.string\"
                  :version \"2.4.8\"
                  :extension \"jar\"
                  :file \"hara-string.jar\"}]
     :repository {:id \"clojars\"
                  :url \"https://clojars.org/repo/\"
                  :authentication {:username \"zcaudate\"
                                   :password \"hello\"}}})
   ;;=> #req.deploy{:artifacts [\"hara:hara.string:jar:2.4.8\"]
   ;;               :repository {:id \"clojars\",
   ;;                            :authentication {:username \"zcaudate\", :password \"hello\"}
   ;;                            :url \"https://clojars.org/repo/\"}}
 "
  {:added "3.0"}
  [{:keys [artifacts repository] :as m}]
  (object/from-data m DeployRequest))

(defn install-request
  "creates a `InstallRequest` object from map
 
   (install-request
    {:artifacts [{:group \"hara\"
                  :artifact \"hara.string\"
                  :version \"2.4.8\"
                  :extension \"jar\"
                  :file \"hara-string.jar\"}
                 {:group \"hara\"
                  :artifact \"hara.string\"
                  :version \"2.4.8\"
                  :extension \"pom\"
                  :file \"hara-string.pom\"}]})
   ;;=> #req.install{:artifacts [\"hara:hara.string:jar:2.4.8\"
   ;;                            \"hara:hara.string:pom:2.4.8\"]
   ;;                :metadata []}
 "
  {:added "3.0"}
  [{:keys [artifacts] :as m}]
  (object/from-data m InstallRequest))

(defn metadata-request
  "constructs a metadat request
 
   (metadata-request
    {:metadata   {:group \"hara\"
                 :artifact \"hara.string\"
                  :version \"2.4.8\"}
     :repository {:id \"clojars\"
                  :url \"https://clojars.org/repo/\"
                  :authentication {:username \"zcaudate\"
                                   :password \"hello\"}}})"
  {:added "3.0"}
  [{:keys [metadata repository] :as m}]
  (object/from-data m MetadataRequest))

(defn range-request
  "constructs a range request
 
   (range-request {:artifact {:group \"hara\"
                              :artifact \"hara.string\"
                             :version \"2.4.8\"}
                   :repositories [{:id \"clojars\"
                                    :url \"https://clojars.org/repo/\"
                                    :authentication {:username \"zcaudate\"
                                                     :password \"hello\"}}]})"
  {:added "3.0"}
  [{:keys [artifact repositories] :as m}]
  (object/from-data m VersionRangeRequest))

(defn version-request
  "constructs a version request
   
   (version-request {:artifact {:group \"hara\"
                                :artifact \"hara.string\"
                                :version \"2.4.8\"}
                     :repositories [{:id \"clojars\"
                                    :url \"https://clojars.org/repo/\"
                                     :authentication {:username \"zcaudate\"
                                                      :password \"hello\"}}]})"
  {:added "3.0"}
  [{:keys [artifact repositories] :as m}]
  (object/from-data m VersionRequest))
