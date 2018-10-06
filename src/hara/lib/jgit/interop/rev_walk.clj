(ns hara.lib.jgit.interop.rev-walk
  (:require [hara.object :as object])
  (:import (org.eclipse.jgit.revwalk RevWalk)))

(object/vector-like
 RevWalk
 {:tag "commits"
  :read (fn [^RevWalk walk]
          (->> walk (.iterator) object/to-data))})
