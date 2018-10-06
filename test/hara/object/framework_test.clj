(ns hara.object.framework-test
  (:use hara.test)
  (:require [hara.object.framework :as framework]
            [hara.object :as object]
            [hara.function :refer [definvoke]])
  (:refer-clojure :exclude [get set get-in keys]))

^{:refer hara.object.framework/string-like :added "3.0"}
(fact "creates an accessibility layer for string-like objects"

  (framework/string-like
   java.io.File
   {:tag "path"
    :read (fn [f] (.getPath f))
    :write (fn [^String path] (java.io.File. path))})
  
  (object/to-data (java.io.File. "/home"))
  => "/home"

  (object/from-data "/home" java.io.File)
  => java.io.File

  ;; Enums are automatically string-like

  (object/to-data java.lang.Thread$State/NEW)
  => "NEW")

^{:refer hara.object.framework/map-like :added "3.0"}
(fact "creates an accessibility layer for map-like objects"

  (framework/map-like
   org.eclipse.jgit.revwalk.RevCommit
   {:tag "commit"
    :include [:commit-time :name :author-ident :full-message]})

  (framework/map-like
   org.eclipse.jgit.lib.PersonIdent
   {:tag "person"
    :exclude [:time-zone]})

  (framework/map-like
   org.eclipse.jgit.api.Status
   {:tag "status"
    :display (fn [m]
               (reduce-kv (fn [out k v]
                            (if (and (or (instance? java.util.Collection v)
                                         (instance? java.util.Map v))
                                     (empty? v))
                              out
                              (assoc out k v)))
                          {}
                          m))}))

^{:refer hara.object.framework/vector-like :added "3.0"}
(fact "creates an accessibility layer for vector-like objects"

  (framework/vector-like
   org.eclipse.jgit.revwalk.RevWalk
   {:tag "commits"
    :read (fn [^org.eclipse.jgit.revwalk.RevWalk walk]
            (->> walk (.iterator) object/to-data))}))

^{:refer hara.object.framework/unextend :added "3.0"}
(fact "unextend a given class from the object framework"

  (framework/unextend org.eclipse.jgit.lib.PersonIdent)
  ;;=> [[#multifn[-meta-read 0x4ead3109] nil #multifn[print-method 0xcd219d4]]]
  ^:hidden
  (framework/map-like
   org.eclipse.jgit.lib.PersonIdent
   {:tag "person"
    :exclude [:time-zone]}))

^{:refer hara.object.framework/invoke-intern-object :added "3.0"}
(fact "creates an invoke form for an object"
  
  (framework/invoke-intern-object
   '-cl-context-
   {:type org.eclipse.jgit.lib.PersonIdent
    :tag "person"
    :exclude [:time-zone]}
   '([] nil)))
