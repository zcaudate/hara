(ns hara.io.project.shadow-test
  (:use hara.test)
  (:require [hara.io.project.shadow :refer :all]))

^{:refer hara.io.project.shadow/project :added "3.0"}
(comment "opens a shadow.edn file as the project"

  (project "../yin/shadow-cljs.edn"))
