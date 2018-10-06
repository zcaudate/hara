(ns hara.io.file.path
  (:require [hara.io.file.common :as common])
  (:import (java.io File Writer)
           (java.nio.file Files Path Paths))
  (:refer-clojure :exclude [resolve]))

(def ^:dynamic *empty-string-array*
  (make-array String 0))

(defn normalise
  "creates a string that takes notice of the user home
 
   (normalise \".\")
   => (str common/*cwd* \"/\" \".\")
 
   (normalise \"~/hello/world.txt\")
   => (str common/*home* \"/hello/world.txt\")
 
   (normalise \"/usr/home\")
   => \"/usr/home\""
  {:added "3.0"}
  [s]
  (cond (= common/*os* :windows)
        (cond (not (.startsWith ^String s common/*sep*))
              (if (= 1 (.indexOf s ":\\"))
                s
                (str common/*cwd* common/*sep* s))

              :else s)

        :else
        (cond (= s "~")
              common/*home*

              (.startsWith ^String s (str "~" common/*sep*))
              (.replace ^String s "~" ^String common/*home*)

              (not (.startsWith ^String s common/*sep*))
              (str common/*cwd* common/*sep* s)

              :else s)))

(defn path
  "creates a `java.nio.file.Path object
 
   (path \"project.clj\")
  ;;=> #path:\"/Users/chris/Development/chit/hara/project.clj\"
 
   (path (path \"project.clj\"))       ;; idempotent
  ;;=> #path:\"/Users/chris/Development/chit/hara/project.clj\"
 
   (path \"~\")                       ;; tilda
  ;;=> #path:\"/Users/chris\"
 
   (path \"src\" \"hara/time.clj\")      ;; multiple arguments
  ;;=> #path:\"/Users/chris/Development/chit/hara/src/hara/time.clj\"
 
   (path [\"src\" \"hara\" \"time.clj\"])  ;; vector 
  ;;=> #path:\"/Users/chris/Development/chit/hara/src/hara/time.clj\"
 
   (path (java.io.File.              ;; java.io.File object 
          \"src/hara/time.clj\"))
  ;;=> #path:\"/Users/chris/Development/chit/hara/src/hara/time.clj\"
 
   (path (java.net.URI.              ;; java.net.URI object 
          \"file:///Users/chris/Development/chit/hara/project.clj\"))
  ;;=> #path:\"/Users/chris/Development/chit/hara/project.clj\"
 "
  {:added "3.0"}
  ([x]
   (cond (instance? Path x)
         x

         (string? x)
         (.normalize (Paths/get (normalise x) *empty-string-array*))

         (vector? x)
         (apply path x)

         (instance? java.net.URI x)
         (Paths/get x)

         (instance? File x)
         (path (.toString ^File x))

         :else
         (throw (Exception. (format "Input %s is not of the correct format" x)))))
  ([s & more]
   (.normalize (Paths/get (normalise (str s)) (into-array String (map str more))))))

(defn path?
  "checks to see if the object is of type Path
 
   (path? (path \"/home\"))
   => true"
  {:added "3.0"}
  [x]
  (instance? Path x))

(defn section
  "path object without normalisation
 
   (str (section \"project.clj\"))
   => \"project.clj\"
 
   (str (section \"src\" \"hara/time.clj\"))
   => \"src/hara/time.clj\""
  {:added "3.0"}
  ([s & more]
   (Paths/get s (into-array String more))))

(defmethod print-method Path
  [^Path v ^Writer w]
  (.write w (str "#path:\"" (.toString v) "\"")))

(defmethod print-method File
  [^File v ^Writer w]
  (.write w (str "#file:\"" (.toString v) "\"")))

(defn to-file
  "creates a java.io.File object
 
   (to-file (section \"project.clj\"))
   => (all java.io.File
           #(-> % str (= \"project.clj\")))"
  {:added "3.0"}
  [^Path path]
  (.toFile path))

(defn file-name
  "returns the last section of the path
 
   (str (file-name \"src/hara\"))
   => \"hara\""
  {:added "3.0"}
  [x]
  (.getFileName (path x)))

(defn file-system
  "returns the filesystem governing the path
 
   (file-system \".\")
   ;; #object[sun.nio.fs.MacOSXFileSystem 0x512a9870 \"sun.nio.fs.MacOSXFileSystem@512a9870\"]
   => java.nio.file.FileSystem"
  {:added "3.0"}
  [x]
  (.getFileSystem (path x)))

(defn nth-segment
  "returns the nth segment of a given path
 
   (str (nth-segment \"/usr/local/bin\" 1))
   => \"local\""
  {:added "3.0"}
  [x i]
  (.getName (path x) i))

(defn segment-count
  "returns the number of segments of a given path
 
   (segment-count \"/usr/local/bin\")
   => 3"
  {:added "3.0"}
  [x]
  (.getNameCount (path x)))

(defn parent
  "returns the parent of the given path
 
   (str (parent \"/usr/local/bin\"))
   => \"/usr/local\""
  {:added "3.0"}
  [x]
  (.getParent (path x)))

(defn root
  "returns the root path
 
   (str (root \"/usr/local/bin\"))
   => \"/\""
  {:added "3.0"}
  [x]
  (.getRoot (path x)))

(defn relativize
  "returns one path relative to another
 
   (str (relativize \"test\" \"src/hara\"))
   => \"../src/hara\""
  {:added "3.0"}
  [x other]
  (.relativize (path x) (path other)))

(defn subpath
  "returns the subpath of a given path
 
   (str (subpath \"/usr/local/bin/hello\" 1 3))
   => \"local/bin\""
  {:added "3.0"}
  [x start end]
  (.subpath (path x) start end))

(comment
  (root (path "."))

  (file-name (path "."))

  (parent (path "."))

  (nth-segment (path ".") 3))
