(ns hara.lib.aether.local-repo
  (:require [hara.io.file :as fs]
            [hara.object :as object])
  (:import (org.eclipse.aether.repository LocalRepository)))

(defonce +default-local-repo+
  (-> (System/getProperty "user.home")
      (fs/path ".m2" "repository")
      (str)))

(defn local-repo
  "creates a `LocalRepository` from a string
 
   (local-repo)
   => LocalRepository ;; #local \"<.m2/repository>\"
 
   ;; hooks into hara.object
   (-> (local-repo \"/tmp\")
       (object/to-data))
   => \"/tmp\""
  {:added "3.0"}
  ([]
   (local-repo +default-local-repo+))
  ([path]
   (LocalRepository. path)))

(object/string-like

 LocalRepository
 {:tag "local"
  :read (fn [repo] (str (.getBasedir repo)))
  :write local-repo})
