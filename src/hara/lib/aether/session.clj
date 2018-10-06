(ns hara.lib.aether.session
  (:require [hara.lib.aether.listener :as listener]
            [hara.lib.aether.local-repo :as local])
  (:import (org.apache.maven.repository.internal MavenRepositorySystemUtils)
           (org.eclipse.aether.repository LocalRepository)
           (org.eclipse.aether.util.graph.manager DependencyManagerUtils)
           (org.eclipse.aether.util.graph.transformer ConflictResolver)))

(defn session
  "creates a session from a system:
 
   (session (system/repository-system)
            {})
   => org.eclipse.aether.RepositorySystemSession"
  {:added "3.0"}
  [system {:keys [local-repo listeners] :as opts}]
  (let [session (doto (MavenRepositorySystemUtils/newSession)
                  (.setConfigProperty ConflictResolver/CONFIG_PROP_VERBOSE true)
                  (.setConfigProperty DependencyManagerUtils/CONFIG_PROP_VERBOSE true)
                  (.setRepositoryListener (or (:repository listeners)
                                              listener/+default-repository-listener+))
                  (.setTransferListener   (or (:transfer listeners)
                                              listener/blank-transfer-listener)))
        manager (.newLocalRepositoryManager system
                                            session
                                            (-> (or local-repo local/+default-local-repo+)
                                                (local/local-repo)))
        _ (.setLocalRepositoryManager session manager)]
    session))
