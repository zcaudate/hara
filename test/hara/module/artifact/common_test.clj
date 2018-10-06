(ns hara.module.artifact.common-test
  (:use hara.test)
  (:require [hara.module.artifact.common :refer :all]))

^{:refer hara.module.artifact.common/resource-entry-symbol :added "3.0"}
(fact "creates a path based on a symbol"

  (resource-entry-symbol 'hara.test)
  => "hara/test.clj"

  (resource-entry-symbol 'clojure.lang.AFn)
  => "clojure/lang/AFn.class")

^{:refer hara.module.artifact.common/resource-entry :added "3.0"}
(fact "creates a entry-path based on input"

  (resource-entry "hello/world.txt")
  => "hello/world.txt"

  (resource-entry 'version-clj.core)
  => "version_clj/core.clj"

  (resource-entry java.io.File)
  => "java/io/File.class")