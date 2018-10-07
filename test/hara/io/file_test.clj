(ns hara.io.file-test
  (:use hara.test)
  (:require [hara.io.file :refer :all])
  (:refer-clojure :exclude [list resolve]))

^{:refer hara.io.file/file-type :added "3.0"}
(fact "encodes the type of file as a keyword"

  (file-type "hello.clj")
  => :clj

  (file-type "hello.java")
  => :java)

^{:refer hara.io.file/directory? :added "3.0"}
(fact "checks whether a file is a directory"

  (directory? "src")
  => true

  (directory? "project.clj")
  => false)

^{:refer hara.io.file/executable? :added "3.0"}
(fact "checks whether a file is executable"

  (executable? "project.clj")
  => false

  (executable? "/usr/bin/whoami")
  => true)

^{:refer hara.io.file/exists? :added "3.0"}
(fact "checks whether a file exists"

  (exists? "project.clj")
  => true

  (exists? "NON.EXISTENT")
  => false)

^{:refer hara.io.file/hidden? :added "3.0"}
(fact "checks whether a file is hidden"

  (hidden? ".gitignore")
  => true

  (hidden? "project.clj")
  => false)

^{:refer hara.io.file/file? :added "3.0"}
(fact "checks whether a file is not a link or directory"

  (file? "project.clj")
  => true

  (file? "src")
  => false)

^{:refer hara.io.file/link? :added "3.0"}
(fact "checks whether a file is a link"

  (link? "project.clj")
  => false

  (link? (create-symlink "dev/scratch/project.lnk"
                         "project.clj"))
  => true
  (delete "dev/scratch/project.lnk"))

^{:refer hara.io.file/readable? :added "3.0"}
(fact "checks whether a file is readable"

  (readable? "project.clj")
  => true)

^{:refer hara.io.file/writable? :added "3.0"}
(fact "checks whether a file is writable"

  (writable? "project.clj")
  => true)

