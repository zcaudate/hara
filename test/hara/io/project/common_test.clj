(ns hara.io.project.common-test
  (:use hara.test)
  (:require [hara.io.project.common :refer :all]))

^{:refer hara.io.project.common/artifact :added "3.0"}
(fact "returns the artifact map given a symbol"

  (artifact 'hara/hara)
  => '{:name hara/hara, :artifact "hara", :group "hara"})
