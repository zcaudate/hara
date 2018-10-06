(ns hara.lib.aether.system
  (:import (org.apache.maven.repository.internal MavenRepositorySystemUtils)
           (org.eclipse.aether RepositorySystem)
           (org.eclipse.aether.connector.basic BasicRepositoryConnectorFactory)
           (org.eclipse.aether.spi.connector RepositoryConnectorFactory)
           (org.eclipse.aether.spi.connector.transport TransporterFactory)
           (org.eclipse.aether.transport.file FileTransporterFactory)
           (org.eclipse.aether.transport.http HttpTransporterFactory)
           (org.eclipse.aether.transport.wagon WagonProvider WagonTransporterFactory)))

(defn repository-system
  "creates a repository system for interfacting with maven
 
   (repository-system)
   => org.eclipse.aether.RepositorySystem"
  {:added "3.0"}
  []
  (-> (doto (MavenRepositorySystemUtils/newServiceLocator)
        (.addService RepositoryConnectorFactory BasicRepositoryConnectorFactory)
        (.addService TransporterFactory FileTransporterFactory)
        (.addService TransporterFactory HttpTransporterFactory))
      (.getService RepositorySystem)))
