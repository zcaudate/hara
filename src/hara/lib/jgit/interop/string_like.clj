(ns hara.lib.jgit.interop.string-like
  (:require [clojure.java.io :as io]
            [hara.object :as object]))

(object/string-like

 java.io.File
 {:tag   "//"
  :read     (fn [^java.io.File f] (.getPath f))
  :write    (fn [path] (io/file path))}

 org.eclipse.jgit.lib.FileMode
 {:tag   "mode"
  :read     (fn [^org.eclipse.jgit.lib.FileMode mode] (.toString mode))
  :write    (fn [data]
              (org.eclipse.jgit.lib.FileMode/fromBits
               (read-string (str "8r" data))))}

 org.eclipse.jgit.api.Git
 {:tag      "git"
  :read     (fn [^org.eclipse.jgit.api.Git repo]
              (-> repo (.getRepository) object/to-data))
  :write    (fn [path]
              (-> path
                  (object/from-data org.eclipse.jgit.lib.Repository)
                  (org.eclipse.jgit.api.Git.)))}

 org.eclipse.jgit.lib.AnyObjectId
 {:tag      "id"
  :read     (fn [^org.eclipse.jgit.lib.AnyObjectId id] (.getName id))
  :write    (fn [data]
              (org.eclipse.jgit.lib.ObjectId/fromString data))}

 org.eclipse.jgit.lib.AbbreviatedObjectId
 {:tag      "id"
  :read     (fn [^org.eclipse.jgit.lib.AbbreviatedObjectId id] (.name id))
  :write    (fn [data]
              (org.eclipse.jgit.lib.AbbreviatedObjectId/fromString data))}

 org.eclipse.jgit.lib.Repository
 {:tag      "repository"
  :read     (fn [^org.eclipse.jgit.lib.Repository repo]
              (-> repo (.getDirectory) object/to-data))
  :write    (fn [^String path]
              (org.eclipse.jgit.internal.storage.file.FileRepository. path))}

 org.eclipse.jgit.transport.URIish
 {:tag      "url"
  :read     str
  :write    (fn [^String path]
              (org.eclipse.jgit.transport.URIish. path))})
