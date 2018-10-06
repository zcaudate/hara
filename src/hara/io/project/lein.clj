(ns hara.io.project.lein
  (:require [hara.io.file :as fs]
            [hara.io.project.common :as common]))

(def ^:dynamic *project-file* "project.clj")

(defn project
  "returns the root project map
 
   (project)"
  {:added "3.0"}
  ([] (project *project-file*))
  ([project-file]
   (let [proj-form (-> project-file
                       slurp
                       read-string)
         root  (-> project-file fs/path fs/parent str)
         [_ full version] (take 3 proj-form)
         entry (common/artifact full)
         proj  (-> (apply hash-map (drop 3 proj-form))
                   (merge entry
                          {:version version
                           :root root})
                   (->> (merge common/*defaults*)))]
     proj)))

(defn project-name
  "returns the project name
 
   (project-name)
   => 'hara/base"
  {:added "3.0"}
  ([] (project-name *project-file*))
  ([project-file]
   (-> (fs/code project-file)
       first
       second)))
