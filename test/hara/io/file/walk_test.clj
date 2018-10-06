(ns hara.io.file.walk-test
  (:use hara.test)
  (:require [hara.io.file.walk :refer :all]
            [hara.io.file.path :as path]))

^{:refer hara.io.file.walk/match-single :added "3.0"}
(fact "matches according to the defined filter"

  (match-single {:root (path/path ".")
                 :path (path/path "src/hara/test.clj")}
                {:tag :pattern
                 :pattern #"src"})
  => true

  (match-single {:root (path/path "src")
                 :path (path/path "src/hara/test.clj")}
                {:tag :pattern
                 :pattern #"src"})
  => false

  (match-single {:path (path/path "src/hara/test.clj")}
                {:tag :fn
                 :fn (fn [m]
                       (re-find #"hara" (str m)))})
  => true)

^{:refer hara.io.file.walk/match-filter :added "3.0"}
(fact "matches according to many filters"

  (match-filter {})
  => true

  (match-filter {:root (path/path "")
                 :path (path/path "src/hara/test.clj")
                 :include [{:tag :pattern
                            :pattern #"test"}]})
  => true

  (match-filter {:root (path/path "")
                 :path (path/path "src/hara/test.clj")
                 :exclude [{:tag :pattern
                            :pattern #"test"}]})
  => false)

^{:refer hara.io.file.walk/visit-directory-pre :added "3.0"}
(fact "helper function, triggers before visiting a directory")

^{:refer hara.io.file.walk/visit-directory-post :added "3.0"}
(fact "helper function, triggers after visiting a directory")

^{:refer hara.io.file.walk/visit-file :added "3.0"}
(fact "helper function, triggers on visiting a file")

^{:refer hara.io.file.walk/visit-file-failed :added "3.0"}
(fact "helper function, triggers on after a file cannot be visited")

^{:refer hara.io.file.walk/visitor :added "3.0"}
(fact "contructs the clojure wrapper for `java.nio.file.FileVisitor`")

^{:refer hara.io.file.walk/walk :added "3.0"}
(fact "visits files based on a directory"

  (walk "src" {:accumulate #{:directories}})
  => vector?

  (walk "src" {:accumulator (atom {})
               :accumulate  #{}
               :file (fn [{:keys [path attrs accumulator]}]
                       (swap! accumulator
                              assoc
                              (str path)
                              (.toMillis (.lastAccessTime attrs))))})
  => map?)