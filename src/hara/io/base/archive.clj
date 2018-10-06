(ns hara.io.base.archive
  (:require [hara.core.base.environment :as env]
            [hara.io.file :as fs]
            [hara.protocol.archive :as protocol.archive])
  (:import (java.net URI)
           (java.nio.file FileSystem FileSystems Files Paths))
  (:refer-clojure :exclude [list remove]))
  
(def supported #{:zip :jar})


(env/init [[:java :newer  {:major 1 :minor 9}]]
  (:import (jdk.nio.zipfs ZipFileSystem)))

(env/init [[:java :not-newer  {:major 1 :minor 9}]]
  (:import (com.sun.nio.zipfs ZipFileSystem)))

(extend-protocol protocol.archive/IArchive
  ZipFileSystem
  (-url    [archive]
    (str archive))

  (-path    [archive entry]
    (.getPath archive (str entry) (make-array String 0)))

  (-list    [archive]
    (-> (protocol.archive/-path archive "/")
        (fs/select)))

  (-has?    [archive entry]
    (fs/exists? (protocol.archive/-path archive entry)))

  (-archive [archive root inputs]
    (->> inputs
         (map (juxt #(str (fs/relativize root %))
                    identity))
         (mapv (fn [[entry input]]
                 (protocol.archive/-insert archive (str entry) input)))))

  (-extract [archive output entries]
    (keep (fn [entry]
            (let [zip-path (protocol.archive/-path archive entry)
                  out-path (fs/path (str output) entry)]
              (when-not (fs/directory? zip-path)
                (fs/create-directory (fs/parent out-path))
                (fs/copy-single (protocol.archive/-path archive entry)
                                out-path
                                {:options [:replace-existing]}))))
          entries))

  (-insert  [archive entry input]
    (fs/copy-single (fs/path input)
                    (protocol.archive/-path archive entry)
                    {:options [:replace-existing]}))

  (-remove  [archive entry]
    (fs/delete (protocol.archive/-path archive entry)))

  (-write   [archive entry stream]
    (fs/write stream (protocol.archive/-path archive entry)))
  
  (-stream  [archive entry]
    (fs/input-stream (protocol.archive/-path archive entry))))

(extend-protocol protocol.archive/IArchive
  String
  (-url [archive]
    archive))

(defn create
  "creats a zip file
 
   (create \"hello/stuff.jar\")
   ;;=> creates a zip-file
 "
  {:added "3.0"}
  [archive]
  (if (fs/exists? archive)
    (throw (ex-info "Archive already exists" {:path archive}))
    (let [path (fs/path archive)]
      (do (fs/create-directory (fs/parent path))
          (FileSystems/newFileSystem
           (URI. (str "jar:file:" path))
           {"create" "true"})))))

(defn open
  "either opens an existing archive or creates one if it doesn't exist
   
   (open \"hello/stuff.jar\" {:create true})
   ;;=> creates a zip-file
 "
  {:added "3.0"}
  ([archive]
   (open archive {:create true}))
  ([archive opts]
   (cond (instance? FileSystem archive)
         archive

         :else
         (let [path (fs/path archive)]
           (cond (fs/exists? path)
                 (FileSystems/newFileSystem path nil)

                 (:create opts)
                 (create archive)

                 :else
                 (throw (ex-info "Archive does not exist" {:path archive})))))))

(defn url
  "returns the url of the archive
 
   (url (open \"hello/stuff.jar\"))
   => \"/Users/chris/Development/chit/lucidity/hello/stuff.jar\""
  {:added "3.0"}
  [archive]
  (protocol.archive/-url archive))

(defn path
  "returns the url of the archive
 
   (-> (open \"hello/stuff.jar\")
       (path \"world.java\")
       (str))
   => \"world.java\""
  {:added "3.0"}
  [archive entry]
  (protocol.archive/-path (open archive {:create false}) entry))

(defn list
  "lists all the entries in the archive
 
   (list \"hello/stuff.jar\")
   ;;=> [#path:\"/\"]
 "
  {:added "3.0"}
  [archive]
  (protocol.archive/-list (open archive {:create false})))

(defn has?
  "checks if the archive has a particular entry
 
   (has? \"hello/stuff.jar\" \"world.java\")
   => false"
  {:added "3.0"}
  [archive entry]
  (protocol.archive/-has? (open archive {:create false}) entry))

(defn archive
  "puts files into an archive
 
   (archive \"hello/stuff.jar\" \"src\")"
  {:added "3.0"}
  ([archive root]
   (let [ach (open archive)
         res (protocol.archive/-archive ach
                                        root
                                        (fs/select root {:exclude [fs/directory?]}))]
     (.close ach)
     res))
  ([archive root inputs]
   (protocol.archive/-archive (open archive) root inputs)))

(defn extract
  "extracts all file from an archive
 
   (extract \"hello/stuff.jar\")
 
   (extract \"hello/stuff.jar\" \"output\")
 
   (extract \"hello/stuff.jar\"
            \"output\"
            [\"world.java\"])"
  {:added "3.0"}
  ([archive]
   (extract archive (fs/parent (url archive))))
  ([archive output]
   (extract archive output (list archive)))
  ([archive output entries]
   (protocol.archive/-extract (open archive {:create false}) output entries)))

(defn insert
  "inserts a file to an entry within the archive
 
   (insert \"hello/stuff.jar\" \"world.java\" \"path/to/world.java\")"
  {:added "3.0"}
  [archive entry input]
  (protocol.archive/-insert (open archive) entry input))

(defn remove
  "removes an entry from the archive
 
   (remove \"hello/stuff.jar\" \"world.java\")"
  {:added "3.0"}
  [archive entry]
  (protocol.archive/-remove (open archive {:create false}) entry))

(defn write
  [archive entry stream]
  (protocol.archive/-write (open archive {:create false}) entry stream))

(defn stream
  "creates a stream for an entry wthin the archive
 
   (stream \"hello/stuff.jar\" \"world.java\")"
  {:added "3.0"}
  [archive entry]
  (protocol.archive/-stream (open archive {:create false}) entry))
