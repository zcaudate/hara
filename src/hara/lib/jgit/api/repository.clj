(ns hara.lib.jgit.api.repository
  (:require [clojure.java.io :as io]
            [hara.lib.jgit.interop :deps true])
  (:import (java.io File)
           (java.util Date)
           (org.eclipse.jgit.api Git)
           (org.eclipse.jgit.lib Constants Repository)
           (org.eclipse.jgit.revwalk RevCommit RevWalk)
           (org.eclipse.jgit.storage.file FileRepositoryBuilder)
           (org.eclipse.jgit.treewalk AbstractTreeIterator CanonicalTreeParser TreeWalk)
           (org.eclipse.jgit.treewalk.filter PathFilter)))

(def ^:dynamic *current-directory* nil)

(defn as-directory
  "converts a string to a `java.io.File` object
 
   (as-directory \"project.clj\") => false
 
   (as-directory \".\") ;;=> #// \".\"
   "
  {:added "1.2" :tag java.io.File}
  [path]
  (if-let [^File curr-dir (io/as-file path)]
    (and (.isDirectory curr-dir)
         (.getCanonicalFile curr-dir))))

(defn root-directory
  "finds the root `.git` repo of a given path
 
   (root-directory \"src/hara/tool/git/api/\")
   => (as-directory \".git\")
 
   (root-directory \".\")
   => (as-directory \".git\")"
  {:added "1.2" :tag java.io.File}
  [path]
  (if-let [curr-dir (as-directory path)]
    (if-let [git-dir (as-directory (str path "/.git"))]
      git-dir
      (recur (.getParent curr-dir)))))

(defn repository
  "creates a repo object for `path` or the current directory
 
   (repository)
   ;;=> #repository \"/Users/chris/Development/zcaudate/lucidity/.git\"
 "
  {:added "1.2" :tag org.eclipse.jgit.lib.Repository}
  ([] (repository (or *current-directory*
                      (System/getProperty "user.dir"))))
  ([path]
   (if-let [git-dir (root-directory path)]
     (let [repo (FileRepositoryBuilder/create git-dir)
           config (doto (.getConfig repo)
                    (.setString "remote", "origin", "fetch", "+refs/*:refs/*")
                    (.save))]
       repo)
     (throw (Exception. (str "The Git repository at '"
                             path "' could not be located."))))))

(defn repository?
  "checks if the object is a repository
 
   (repository? (repository))
   => true"
  {:added "3.0"}
  [obj]
  (instance? Repository obj))

(defn list-commits
  "lists current commits for a particular repository
   (-> (repository)
       (list-commits \"master\")
       (first))
   ;; => {:time #inst \"2018-05-27T03:54:52.000-00:00\",
   ;;     :id \"c8fa557ce252dffdfbdd5c1884cf87d41c0f222f\"}
 "
  {:added "3.0"}
  ([^Repository repo]
   (list-commits repo nil))
  ([^Repository repo ^String branch]
   (let [log (-> (Git. repo) (.log))
         log (if branch
               (.add log (.resolve repo branch))
               log)]
     (->> log
          (.call) (.iterator) (iterator-seq)
          (map (fn [^RevCommit commit] (hash-map :id (.getName commit)
                                                 :time (Date. (* 1000 (.getCommitTime commit))))))))))

(defn time->id
  "finds the id associated with a commit at a particular time
 
   (time->id (repository)
             (java.util.Date.))
   ;; \"77831a03e8c6c9c0aada87412ec0efb5dadcc7bd\"
   => string?"
  {:added "3.0"}
  ([repo t]
   (time->id repo nil t))
  ([repo branch ^Date t]
   (loop [[x & [y & _ :as more]] (reverse (list-commits repo branch))]
     (cond (nil? x)
           nil

           (nil? y)
           (if (.after t (:time x))
             (:id x))

           (and (or (.after  t (:time x))
                    (= t (:time x)))
                (.before t (:time y)))
           (:id x)

           :else (recur more)))))

(defn parse-head-string
  "helper function for `resolve-id`"
  {:added "3.0"}
  [x]
  (let [entry (subs x 4)] ;; 4 HEAD
    (if-let [carets (re-find #"^\^+$" entry)]
      (count carets)
      (if-let [number (re-find #"^[\^~](\d+)$" entry)]
        (Long/parseLong (second number))
        (throw (Exception. "Not yet supported"))))))

(defn resolve-id
  "resolves to an id given a date or a string
 
   (resolve-id (repository) \"master\" \"77831a\")
   ;; #id \"77831a03e8c6c9c0aada87412ec0efb5dadcc7bd\"
   => org.eclipse.jgit.lib.ObjectId"
  {:added "3.0"}
  [^Repository repo branch x]
  (cond (instance? Date x)
        (recur repo branch (time->id repo branch x))

        (instance? Long x)
        (recur repo branch (time->id repo branch (Date. ^Long x)))

        (string? x)
        (cond (= x Constants/HEAD)
              (if-let [id (:id (first (list-commits repo branch)))]
                (.resolve repo id))

              (.startsWith ^String x Constants/HEAD)
              (if-let [id (->> (list-commits repo branch)
                               (drop (parse-head-string x))
                               first
                               :id)]
                (.resolve repo id))

              :else (.resolve repo x))))

(defn  tree-walk
  "initialises the `TreeWalk` object for exploring commits
 
   (tree-walk (repository))
   => org.eclipse.jgit.treewalk.TreeWalk"
  {:added "1.2" :tag org.eclipse.jgit.treewalk.TreeWalk}
  ([repo] (tree-walk repo nil))
  ([^Repository repo opts]
   (let [{:keys [branch commit]} (merge {:branch Constants/MASTER :commit Constants/HEAD}
                                        opts)
         rwalk    (RevWalk. repo)
         cid      (resolve-id repo branch commit)]
     (if cid
       (let [rcommit  (.parseCommit rwalk cid)
             rtree    (.getTree rcommit)]
         (doto (TreeWalk. repo)
           (.addTree rtree)
           (.setRecursive true)))))))

(defn tree-parser
  "creates a `TreeInterator` for diff operations
 
   (tree-parser (repository) {:commit \"77831a\"
                              :branch \"master\"})
   => org.eclipse.jgit.treewalk.CanonicalTreeParser"
  {:added "1.2" :tag org.eclipse.jgit.treewalk.AbstractTreeIterator}
  ([repo] (tree-parser repo nil))
  ([^Repository repo opts]
   (let [{:keys [branch commit]} (merge {:branch Constants/MASTER :commit Constants/HEAD}
                                        opts)
         rwalk    (RevWalk. repo)
         cid      (resolve-id repo branch commit)]
     (if cid
       (let [reader   (.newObjectReader repo)
             rcommit  (.parseCommit rwalk cid)
             rtree    (.getTree rcommit)]
         (doto (CanonicalTreeParser.)
           (.reset reader (.getId rtree))))))))

(defn list-files
  "list files within a given branch and commit
 
   (list-files (repository) {:commit \"77831a\"
                             :branch \"master\"})
   ;;=> (\".gitignore\" \".travis.yml\" ....)
   => vector?"
  {:added "3.0"}
  ([repo] (list-files repo nil))
  ([^Repository repo opts]
   (if-let [walk (tree-walk repo opts)]
     (loop [walk walk
            out []]
       (if (.next walk)
         (recur walk (conj out (.getPathString walk)))
         out)))))

(defn raw
  "gives an inputstream for evalution
 
   (-> (raw (repository) {:path \"project.clj\"})
       (slurp)
       (read-string)
       (second))
   => 'hara/base"
  {:added "3.0"}
  [^Repository repo opts]
  (when-let [walk (tree-walk repo opts)]
    (.setFilter walk (PathFilter/create (:path opts)))
    (when (.next walk)
      (->> (.getObjectId walk 0)
           (.open repo)
           (.openStream)))))

(defn blob
  "gets a blob according to id
 
   (->> (ObjectId/fromString \"80973f8e246b58f84a94ee9c1b91336ad117924e\")
        (blob (repository))
        (slurp)
        (read-string)
        (second))
   => 'hara/hara"
  {:added "3.0"}
  [^Repository repo id]
  (-> (.open repo
             id Constants/OBJ_BLOB)
      (.openStream)))

(comment
  (def tw (doto (tree-walk (repository))
            (.setFilter (PathFilter/create "project.clj"))))

  (.next tw)
  (.getObjectId tw 0);#id "80973f8e246b58f84a94ee9c1b91336ad117924e"
  (.getNameString tw)
  (.getNameString tw)
  (.getPathString tw)
  (slurp (.openStream (.open (repository) (.getObjectId tw 0))))

  (when-let [walk]
    (.setFilter walk (PathFilter/create (:path opts)))
    (when (.next walk)
      (->> (.getObjectId walk 0)
           (.open repo)
           (.openStream)))))
