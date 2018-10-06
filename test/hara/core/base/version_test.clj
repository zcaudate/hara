(ns hara.core.base.version-test
  (:use hara.test)
  (:require [hara.core.base.version :refer :all]))

^{:refer hara.core.base.version/parse-number :added "3.0"}
(fact "parse a number from string input"

  (parse-number "1") => 1)

^{:refer hara.core.base.version/parse-qualifier :added "3.0"}
(fact "parses a qualifier from string input"

  (parse-qualifier "" "") => 6

  (parse-qualifier "alpha" "") => 0^:hidden

  (parse-qualifier "beta" "") => 1

  (parse-qualifier "" "build") => -1)

^{:refer hara.core.base.version/parse :added "3.0"}
(fact "parses a version input"
  (parse "1.0.0-final")
  => {:major 1, :minor 0, :incremental 0, :qualifier 6, :release "final", :build ""}

  (parse "1.0.0-alpha+build.123")
  => {:major 1,
      :minor 0,
      :incremental 0,
      :qualifier 0,
      :release "alpha",
      :build "build.123"}^:hidden
  
  (parse "9.1-901.jdbc4")
  => {:major 9,
      :minor 1,
      :incremental nil,
      :qualifier -1,
      :release "901.jdbc4",
      :build ""})

^{:refer hara.core.base.version/version :added "3.0"}
(fact "like parse but also accepts maps"
  
  (version "1.0-RC5")
  => {:major 1, :minor 0, :incremental nil, :qualifier 3, :release "rc5", :build ""})

^{:refer hara.core.base.version/equal? :added "3.0"}
(fact "compares if two versions are the same"

  (equal? "1.2-final" "1.2")
  => true)

^{:refer hara.core.base.version/newer? :added "3.0"}
(fact "returns true if the the first argument is newer than the second"

  (newer? "1.2" "1.0")
  => true

  (newer? "1.2.2" "1.0.4")
  => true)

^{:refer hara.core.base.version/older? :added "3.0"}
(fact "returns true if the the first argument is older than the second"

  (older? "1.0-alpha" "1.0-beta")
  => true

  (older? "1.0-rc1" "1.0")
  => true)
