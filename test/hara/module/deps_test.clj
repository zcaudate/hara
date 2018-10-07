(ns hara.module.deps-test
  (:use hara.test)
  (:require [hara.module.deps :refer :all]
            [hara.io.file :as fs])
  (:refer-clojure :exclude [load resolve]))

^{:refer hara.module.deps/resolve-classloader :added "3.0"}
(fact "resolves a class or namespace to a physical location"

  (resolve-classloader String)
  => (contains
      [anything #"java/lang/String.class"])

  (resolve-classloader 'hara.test)
  => (contains 
      [nil (str (fs/path "src/hara/test.clj"))]))

^{:refer hara.module.deps/resolve-jar-entry :added "3.0"}
(fact "resolves a class or namespace within a jar"

  (resolve-jar-entry 'clojure.core
                     ['org.clojure/clojure
                      (current-version 'org.clojure/clojure)])
  => (contains-in [string?
                   "clojure/core.clj"])

  (resolve-jar-entry 'clojure.core
                     "org.clojure:clojure:1.9.0"
                     {:tag :coord})
  => '[[org.clojure/clojure "1.9.0"]
       "clojure/core.clj"])

^{:refer hara.module.deps/resolve :added "3.0"}
(fact "resolves a class or namespace within a context"

  (resolve 'clojure.core
           ['org.clojure/clojure
            (current-version 'org.clojure/clojure)])
  => (contains [string?
                "clojure/core.clj"])

  (resolve 'wrong.namespace
           ["org.clojure:clojure:1.9.0"]
           {:tag :coord})
  => nil)

^{:refer hara.module.deps/loaded-artifact? :added "3.0"}
(fact "checks if artifact has been loaded"

  (loaded-artifact? '[org.clojure/clojure "1.9.0"])
  => true)

^{:refer hara.module.deps/load-artifact :added "3.0"}
(fact "loads an artifact into the system"

  (load-artifact '[org.clojure/clojure "1.9.0"]))

^{:refer hara.module.deps/unload-artifact :added "3.0"}
(comment "unloads an artifact from the system"

  (unload-artifact '[org.clojure/clojure "1.9.0"]))

^{:refer hara.module.deps/all-loaded-artifacts :added "3.0"}
(fact "returns all loaded artifacts"

  (all-loaded-artifacts)^:hidden
  => sequential?)

^{:refer hara.module.deps/all-loaded :added "3.0"}
(fact "returns all the loaded artifacts of the same group and name"

  (all-loaded 'org.clojure/clojure)
  ;;=> ('org.clojure:clojure:jar:<version>)
  => sequential?)

^{:refer hara.module.deps/unload :added "3.0"}
(fact "unloads all artifacts in list")

^{:refer hara.module.deps/load :added "3.0"}
(fact "loads all artifacts in list, unloading previous versions of the same artifact")

^{:refer hara.module.deps/clean :added "3.0"}
(fact "cleans the maven entries for the artifact, `:full` deletes all the versions"
  
  (clean '[org.clojure/clojure "2.4.8"]
         {:full true
          :simulate true})
  => set?)

^{:refer hara.module.deps/version-map :added "3.0"}
(fact "returns all the loaded artifacts and their versions"
  (version-map)
  => map?)

^{:refer hara.module.deps/current-version :added "3.0"}
(fact "finds the current artifact version for a given classloader"

  (current-version 'org.clojure/clojure)
  => string?)
