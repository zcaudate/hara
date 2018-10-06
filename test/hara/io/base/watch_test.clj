(ns hara.io.base.watch-test
  (:use hara.test)
  (:require [hara.io.base.watch :refer :all]
            [hara.core.base.watch :as watch]
            [clojure.java.io :as io])
  (:import [java.nio.file WatchService Paths FileSystems]))

^{:refer hara.io.base.watch/pattern :added "3.0"}
(comment "creates a regex pattern from the string representation"

  (pattern ".*") => #"\Q.\E.+"

  (pattern "*.jar") => #".+\Q.\Ejar")

^{:refer hara.io.base.watch/register-entry :added "3.0"}
(comment "adds a path to the watch service"

  (-> (.newWatchService (FileSystems/getDefault))
      (register-entry "src")))

^{:refer hara.io.base.watch/register-sub-directory :added "3.0"}
(comment "registers a directory to an existing watcher"

  (-> (watcher ["src"] {} {})
      (assoc :service (.newWatchService (FileSystems/getDefault)))
      (register-sub-directory "test")))

^{:refer hara.io.base.watch/register-path :added "3.0"}
(comment "registers either a file or a path to the watcher"
  (-> (watcher [] {} {})
      (assoc :service (.newWatchService (FileSystems/getDefault)))
      (register-path "test")))

^{:refer hara.io.base.watch/process-event :added "3.0"}
(comment "helper function to process event")

^{:refer hara.io.base.watch/run-watcher :added "3.0"}
(comment "initiates the watcher with the given callbacks")

^{:refer hara.io.base.watch/start-watcher :added "3.0"}
(comment "starts the watcher")

^{:refer hara.io.base.watch/stop-watcher :added "3.0"}
(comment "stops the watcher")

^{:refer hara.io.base.watch/watcher :added "3.0"}
(fact "the watch interface provided for java.io.File"

  (def ^:dynamic *happy* (promise))

  (watch/add (io/file ".") :save
             (fn [f k _ [cmd file]]
               (watch/remove f k)
               (.delete file)
               (deliver *happy* [cmd (.getName file)]))
             {:types #{:create :modify}
              :recursive false
              :filter  [".hara"]
              :exclude [".git" "target"]})

  (watch/list (io/file "."))
  => (contains {:save fn?})

  (spit "happy.hara" "hello")

  @*happy*
  => [:create "happy.hara"]

  (watch/list (io/file "."))
  => {})

^{:refer hara.io.base.watch/watch-callback :added "3.0"}
(comment "helper function to create watch callback")

^{:refer hara.io.base.watch/add-io-watch :added "3.0"}
(comment "registers the watch to a global list of *filewatchers*")

^{:refer hara.io.base.watch/list-io-watch :added "3.0"}
(comment "list all *filewatchers")

^{:refer hara.io.base.watch/remove-io-watch :added "3.0"}
(comment "removes the watcher with the given key")

(comment
  (hara.code/import))