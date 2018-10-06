(ns hara.io.project.lein-test
  (:use hara.test)
  (:require [hara.io.project.lein :refer :all]))

^{:refer hara.io.project.lein/project :added "3.0"}
(fact "returns the root project map"

  (project))

^{:refer hara.io.project.lein/project-name :added "3.0"}
(fact "returns the project name"

  (project-name)
  => symbol?)
