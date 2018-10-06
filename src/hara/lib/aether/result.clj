(ns hara.lib.aether.result
  (:require [hara.string :as string]
            [hara.string.base.ansi :as ansi]
            [hara.print :as print]
            [hara.core.base.version :as version]
            [hara.module.artifact :as jvm.artifact]
            [hara.object :as object]
            [hara.lib.aether.artifact])
  (:import (org.eclipse.aether.collection CollectResult)
           (org.eclipse.aether.deployment DeployResult)
           (org.eclipse.aether.graph DependencyNode)
           (org.eclipse.aether.installation InstallResult)
           (org.eclipse.aether.resolution ArtifactDescriptorResult ArtifactResult DependencyResult MetadataResult VersionRangeResult VersionResult)))

(defn clojure-core?
  "checks if artifact represents clojure.core
 
   (clojure-core? '[org.clojure/clojure \"1.2.0\"])
   => true"
  {:added "3.0"}
  [node]
  (let [{:keys [group artifact]} (jvm.artifact/artifact node)]
    (and (= group "org.clojure")
         (= artifact "clojure"))))

(defn prioritise
  "gives the higher version library more priority
 
   (prioritise '[[a \"1.0\"]
                 [a \"1.2\"]
                 [a \"1.1\"]]
               :coord)
   => '[[a/a \"1.2\"]]"
  {:added "3.0"}
  ([results]
   (prioritise results :default))
  ([results type]
   (->> (reduce (fn [out {:keys [group artifact version] :as result}]
                  (if-let [current (get-in out [group artifact])]
                    (if (version/newer? version (:version current))
                      (assoc-in out [group artifact] result)
                      out)
                    (assoc-in out [group artifact] result)))
                {}
                (map jvm.artifact/artifact results))
        (vals)
        (mapcat vals)
        (remove clojure-core?)
        (mapv #(jvm.artifact/artifact type %)))))

(defn print-tree
  "prints a tree structure
 
   (-> (print-tree '[[a \"1.1\"]
                     [[b \"1.1\"]
                     [[c \"1.1\"]
                       [d \"1.1\"]]]])
       (with-out-str))"
  {:added "3.0"}
  [tree]
  (print/print-tree tree "" jvm.artifact/coord?))

(defn dependency-graph
  "creates a dependency graph for the results"
  {:added "3.0"}
  ([node]
   (dependency-graph node
                     :default))
  ([node type]
   (dependency-graph node
                     (or type :default)
                     (comp not clojure-core?)))
  ([^DependencyNode node type pred]
   (let [artifact (.getArtifact node)
         artifact (if artifact
                    (jvm.artifact/artifact type artifact))
         children (->> (.getChildren node)
                       (filter (fn [child]
                                 (-> child (.getArtifact) pred))))]
     (apply vector artifact (mapv #(dependency-graph % type pred) children)))))

(defn flatten-tree
  "converts a tree structure into a vector
 
   (flatten-tree '[[a \"1.1\"]
                   [[b \"1.1\"]
                    [[c \"1.1\"]
                     [d \"1.1\"]]]])
   => '[[a \"1.1\"] [b \"1.1\"] [c \"1.1\"] [d \"1.1\"]]"
  {:added "3.0"}
  [x]
  (let [arr? (fn [x] (and (sequential? x)
                          (not (jvm.artifact/coord? x))))]
    (filter (complement arr?)
            (rest (tree-seq arr? seq x)))))

(defmulti summary
  "creates a summary for the different types of results"
  {:added "3.0"}
  (fn [result opts] (type result)))

(defmethod summary DependencyResult
  [result opts]
  (->> (dependency-graph (.getRoot result) (:type opts))
       (filterv identity)))

(defmethod summary CollectResult
  [result opts]
  (->> (dependency-graph (.getRoot result) (:type opts))
       (filterv identity)))

(defmethod summary InstallResult
  [result opts]
  (->> (.getArtifacts result)
       (mapv #(jvm.artifact/artifact (:type opts) %))))

(defmethod summary DeployResult
  [result opts]
  (->> (.getArtifacts result)
       (mapv #(jvm.artifact/artifact (:type opts) %))))

(defmethod summary VersionResult
  [result opts]
  [(assoc (into {} (jvm.artifact/artifact (.getArtifact (.getRequest result))))
          :version (.getVersion result))])

(defmethod summary VersionRangeResult
  [result opts]
  [{:highest  (.getHighestVersion result)
    :lowest   (.getLowestVersion result)
    :versions (vec (.getVersions result))}])

(defmethod summary java.util.List
  [result opts]
  (mapcat (fn [r] (summary r opts)) result))

(defn return
  "returns a summary of install and deploy results"
  {:added "3.0"}
  [result events {:keys [return print type] :as  opts}]
  (let [output (->> (summary result opts)
                    (filterv identity))]
    (clojure.core/print "\n")
    (when (:timing print)
      (print/print-subtitle
       (if-not (empty? events)
         (format "TIME (%.2fs, %d events)"
                 (/ (- (:time (last events))
                       (:time (first events)))
                    1000.0)
                 (count events))
         "(DONE)")))
    output))

(defn return-deps
  "returns a summary of resolve and collect results"
  {:added "3.0"}
  [result events {:keys [return print type] :as  opts}]
  (let [output (->> (summary result opts)
                    (filterv identity))]
    (clojure.core/print "\n")
    (if (:timing print)
      (print/print-subtitle
       (if-not (empty? events)
         (format "TIME (%.2fs, %d events)"
                 (/ (- (:time (last events))
                       (:time (first events)))
                    1000.0)
                 (count events))
         "(DONE)")))

    (when (:hierarchy print)
      (println "")
      (print-tree output))

    (case return
      :hierarchy output
      :resolved (prioritise (flatten-tree output) type))))
