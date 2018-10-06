(ns hara.lib.aether.dependency-test
  (:use hara.test)
  (:require [hara.lib.aether.dependency :refer :all])
  (:import (org.eclipse.aether.graph  Exclusion)))

^{:refer hara.lib.aether.dependency/rep-exclusion :added "3.0"}
(fact "creates a rep from an exclusion"
  
  (str (rep-exclusion (artifact-exclusion "hara:hara")))
  => "hara:hara:jar:")

^{:refer hara.lib.aether.dependency/artifact-exclusion :added "3.0"}
(fact "creates an artifact exclusion"

  (artifact-exclusion "hara:hara:jar:2.8.4")
  => Exclusion)
