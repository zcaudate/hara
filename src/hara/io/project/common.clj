(ns hara.io.project.common)

(def ^:dynamic *source-paths* ["src"])

(def ^:dynamic *test-paths* ["test"])

(def ^:dynamic *target-dir* "target")

(def ^:dynamic *resource-paths* ["resources"])

(def ^:dynamic *defaults* {:source-paths *source-paths*
                           :test-paths  *test-paths*
                           :resource-paths *resource-paths*
                           :target-dir *target-dir*})

(def ^:dynamic *type* :clj)

(def type-lookup {:clj  {:extension ".clj"}
                  :cljs {:extension ".cljs"}})

(def ^:dynamic *test-suffix* "-test")

(defn artifact
  "returns the artifact map given a symbol
 
   (artifact 'hara/hara)
   => '{:name hara/hara, :artifact \"hara\", :group \"hara\"}"
  {:added "3.0"}
  [full]
  (let [group    (or (namespace full)
                     (str full))
        artifact (name full)]
    {:name full
     :artifact artifact
     :group group}))
