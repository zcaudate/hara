(ns hara.module.artifact-test
  (:use hara.test)
  (:require [hara.module.artifact :refer :all]
            [hara.module.artifact.common :as base]))

^{:refer hara.module.artifact/rep->coord :added "3.0"}
(fact "encodes the rep to a coordinate"

  (-> {:group "hara" :artifact "hara" :version "2.4.0"}
      (map->Rep)
      (rep->coord))
  => '[hara/hara "2.4.0"])

^{:refer hara.module.artifact/rep->path :added "3.0"}
(comment "encodes the rep to a path"

  (-> {:group "hara" :artifact "hara" :version "2.4.0"}
      (map->Rep)
      (rep->path))
  => "<.m2>/hara/hara/2.4.0/hara-2.4.0.jar")

^{:refer hara.module.artifact/rep->string :added "3.0"}
(fact "encodes the rep to a string"

  (-> {:group "hara" :artifact "hara" :version "2.4.0"}
      (map->Rep)
      (rep->string))
  => "hara:hara:2.4.0")

^{:refer hara.module.artifact/rep? :added "3.0"}
(fact "checks if an object is of type `hara.module.artifact.Rep`"

  (rep? (rep "hara:hara:2.4.0"))
  => true)

^{:refer hara.module.artifact/coord->rep :added "3.0"}
(fact "converts a coord to a rep instance"

  (coord->rep '[hara/hara "2.4.0"])
  => (contains {:group "hara"
                :artifact "hara"
                :version "2.4.0"}))

^{:refer hara.module.artifact/path->rep :added "3.0"}
(fact "converts a path to a rep instance"

  (path->rep (str base/*local-repo* "/hara/hara/2.4.0/hara-2.4.0.jar"))
  => (contains {:group "hara"
                :artifact "hara"
                :version "2.4.0"}))

^{:refer hara.module.artifact/string->rep :added "3.0"}
(fact "converts a string to a rep instance"

  (string->rep "hara:hara:2.4.0")
  => (contains {:group "hara"
                :artifact "hara"
                :version "2.4.0"}))

^{:refer hara.module.artifact/coord? :added "3.0"}
(fact "checks for a valid coordinate"

  (coord? '[org.clojure/clojure "1.8.0"]) => true

  (coord? '[1 2 3]) => false)

^{:refer hara.module.artifact/rep :added "3.0"}
(fact "converts various formats to a rep"

  (str (rep '[hara/hara "2.4.0"]))
  => "hara:hara:jar:2.4.0"

  (str (rep "hara:hara:2.4.0"))
  => "hara:hara:jar:2.4.0")

^{:refer hara.module.artifact/rep-default :added "3.0"}
(fact "creates the default representation of a artifact"

  (into {} (rep-default "hara:hara:2.4.0"))
  => {:properties {},
      :group "hara",
      :classifier nil,
      :file nil,
      :exclusions nil,
      :scope nil,
      :extension "jar",
      :artifact "hara",
      :version "2.4.0"})

^{:refer hara.module.artifact/artifact :added "3.0"}
(fact "converts various artifact formats"

  (artifact :string '[hara/hara "2.4.0"])
  => "hara:hara:jar:2.4.0"

  (artifact :path "hara:hara:2.4.0")
  => (str base/*local-repo*
          "/hara/hara/2.4.0/hara-2.4.0.jar"))

^{:refer hara.module.artifact/artifact-default :added "3.0"}
(fact "converts an artifact in any format to the default representation"

  (artifact-default '[hara/hara "2.4.0"])
  => rep?)

^{:refer hara.module.artifact/artifact-string :added "3.0"}
(fact "converts an artifact in any format to the string representation"

  (artifact-string '[hara/hara "2.4.0"])
  => "hara:hara:jar:2.4.0")

^{:refer hara.module.artifact/artifact-symbol :added "3.0"}
(fact "converts an artifact in any format to the symbol representation"

  (artifact-symbol '[hara/hara "2.4.0"])
  => 'hara/hara)

^{:refer hara.module.artifact/artifact-path :added "3.0"}
(comment "converts an artifact in any format to the path representation"

  (artifact-path '[hara/hara "2.4.0"])
  => "<.m2>/hara/hara/2.4.0/hara-2.4.0.jar")

^{:refer hara.module.artifact/artifact-coord :added "3.0"}
(fact "converts an artifact in any format to the coord representation"

  (artifact-coord "hara:hara:jar:2.4.0")
  => '[hara/hara "2.4.0"])
