(ns hara.lib.aether.local-repo-test
  (:use hara.test)
  (:require [hara.lib.aether.local-repo :refer :all]
            [hara.object :as object])
  (:import [org.eclipse.aether.repository LocalRepository]))

^{:refer hara.lib.aether.local-repo/local-repo :added "3.0"}
(fact "creates a `LocalRepository` from a string"

  (local-repo)
  => LocalRepository ;; #local "<.m2/repository>"

  ;; hooks into hara.object
  (-> (local-repo "/tmp")
      (object/to-data))
  => "/tmp")

(fact "creates a `LocalRepository` from a string"

  (object/from-data "/tmp" LocalRepository)
  ;;=> #local "/tmp"
  )