(ns hara.core.environment-test
  (:use hara.test)
  (:require [hara.core.environment :as env]
            [clojure.java.io :as io]))

^{:refer hara.core.environment/properties :added "3.0"}
(fact "returns jvm properties in a nested map for easy access"
  (->> (env/properties)
       :os)
  => (contains {:arch anything
                :name anything
                :version anything}))

^{:refer hara.core.environment/load :added "3.0"}
(fact "returns jvm properties in a nested map for easy access"
  (->> (java.io.StringReader. (str {:version [:property "os.version"]}))
       (env/load))
  => (contains {:version string?}))

^{:refer hara.core.environment/clojure-version :added "3.0"}
(fact "returns the current clojure version"
  (env/clojure-version)
  => (contains
      {:major anything,
       :minor anything,
       :incremental anything
       :qualifier anything}))

^{:refer hara.core.environment/java-version :added "3.0"}
(fact "returns the current java version"
  (env/java-version)
  => (contains
      {:major anything,
       :minor anything,
       :incremental anything
       :qualifier anything}))

^{:refer hara.core.environment/version :added "3.0"}
(fact "alternate way of getting clojure and java version"
  (env/version :clojure)
  => (env/clojure-version)

  (env/version :java)
  => (env/java-version))

^{:refer hara.core.environment/satisfied :added "3.0"}
(fact "checks to see if the current version satisfies the given constraints"
  (env/satisfied [:java    :newer {:major 1 :minor 7}]
                 {:major 1  :minor 8})
  => true

  (env/satisfied [:java  :older {:major 1 :minor 7}]
                 {:major 1  :minor 7})
  => false

  (env/satisfied [:java  :not-newer  {:major 12 :minor 0}])
  => true)

^{:refer hara.core.environment/init :added "3.0"}
(fact "only attempts to load the files when the minimum versions have been met"

  (env/init [[:java    :newer {:major 1 :minor 8}]
             [:clojure :newer {:major 1 :minor 6}]]
            (:import java.time.Instant)))

^{:refer hara.core.environment/run :added "3.0"}
(fact "only runs the following code is the minimum versions have been met"

  (env/run [[:java    :newer {:major 1 :minor 8}]
            [:clojure :newer {:major 1 :minor 6}]]
           (Instant/ofEpochMilli 0)))
