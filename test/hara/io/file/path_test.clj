(ns hara.io.file.path-test
  (:use hara.test)
  (:require [hara.io.file.path :refer :all]
            [hara.io.file.common :as common]
            [hara.string :as string])
  (:refer-clojure :exclude [resolve]))

^{:refer hara.io.file.path/normalise :added "3.0"}
(fact "creates a string that takes notice of the user home"

  (normalise ".")
  => (str common/*cwd* "/" ".")

  (normalise "~/hello/world.txt")
  => (str common/*home* "/hello/world.txt")

  (normalise "/usr/home")
  => "/usr/home")

^{:refer hara.io.file.path/path :added "3.0"}
(comment "creates a `java.nio.file.Path object"

  (path "project.clj")
 ;;=> #path:"/Users/chris/Development/chit/hara/project.clj"

  (path (path "project.clj"))       ;; idempotent
 ;;=> #path:"/Users/chris/Development/chit/hara/project.clj"

  (path "~")                       ;; tilda
 ;;=> #path:"/Users/chris"

  (path "src" "hara/time.clj")      ;; multiple arguments
 ;;=> #path:"/Users/chris/Development/chit/hara/src/hara/time.clj"

  (path ["src" "hara" "time.clj"])  ;; vector 
 ;;=> #path:"/Users/chris/Development/chit/hara/src/hara/time.clj"

  (path (java.io.File.              ;; java.io.File object 
         "src/hara/time.clj"))
 ;;=> #path:"/Users/chris/Development/chit/hara/src/hara/time.clj"

  (path (java.net.URI.              ;; java.net.URI object 
         "file:///Users/chris/Development/chit/hara/project.clj"))
 ;;=> #path:"/Users/chris/Development/chit/hara/project.clj"
)

^{:refer hara.io.file.path/path? :added "3.0"}
(fact "checks to see if the object is of type Path"

  (path? (path "/home"))
  => true)

^{:refer hara.io.file.path/section :added "3.0"}
(fact "path object without normalisation"

  (str (section "project.clj"))
  => "project.clj"

  (str (section "src" "hara/time.clj"))
  => "src/hara/time.clj")

^{:refer hara.io.file.path/to-file :added "3.0"}
(fact "creates a java.io.File object"

  (to-file (section "project.clj"))
  => (all java.io.File
          #(-> % str (= "project.clj"))))

^{:refer hara.io.file.path/path!functionality :added "3.0"}
(fact "returns a java.nio.file.Path object"

  (str (path "~"))
  => common/*home*

  (str (path "~/../shared/data"))
  => (str (->> (re-pattern common/*sep*)
               (string/split common/*home*)
               (butlast)
               (string/joinr "/"))
          "/shared/data")

  (str (path ["hello" "world.txt"]))
  => (str common/*cwd* "/hello/world.txt"))

^{:refer hara.io.file.path/file-name :added "3.0"}
(fact "returns the last section of the path"

  (str (file-name "src/hara"))
  => "hara")

^{:refer hara.io.file.path/file-system :added "3.0"}
(fact "returns the filesystem governing the path"

  (file-system ".")
  ;; #object[sun.nio.fs.MacOSXFileSystem 0x512a9870 "sun.nio.fs.MacOSXFileSystem@512a9870"]
  => java.nio.file.FileSystem)

^{:refer hara.io.file.path/nth-segment :added "3.0"}
(fact "returns the nth segment of a given path"

  (str (nth-segment "/usr/local/bin" 1))
  => "local")

^{:refer hara.io.file.path/segment-count :added "3.0"}
(fact "returns the number of segments of a given path"

  (segment-count "/usr/local/bin")
  => 3)

^{:refer hara.io.file.path/parent :added "3.0"}
(fact "returns the parent of the given path"

  (str (parent "/usr/local/bin"))
  => "/usr/local")

^{:refer hara.io.file.path/root :added "3.0"}
(fact "returns the root path"

  (str (root "/usr/local/bin"))
  => "/")

^{:refer hara.io.file.path/relativize :added "3.0"}
(fact "returns one path relative to another"

  (str (relativize "test" "src/hara"))
  => "../src/hara")

^{:refer hara.io.file.path/subpath :added "3.0"}
(fact "returns the subpath of a given path"

  (str (subpath "/usr/local/bin/hello" 1 3))
  => "local/bin")

(comment
  (hara.code/import)
  )
