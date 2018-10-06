(ns hara.test.executive-test
  (:use [hara.test :exclude [run]])
  (:require [hara.test.executive :refer :all]
            [hara.io.project :as project]))

^{:refer hara.test.executive/accumulate :added "3.0"}
(fact "helper function for accumulating results over disparate facts and files")

^{:refer hara.test.executive/interim :added "3.0"}
(fact "summary function for accumulated results")

^{:refer hara.test.executive/retrieve-line :added "3.0"}
(fact "returns the line of the test")

^{:refer hara.test.executive/summarise :added "3.0"}
(fact "creates a summary of given results")

^{:refer hara.test.executive/summarise-bulk :added "3.0"}
(fact "creates a summary of all bulk results")

^{:refer hara.test.executive/run :added "3.0"}
(fact "runs all tests in a given namespace")

(comment
  
  (def res (project/in-context (run 'hara.core.base.check-test)))

  (->> (:passed res)
       (count)))