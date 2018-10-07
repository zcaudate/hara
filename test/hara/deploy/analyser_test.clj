(ns hara.deploy.analyser-test
  (:use hara.test)
  (:require [hara.deploy.analyser :refer :all]
            [hara.deploy.common :as common]
            [hara.io.project :as project]))

^{:refer hara.deploy.analyser/create-lookups :added "3.0"}
(fact "creates a series of code lookup maps"

  (create-lookups (project/project))
  => (contains {:clj map?
                :cljs map?
                :cljc map?}))

^{:refer hara.deploy.analyser/init-groups :added "3.0"}
(comment "creates a list of associated namespaces"
  
  (-> (init-groups (common/all-packages {:root "."})
                   (project/all-files ["src"]))
      (select-keys '[hara.function.task hara.io.file.watch]))
  => '{hara.function.task [hara.function.task
                           hara.function.task.process
                           hara.function.task.bulk],
       hara.io.file.watch [hara.io.file.watch]})

^{:refer hara.deploy.analyser/collect-groups :added "3.0"}
(comment "collect and merge all `file-info` of namespaces for each group"

  (collect-groups '{hara.io.file.watch [hara.io.file.watch]}
                  (project/all-files ["src"]))
  => '{hara.io.file.watch {:exports #{[:clj hara.io.file.watch]
                                      [:class hara.io.file.watch.Watcher]},
                           :imports #{[:clj clojure.string]
                                      [:clj clojure.java.io]
                                      [:clj hara.protocol.watch]
                                      [:clj hara.data.base.map]
                                      [:class java.util.concurrent.TimeUnit]
                                      [:class java.nio.file.StandardWatchEventKinds]
                                      [:class java.nio.file.WatchService]
                                      [:class java.nio.file.Paths]
                                      [:class java.nio.file.FileSystems]}}})

^{:refer hara.deploy.analyser/collect-types :added "3.0"}
(comment "collect `file-info` for all `:clj`, `:cljs` and `:cljc` types"

  (-> (collect-types (common/all-packages {:root "."})
                     (create-lookups (project/project)))
      (select-keys '[hara.function.task]))
  => '{hara.function.task {:exports #{[:class hara.function.task.Task]
                                      [:clj hara.function.task]
                                      [:clj hara.function.task.process]
                                      [:clj hara.function.task.bulk]},
                           :imports #{[:clj hara.core.base.result]
                                      [:clj hara.function]
                                      [:clj clojure.set]
                                      [:clj hara.data.base.nested]
                                      [:clj hara.data.base.map]
                                      [:clj hara.data.base.seq]
                                      [:clj hara.print]
                                      [:clj hara.function.task.process]
                                      [:clj hara.function.task.bulk]}}})

^{:refer hara.deploy.analyser/collect-files :added "3.0"}
(comment "collects all files for a given package"

  (-> (collect-files (common/all-packages {:root "."})
                     (create-lookups (project/project)))
      (select-keys '[hara.function.task]))
  => (contains-in
      {'hara.function.task [[:clj 'hara.function.task string?]
                            [:clj 'hara.function.task.process string?]
                            [:clj 'hara.function.task.bulk string?]]}))

^{:refer hara.deploy.analyser/internal-deps :added "3.0"}
(comment "finds internal dependencies given collected information"

  (-> (internal-deps (collect-types (common/all-packages {:root "."})
                                    (create-lookups (project/project))))
      (select-keys '[hara.function.task hara.io.file.watch]))
  => '{hara.function.task #{hara.data hara.core},
       hara.io.file.watch #{hara.data hara.protocol}})

^{:refer hara.deploy.analyser/process-additions :added "3.0"}
(comment "allows additional files to be included for packaging"

  (->> (process-additions [{:include ["hara/string/mustache"]
                            :path "target/classes"}]
                          (project/project))
       sort
       (mapv second))
  => ["hara/string/mustache/Context.class"
      "hara/string/mustache/Mustache.class"
      "hara/string/mustache/ParserException.class"
      "hara/string/mustache/Scanner.class"
      "hara/string/mustache/Token.class"])

^{:refer hara.deploy.analyser/add-version :added "3.0"}
(comment "finds out the version of the artifact in use"

  (add-version 'org.clojure/clojure)
  => (contains ['org.clojure/clojure string?])^:hidden
  
  (add-version '[com.google.zxing/javase ""
                 :exclusions [com.beust/jcommander
                              com.github.jai-imageio/jai-imageio-core]])
  => '[com.google.zxing/javase "3.3.3"
       :exclusions [com.beust/jcommander
                    com.github.jai-imageio/jai-imageio-core]])

^{:refer hara.deploy.analyser/create-plan :added "3.0"}
(comment "creates a deployment plan"

  (-> (create-plan (project/project))
      (select-keys ['hara.function.task]))
  => (contains-in
      {'hara.function.task {:description "task execution of and standardization"
                            :name 'hara/hara.function.task
                            :artifact "hara.function.task"
                            :group "hara"
                            :version string?
                            :dependencies [['hara/hara.core string?]
                                           ['hara/hara.data string?]]
                            :files [[string? "hara/io/task.clj"]
                                    [string? "hara/io/task/process.clj"]
                                    [string? "hara/io/task/bulk.clj"]]
                            :url string?
                            :license anything}}))
