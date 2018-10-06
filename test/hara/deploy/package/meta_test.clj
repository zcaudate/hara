(ns hara.deploy.package.meta-test
  (:use hara.test)
  (:require [hara.deploy.package.meta :refer :all]
            [hara.io.project :as project]
            [hara.lib.jsoup :as html]))

^{:refer hara.deploy.package.meta/pom-properties :added "3.0"}
(comment "creates a pom.properties file"

  (pom-properties (project/project)))

^{:refer hara.deploy.package.meta/coordinate->dependency :added "3.0"}
(fact "creates a html tree dependency entry"

  (coordinate->dependency '[im.chit/hara "0.1.1"])
  => [:dependency
      [:groupId "im.chit"]
      [:artifactId "hara"]
      [:version "0.1.1"]])

^{:refer hara.deploy.package.meta/pom-xml :added "3.0"}
(fact "creates a pom.properties file"

  (->> (pom-xml '{:description "task execution of and standardization",
                  :name hara/hara.function.task,
                  :artifact "hara.function.task",
                  :group "hara",
                  :version "3.0.1",
                  :dependencies [[hara/hara.core "3.0.1"]
                                 [hara/hara.data "3.0.1"]]})
       (html/tree))^:hidden
  => (contains [[:modelversion "4.0.0"]
                [:packaging "jar"]
                [:groupid "hara"]
                [:artifactid "hara.function.task"]
                [:version "3.0.1"]
                [:name "hara/hara.function.task"]
                [:description "task execution of and standardization"]
                [:dependencies
                 [:dependency
                  [:groupid "hara"]
                  [:artifactid "hara.core"]
                  [:version "3.0.1"]]
                 [:dependency
                  [:groupid "hara"]
                  [:artifactid "hara.data"]
                  [:version "3.0.1"]]]]
               :gaps-ok))

^{:refer hara.deploy.package.meta/generate-manifest :added "3.0"}
(comment "creates a manifest.mf file for the project"

  (generate-manifest (project/project)))

^{:refer hara.deploy.package.meta/generate-pom :added "3.0"}
(comment "generates a pom file given an entry"

  (generate-pom {:artifact "hara.function.task"
                 :group "hara"
                 :version "3.0.1"}
                "."))

(comment

  (.replaceAll "\n\n" "\\s" "" ))