(ns hara.lib.aether.dependency
  (:require [hara.protocol.loader :as protocol.loader]
            [hara.function :refer [definvoke]]
            [hara.module.artifact :as jvm.artifact]
            [hara.lib.aether.artifact :as artifact]
            [hara.object :as object])
  (:import (org.eclipse.aether.graph DefaultDependencyNode Dependency DependencyNode Exclusion)))

(definvoke rep-exclusion
  "creates a rep from an exclusion
   
   (str (rep-exclusion (artifact-exclusion \"hara:hara\")))
   => \"hara:hara:jar:\""
  {:added "3.0"}
  [:method {:multi protocol.loader/-rep
            :val   Exclusion}]
  ([exclusion]
   (jvm.artifact/->Rep (.getGroupId exclusion)
                       (.getArtifactId exclusion)
                       (.getExtension exclusion)
                       (.getClassifier exclusion)
                       nil
                       nil
                       nil
                       nil
                       nil)))

(definvoke artifact-exclusion
  "creates an artifact exclusion
 
   (artifact-exclusion \"hara:hara:jar:2.8.4\")
   => Exclusion"
  {:added "3.0"}
  [:method {:multi protocol.loader/-artifact
            :val   :eclipse.exclusion}]
  ([x]
   (artifact-exclusion nil x))
  ([_ x]
   (let [{:keys [group artifact classifier extension]}
         (jvm.artifact/rep x)]
     (Exclusion. group artifact classifier extension))))

(object/map-like
 Exclusion
 {:tag "exclusion"
  :read {:to-string    (fn [artifact]
                         (jvm.artifact/artifact :string artifact))
         :to-map       (fn [artifact]
                         (into {} (jvm.artifact/rep artifact)))}
  :write {:from-map    (fn [m]
                         (artifact-exclusion m))
          :from-string (fn [m]
                         (artifact-exclusion m))}})

(def artifact-map
  {:artifact
   {:type java.lang.Object
    :fn (fn [req artifact]
          (.setArtifact req (artifact/artifact-eclipse artifact)))}
   :exclusions java.util.List
   :fn (fn [req exclusions]
         (.setExclusions req (mapv artifact-exclusion exclusions)))})

(object/map-like

 Dependency
 {:tag "dep"
  :read :class
  :write {:construct {:fn (fn [artifact scope optional exclusions]
                            (Dependency. (artifact/artifact-eclipse artifact)
                                         (or scope "")
                                         (or optional false)
                                         (mapv artifact-exclusion
                                               (or exclusions []))))
                      :params [:artifact :scope :optional :exclusions]}
          :methods
          (-> (object/write-setters Dependency)
              (merge artifact-map))}}
 
 DependencyNode
 {:tag   "dep.node"
  :read  :class
  :write {:construct {:fn (fn [artifact]
                            (DefaultDependencyNode.
                             (artifact/artifact-eclipse artifact)))
                      :params [:artifact]}
          :methods
          (-> (object/write-setters DefaultDependencyNode)
              (merge artifact-map))}}

 DefaultDependencyNode
 {:tag   "dep.node"
  :read  :class
  :write {:construct {:fn (fn [artifact]
                            (DefaultDependencyNode.
                             (artifact/artifact-eclipse artifact)))
                      :params [:artifact]}
          :methods
          (-> (object/write-setters DefaultDependencyNode)
              (merge artifact-map))}})
