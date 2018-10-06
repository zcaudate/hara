(ns hara.module.artifact.search-test
  (:use hara.test)
  (:require [hara.module.artifact.search :refer :all]
            [hara.module.classloader :as cls]
            [hara.io.base.archive :as archive]))

^{:refer hara.module.artifact.search/match-jars :added "3.0"}
(comment "matches jars from any representation"

  (match-jars '[org.eclipse.aether/aether-api "1.1.0"])
  => ("<.m2>/org/eclipse/aether/aether-api/1.1.0/aether-api-1.1.0.jar"))

^{:refer hara.module.artifact.search/class-seq :added "3.0"}
(comment "creates a sequence of class names"

  (-> (all-jars '[org.eclipse.aether/aether-api "1.1.0"])
      (class-seq)
      (count))
  => 128)

^{:refer hara.module.artifact.search/search-match :added "3.0"}
(fact "constructs a matching function for filtering"

  ((search-match #"hello") "hello.world")
  => true

  ((search-match java.util.List) java.util.ArrayList)
  => true)

^{:refer hara.module.artifact.search/search :added "3.0"}
(comment "searches a pattern for class names"

  (->> (.getURLs cls/+base+)
       (map #(-> % str (subs (count "file:"))))
       (filter #(.endsWith % "jfxrt.jar"))
       (class-seq)
       (search [#"^javafx.*[A-Za-z0-9]Builder$"])
       (take 5))
  => (javafx.animation.AnimationBuilder
      javafx.animation.FadeTransitionBuilder
      javafx.animation.FillTransitionBuilder
      javafx.animation.ParallelTransitionBuilder
      javafx.animation.PathTransitionBuilder))
