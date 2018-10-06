(ns hara.lib.aether.system-test
  (:use hara.test)
  (:require [hara.lib.aether.system :refer :all]))

^{:refer hara.lib.aether.system/repository-system :added "3.0"}
(fact "creates a repository system for interfacting with maven"

  (repository-system)
  => org.eclipse.aether.RepositorySystem)