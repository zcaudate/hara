(ns hara.io.base.archive-test
  (:use hara.test)
  (:require [hara.io.base.archive :refer :all])
  (:refer-clojure :exclude [list remove]))

^{:refer hara.io.base.archive/create :added "3.0"}
(comment "creats a zip file"

  (create "hello/stuff.jar")
  ;;=> creates a zip-file
)

^{:refer hara.io.base.archive/open :added "3.0"}
(comment "either opens an existing archive or creates one if it doesn't exist"
  
  (open "hello/stuff.jar" {:create true})
  ;;=> creates a zip-file
)

^{:refer hara.io.base.archive/url :added "3.0"}
(comment "returns the url of the archive"

  (url (open "hello/stuff.jar"))
  => "/Users/chris/Development/chit/lucidity/hello/stuff.jar")

^{:refer hara.io.base.archive/path :added "3.0"}
(comment "returns the url of the archive"

  (-> (open "hello/stuff.jar")
      (path "world.java")
      (str))
  => "world.java")

^{:refer hara.io.base.archive/list :added "3.0"}
(comment "lists all the entries in the archive"

  (list "hello/stuff.jar")
  ;;=> [#path:"/"]
)

^{:refer hara.io.base.archive/has? :added "3.0"}
(comment "checks if the archive has a particular entry"

  (has? "hello/stuff.jar" "world.java")
  => false)

^{:refer hara.io.base.archive/archive :added "3.0"}
(comment "puts files into an archive"

  (archive "hello/stuff.jar" "src"))

^{:refer hara.io.base.archive/extract :added "3.0"}
(comment "extracts all file from an archive"

  (extract "hello/stuff.jar")

  (extract "hello/stuff.jar" "output")

  (extract "hello/stuff.jar"
           "output"
           ["world.java"]))

^{:refer hara.io.base.archive/insert :added "3.0"}
(comment "inserts a file to an entry within the archive"

  (insert "hello/stuff.jar" "world.java" "path/to/world.java"))

^{:refer hara.io.base.archive/remove :added "3.0"}
(comment "removes an entry from the archive"

  (remove "hello/stuff.jar" "world.java"))

^{:refer hara.io.base.archive/stream :added "3.0"}
(comment "creates a stream for an entry wthin the archive"

  (stream "hello/stuff.jar" "world.java"))
