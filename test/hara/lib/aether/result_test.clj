(ns hara.lib.aether.result-test
  (:use hara.test)
  (:require [hara.lib.aether.result :refer :all]
            [clojure.string :as string]))

^{:refer hara.lib.aether.result/clojure-core? :added "3.0"}
(fact "checks if artifact represents clojure.core"

  (clojure-core? '[org.clojure/clojure "1.2.0"])
  => true)

^{:refer hara.lib.aether.result/prioritise :added "3.0"}
(fact "gives the higher version library more priority"

  (prioritise '[[a "1.0"]
                [a "1.2"]
                [a "1.1"]]
              :coord)
  => '[[a/a "1.2"]])

^{:refer hara.lib.aether.result/print-tree :added "3.0"}
(fact "prints a tree structure"

  (-> (print-tree '[[a "1.1"]
                    [[b "1.1"]
                     [[c "1.1"]
                      [d "1.1"]]]])
      (with-out-str))^:hidden
  => string?)

^{:refer hara.lib.aether.result/dependency-graph :added "3.0"}
(fact "creates a dependency graph for the results")

^{:refer hara.lib.aether.result/flatten-tree :added "3.0"}
(fact "converts a tree structure into a vector"

  (flatten-tree '[[a "1.1"]
                  [[b "1.1"]
                   [[c "1.1"]
                    [d "1.1"]]]])
  => '[[a "1.1"] [b "1.1"] [c "1.1"] [d "1.1"]])

^{:refer hara.lib.aether.result/summary :added "3.0"}
(fact "creates a summary for the different types of results")

^{:refer hara.lib.aether.result/return :added "3.0"}
(fact "returns a summary of install and deploy results")

^{:refer hara.lib.aether.result/return-deps :added "3.0"}
(fact "returns a summary of resolve and collect results")


(comment
  (def res (hara.lib.aether/install-artifact
            '[zcaudate/hara.stuff "2.4.10"]
            {:artifacts [{:file "project.clj"
                          :extension "project"}
                         {:file "README.md"
                          :extension "readme"}]}))
  
  (def res (hara.lib.aether/collect-dependencies
            '[hara.class.enum]))
  
  (hara.lib.aether/collect-dependencies '[[im.chit/hara.class.enum "2.4.8"]
                                           [im.chit/hara.class "2.4.8"]]
                                         {:return :resolved
                                          :type :coord})
  
  (hara.lib.aether/resolve-dependencies '[[im.chit/hara.class.enum "2.4.8"]
                                           [im.chit/hara.class "2.4.8"]]))
