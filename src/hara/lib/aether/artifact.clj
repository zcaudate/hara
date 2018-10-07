(ns hara.lib.aether.artifact
  (:require [hara.module.artifact :as artifact]
            [hara.function :refer [definvoke]]
            [hara.protocol.classloader :as protocol.classloader]
            [hara.object :as object])
  (:import (org.eclipse.aether.artifact Artifact DefaultArtifact)
           (org.eclipse.aether.metadata Metadata Metadata$Nature DefaultMetadata)
           (org.eclipse.aether.graph Exclusion)))

(definvoke rep-eclipse
  "creates a rep from eclipse artifact
 
   (str (rep-eclipse (object/from-data \"hara:hara:2.8.4\" DefaultArtifact)))
   => \"hara:hara:jar:2.8.4\""
  {:added "3.0"}
  [:method {:multi protocol.classloader/-rep
            :val   Artifact}]
  ([artifact]
   (artifact/->Rep (.getGroupId artifact)
                   (.getArtifactId artifact)
                   (.getExtension artifact)
                   (.getClassifier artifact)
                   (.getVersion artifact)
                   (.getProperties artifact)
                   (str (.getFile artifact))
                   nil
                   nil)))

(definvoke artifact-eclipse
  "creates an eclipse artifact
 
   (artifact-eclipse \"hara:hara:jar:2.8.4\")
   => DefaultArtifact"
  {:added "3.0"}
  [:method {:multi protocol.classloader/-artifact
            :val   :eclipse}]
  ([x]
   (artifact-eclipse nil x))
  ([_ x]
   (let [{:keys [group artifact classifier
                 extension version
                 properties file]}
         (artifact/rep x)]
     (DefaultArtifact. group artifact classifier
                       extension version
                       properties (if file (java.io.File. (str file)))))))

(object/map-like
 DefaultArtifact
 {:tag "artifact"
  :read {:to-string    (fn [artifact]
                         (artifact/artifact-string artifact))
         :to-map       (fn [artifact]
                         (into {} (artifact/rep artifact)))}
  :write {:from-map    (fn [m]
                         (artifact-eclipse m))
          :from-string (fn [m]
                         (artifact-eclipse m))}})

(defonce +metadata-type+ "maven-metadata.xml")

(defonce +metadata-nature+ Metadata$Nature/RELEASE)

(definvoke rep-eclipse-metadata
  "creates a rep from an eclipse metadata instance
 
   (str (rep-eclipse-metadata (object/from-data \"hara:hara:2.8.4\" DefaultMetadata)))
   => \"hara:hara:2.8.4\""
  {:added "3.0"}
  [:method {:multi protocol.classloader/-rep
            :val   Metadata}]
  ([metadata]
   (artifact/->Rep (.getGroupId metadata)
                   (.getArtifactId metadata)
                   nil
                   nil
                   (.getVersion metadata)
                   (.getProperties metadata)
                   (str (.getFile metadata))
                   nil
                   nil)))

(definvoke artifact-eclipse-metadata
  "creates an eclipse metadata instance
 
   (artifact-eclipse-metadata \"hara:hara:jar:2.8.4\")
   => DefaultMetadata"
  {:added "3.0"}
  [:method {:multi protocol.classloader/-artifact
            :val   :eclipse.metadata}]
  ([x]
   (artifact-eclipse-metadata nil x))
  ([_ x]
   (let [{:keys [group artifact classifier
                 extension version
                 properties file]}
         (artifact/rep x)]
     (DefaultMetadata. group artifact version +metadata-type+
                       +metadata-nature+ properties
                       (if file (java.io.File. (str file)))))))

(object/map-like
 DefaultMetadata
 {:tag "metadata"
  :read {:to-map       (fn [artifact]
                         (into {} (artifact/rep artifact)))}
  :write {:from-map    (fn [m]
                         (artifact-eclipse-metadata m))
          :from-string (fn [m]
                         (artifact-eclipse-metadata m))}})

(object/map-like
 Metadata
 {:tag "metadata"
  :read {:to-map       (fn [artifact]
                         (into {} (artifact/rep artifact)))}
  :write {:from-map    (fn [m]
                         (artifact-eclipse-metadata m))
          :from-string (fn [m]
                         (artifact-eclipse-metadata m))}})
