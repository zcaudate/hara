(ns hara.module.classloader-test
  (:use hara.test)
  (:require [hara.module.classloader :refer :all]
            [hara.module.classloader.common :as common]
            [hara.io.file :as fs]
            [hara.core.base.check :as check]
            [clojure.java.io :as io]))

^{:refer hara.module.classloader/has-url? :added "3.0"}
(fact "checks whether the classloader has the following url"

  (has-url? (fs/path "src"))
  => true)

^{:refer hara.module.classloader/get-url :added "3.0"}
(fact "returns the required url"

  (get-url (fs/path "src"))
  ;;#object[java.net.URL 0x3d202d52 "file:/Users/chris/Development/hara/hara/src/"]
  => java.net.URL)

^{:refer hara.module.classloader/all-urls :added "3.0"}
(fact "returns all urls contained by the loader"

  (all-urls)^:hidden
  => sequential?)

^{:refer hara.module.classloader/add-url :added "3.0"}
(fact "adds a classpath to the loader"

  (add-url (fs/path "path/to/somewhere"))^:hidden

  (has-url? (fs/path "path/to/somewhere"))
  => true
  (remove-url (fs/path "path/to/somewhere")))

^{:refer hara.module.classloader/remove-url :added "3.0"}
(fact "removes url from classloader"

  (do (add-url (fs/path "path/to/somewhere"))
      (has-url? (fs/path "path/to/somewhere")))
  => true

  (remove-url (fs/path "path/to/somewhere"))
  (has-url? (fs/path "path/to/somewhere"))
  => false)

^{:refer hara.module.classloader/delegation :added "3.0"}
(fact "returns a list of classloaders in order of top to bottom"

  (-> (Thread/currentThread)
      (.getContextClassLoader)
      (delegation))
  => list?)

^{:refer hara.module.classloader/classpath :added "3.0"}
(fact "returns the classpath for the loader, including parent loaders"

  (classpath)^:hidden
  => sequential?)

^{:refer hara.module.classloader/all-jars :added "3.0"}
(fact "gets all jars on the classloader"

  (all-jars)
  => seq?)

^{:refer hara.module.classloader/all-paths :added "3.0"}
(fact "gets all paths on the classloader"

  (all-paths)
  => seq?)

^{:refer hara.module.classloader/url-classloader :added "3.0"}
(fact "returns a `java.net.URLClassLoader` from a list of strings"

  (->> (url-classloader ["/dev/null/"])
       (.getURLs)
       (map str))
  => ["file:/dev/null/"])

^{:refer hara.module.classloader/dynamic-classloader :added "3.0"}
(fact "creates a dynamic classloader instance"

  (dynamic-classloader [])
  => clojure.lang.DynamicClassLoader)

^{:refer hara.module.classloader/to-bytes :added "3.0"}
(fact "opens `.class` file from an external source"
  (to-bytes "target/classes/test/Dog.class")
  => check/bytes?)

^{:refer hara.module.classloader/unload-class :added "3.0"}
(fact "unloads a class from the current namespace"

  (unload-class "test.Cat")
  ;; #object[java.lang.ref.SoftReference 0x10074132
  ;;         "java.lang.ref.SoftReference@10074132"]
  )

^{:refer hara.module.classloader/load-class :added "3.0"}
(fact "loads class from an external source"
  
  (.getName (load-class "target/classes/test/Cat.class"
                        {:name "test.Cat"}))
  => "test.Cat")

^{:refer hara.module.classloader/any-load-class :added "3.0"}
(fact "loads a class, storing class into the global cache"

  (any-load-class test.Cat nil nil)
  => test.Cat)

^{:refer hara.module.classloader/dynamic-load-bytes :added "3.0"}
(fact "loads a class from bytes"

  (dynamic-load-bytes (to-bytes "target/classes/test/Cat.class")
                      (dynamic-classloader)
                      {:name "test.Cat"})
  => test.Cat)

^{:refer hara.module.classloader/dynamic-load-string :added "3.0"}
(comment "loads a class from a path string"
  
  (dynamic-load-string "<.m2>/org/yaml/snakeyaml/1.5/snakeyaml-1.5.jar"
                       (dynamic-classloader)
                       {:name "org.yaml.snakeyaml.Dumper"
                        :entry-path "org/yaml/snakeyaml/Dumper.class"})
  => org.yaml.snakeyaml.Dumper)

^{:refer hara.module.classloader/dynamic-load-coords :added "3.0"}
(comment "loads a class from a coordinate"
  
  (.getName (dynamic-load-coords '[org.yaml/snakeyaml "1.5"]
                                 (dynamic-classloader)
                                 {:name "org.yaml.snakeyaml.Dumper"
                                  :entry-path "org/yaml/snakeyaml/Dumper.class"}))
  => "org.yaml.snakeyaml.Dumper")
