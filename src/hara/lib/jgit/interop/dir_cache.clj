(ns hara.lib.jgit.interop.dir-cache
  (:require [hara.object :as object])
  (:import (org.eclipse.jgit.dircache DirCache DirCacheEntry)))

(defn process-entry
  "testing dir-cache and dir-cache-entry
 
   (def path (str \"/tmp/gita/\" (java.util.UUID/randomUUID)))
   (def tempdir (io/file path))
 
   (git-status-call tempdir)
   (spit (str path \"/hello.txt\") \"hello\")
   (-> (git-add-call tempdir)
       (object/to-data))
   => (contains-in {\"hello.txt\" set?}) ;;#{:smudged :merged}
 
   (-> (git-add-call tempdir)
       (.getEntry 0)
       (object/to-data))
   => (contains {:file-mode \"100644\",
                 :stage 0,
                 :object-id string?
                 :last-modified number?
                 :length number?,
                 :path-string \"hello.txt\"
                 :creation-time 0})"
  {:added "3.0"}
  [entry]
  [(:path-string entry)
   (reduce-kv (fn [s k v]
                (if (= true v) (conj s (-> k name (.replace "?" "") keyword)) s))
              #{} entry)])

(object/map-like
 DirCache
 {:tag "dir"
  :read {:to-map
         (fn [^DirCache dir-cache]
           (let [count (.getEntryCount dir-cache)]
             (->> (map (fn [^Long i] (-> (.getEntry dir-cache i) object/to-data process-entry))
                       (range count))
                  (into {}))))}}

 DirCacheEntry
 {:tag "e" :exclude [:raw-mode :raw-path]})
