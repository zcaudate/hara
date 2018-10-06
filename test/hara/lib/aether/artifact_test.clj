(ns hara.lib.aether.artifact-test
  (:use hara.test)
  (:require [hara.lib.aether.artifact :refer :all]
            [hara.module.artifact :as artifact]
            [hara.object :as object])
  (:import (org.eclipse.aether.artifact Artifact DefaultArtifact)
           (org.eclipse.aether.metadata DefaultMetadata)))

(fact "added `:eclipse` keyword for `Artifact` creation"

  (artifact/artifact :eclipse "hara:hara.test:2.4.8")
  => DefaultArtifact

  (->> "hara:hara.test:2.4.8"
       (artifact/artifact :eclipse)
       (artifact/artifact)
       (into {}))
  => {:group "hara",
      :artifact "hara.test",
      :extension "jar",
      :classifier "",
      :version "2.4.8",
      :properties {},
      :file ""
      :exclusions nil
      :scope nil})

(fact "creates `DefaultArtifact` from a string"

  (object/from-data "hara:hara:2.8.4" DefaultArtifact)
  ;;=> #artifact "hara:hara:jar:2.8.4"
  )


^{:refer hara.lib.aether.artifact/rep-eclipse :added "3.0"}
(fact "creates a rep from eclipse artifact"

  (str (rep-eclipse (object/from-data "hara:hara:2.8.4" DefaultArtifact)))
  => "hara:hara:jar:2.8.4")

^{:refer hara.lib.aether.artifact/artifact-eclipse :added "3.0"}
(fact "creates an eclipse artifact"

  (artifact-eclipse "hara:hara:jar:2.8.4")
  => DefaultArtifact)

^{:refer hara.lib.aether.artifact/rep-eclipse-metadata :added "3.0"}
(fact "creates a rep from an eclipse metadata instance"

  (str (rep-eclipse-metadata (object/from-data "hara:hara:2.8.4" DefaultMetadata)))
  => "hara:hara:2.8.4")

^{:refer hara.lib.aether.artifact/artifact-eclipse-metadata :added "3.0"}
(fact "creates an eclipse metadata instance"

  (artifact-eclipse-metadata "hara:hara:jar:2.8.4")
  => DefaultMetadata)
