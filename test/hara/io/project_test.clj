(ns hara.io.project-test
  (:use hara.test)
  (:require [hara.io.project :refer :all]))

^{:refer hara.io.project/project-file :added "3.0"}
(fact "returns the current project file"

  (project-file)
  => "project.clj")

^{:refer hara.io.project/project :added "3.0"}
(fact "returns project options as a map")

^{:refer hara.io.project/project-name :added "3.0"}
(fact "returns the name, read from the project map"

  (project-name)
  => 'hara/base)

^{:refer hara.io.project/file-namespace :added "3.0"}
(fact "reads the namespace of the given path"

  (file-namespace "src/hara/io/project.clj")
  => 'hara.io.project)

^{:refer hara.io.project/exclude :added "3.0"}
(fact "helper function for excluding certain namespaces"

  (exclude '{lucid.legacy.analyzer :a
             lucid.legacy :a
             hara.lib.aether :b}
           ["lucid.legacy"])
  => '{hara.lib.aether :b})

^{:refer hara.io.project/all-files :added "3.0"}
(fact "returns all the clojure files in a directory"

  (count (all-files ["test"]))
  => number?

  (-> (all-files ["test"])
      (get 'hara.io.project-test))
  => #(.endsWith ^String % "/test/hara/io/project_test.clj"))

^{:refer hara.io.project/file-lookup :added "3.0"}
(fact "creates a lookup of namespaces and files in the project"

  (-> (file-lookup (project))
      (get 'hara.io.project))
  => #(.endsWith ^String % "/src/hara/io/project.clj"))

^{:refer hara.io.project/file-suffix :added "3.0"}
(fact "returns the file suffix for a given type"

  (file-suffix) => ".clj"

  (file-suffix :cljs) => ".cljs")

^{:refer hara.io.project/test-suffix :added "3.0"}
(fact "returns the test suffix"

  (test-suffix) => "-test")

^{:refer hara.io.project/file-type :added "3.0"}
(fact "returns the type of file according to the suffix"
  
  (file-type "project.clj")
  => :source

  (file-type "test/hara/code_test.clj")
  => :test)

^{:refer hara.io.project/sym-name :added "3.0"}
(fact "returns the symbol of the namespace"

  (sym-name *ns*)
  => 'hara.io.project-test

  (sym-name 'a)
  => 'a)

^{:refer hara.io.project/source-ns :added "3.0"}
(fact "returns the source namespace"

  (source-ns 'a) => 'a
  (source-ns 'a-test) => 'a)

^{:refer hara.io.project/test-ns :added "3.0"}
(fact "returns the test namespace"

  (test-ns 'a) => 'a-test
  (test-ns 'a-test) => 'a-test)

^{:refer hara.io.project/in-context :added "3.0"}
(fact "creates a local context for executing code functions"

  (in-context ((fn [current params _ project]
                 [current (:name project)])))
  => '[hara.io.project-test
       hara/base])

(comment
  (hara.code/import))