^{:refer hara.io.file/select :added "3.0"}
(fact "selects all the files in a directory"

  (->> (select "src/hara/io/file")
       (map #(relativize "src/hara" %))
       (map str)
       (sort))
  => ["io/file"
      "io/file/attribute.clj"
      "io/file/charset.clj"
      "io/file/common.clj"
      "io/file/filter.clj"
      "io/file/option.clj"
      "io/file/path.clj"
      "io/file/walk.clj"])

^{:refer hara.io.file/permissions :added "3.0"}
(comment "returns the permissions for a given file"

  (permissions "src")
  => "rwxr-xr-x")

^{:refer hara.io.file/shorthand :added "3.0"}
(fact "returns the shorthand string for a given entry"

  (shorthand "src")
  => "d"

  (shorthand "project.clj")
  => "-")

^{:refer hara.io.file/list :added "3.0"}
(comment "lists the files and attributes for a given directory"

  (list "src")
  => {"/Users/chris/Development/chit/hara/src" "rwxr-xr-x/d",
      "/Users/chris/Development/chit/hara/src/hara" "rwxr-xr-x/d"}

  (list "../hara/src/hara/io" {:recursive true})
  => {"/Users/chris/Development/chit/hara/src/hara/io" "rwxr-xr-x/d",
      "/Users/chris/Development/chit/hara/src/hara/io/file/reader.clj" "rw-r--r--/-",
      "/Users/chris/Development/chit/hara/src/hara/io/project.clj" "rw-r--r--/-",
      "/Users/chris/Development/chit/hara/src/hara/io/file/filter.clj" "rw-r--r--/-",
      ... ...
      "/Users/chris/Development/chit/hara/src/hara/io/file/path.clj" "rw-r--r--/-",
      "/Users/chris/Development/chit/hara/src/hara/io/file/walk.clj" "rw-r--r--/-",
      "/Users/chris/Development/chit/hara/src/hara/io/file.clj" "rw-r--r--/-"})

^{:refer hara.io.file/copy :added "3.0"}
(fact "copies all specified files from one to another"

  (copy "src" ".src" {:include [".clj"]})
  => map?

  (delete ".src"))

^{:refer hara.io.file/delete :added "3.0"}
(fact "copies all specified files from one to another"

  (do (copy "src/hara/test.clj" ".src/hara/test.clj")
      (delete ".src" {:include ["test.clj"]}))
  => #{(str (path ".src/hara/test.clj"))}

  (delete ".src")
  => set?)

^{:refer hara.io.file/empty-directory? :added "3.0"}
(fact "checks if a directory is empty, returns true if both are true"

  (empty-directory? ".")
  => false)

^{:refer hara.io.file/move :added "3.0"}
(fact "moves a file or directory"

  (do (move "shortlist" ".shortlist")
      (move ".shortlist" "shortlist"))

  (move ".non-existent" ".moved")
  => {})

^{:refer hara.io.file/create-directory :added "3.0"}
(fact "creates a directory on the filesystem"

  (do (create-directory "dev/scratch/.hello/.world/.foo")
      (directory? "dev/scratch/.hello/.world/.foo"))
  => true

  (delete "dev/scratch/.hello"))

^{:refer hara.io.file/create-symlink :added "3.0"}
(fact "creates a symlink to another file"

  (do (create-symlink "dev/scratch/project.lnk" "project.clj")
      (link? "dev/scratch/project.lnk"))
  => true

  ^:hidden
  (delete "dev/scratch/project.lnk"))

^{:refer hara.io.file/create-tmpdir :added "3.0"}
(comment "creates a temp directory on the filesystem"

  (create-tmpdir)
  ;;=> #path:"/var/folders/d6/yrjldmsd4jd1h0nm970wmzl40000gn/T/4870108199331749225"
)

^{:refer hara.io.file/parent :added "3.0"}
(fact "returns the parent of the path"

  (str (parent "/hello/world.html"))
  => "/hello")

^{:refer hara.io.file/relativize :added "3.0"}
(fact "returns the relationship between two paths"

  (str (relativize "hello"
                   "hello/world.html"))
  => "world.html")

^{:refer hara.io.file/code :added "3.0"}
(fact "takes a file and returns a lazy seq of top-level forms"

  (->> (code "src/hara/io/file.clj")
       first
       (take 2))
  => '(ns hara.io.file))

^{:refer hara.io.file/copy-single :added "3.0"}
(fact "copies a single file to a destination"

  (copy-single "project.clj"
               "dev/scratch/project.clj.bak"
               {:options #{:replace-existing}})
  => (path "." "dev/scratch/project.clj.bak")

  (delete "dev/scratch/project.clj.bak"))

^{:refer hara.io.file/write :added "3.0"}
(fact "writes a stream to a path"

  (-> (java.io.FileInputStream. "project.clj")
      (write "project.clj"
             {:options #{:replace-existing}})))

^{:refer hara.io.file/input-stream :added "3.0"}
(comment "opens a file as an input-stream"

  (input-stream "project.clj"))

^{:refer hara.io.file/output-stream :added "3.0"}
(comment "opens a file as an output-stream"

  (output-stream "project.clj"))

^{:refer hara.io.file/read-all-bytes :added "3.0"}
(fact "opens a file and reads the contents as a byte array"

  (read-all-bytes "project.clj"))

^{:refer hara.io.file/write-all-bytes :added "3.0"}
(comment "writes a byte-array to file"

  (write-all-bytes "hello.txt" (.getBytes "Hello World")))

^{:refer hara.io.file/read-all-lines :added "3.0"}
(fact "opens a file and reads the contents as an array of lines"

  (read-all-lines "project.clj"))

^{:refer clojure.core/slurp :added "3.0"}
(fact "able to slurp a path"
  (slurp (path "project.clj"))
  => (slurp "project.clj"))

^{:refer hara.io.file/file :added "3.0"}
(fact "returns the input as a file"

  (file "project.clj")
  => java.io.File)

^{:refer hara.io.file/ns->file :added "3.0"}
(fact "converts an ns string to a file string"

  (ns->file 'hara.io.file-test)
  => "hara/io/file_test")

^{:refer hara.io.file/file->ns :added "3.0"}
(fact "converts a file string to an ns string"
  
  (file->ns  "hara/io/file_test")
  => "hara.io.file-test")

(comment
  (require 'hara.code)
  (hara.code/import {:write true}
                    ))
