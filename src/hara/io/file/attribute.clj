(ns hara.io.file.attribute
  (:require [hara.io.file.common :as common]
            [hara.io.file.path :as path]
            [hara.string :as string])
  (:import (java.nio.file FileSystems Files LinkOption)
           (java.nio.file.attribute FileAttribute FileTime PosixFilePermissions)))

(def ^:dynamic *empty* (make-array FileAttribute 0))

(defn owner
  "returns the owner of the file
 
   (owner \"project.clj\")
   => string?"
  {:added "3.0"}
  [path]
  (str (Files/getOwner (path/path path) common/*no-follow*)))

(defn lookup-owner
  "lookup the user registry for the name
 
   (lookup-owner \"WRONG\")
   => (throws)"
  {:added "3.0"}
  [owner]
  (-> (FileSystems/getDefault)
      (.getUserPrincipalLookupService)
      (.lookupPrincipalByName owner)))

(defn set-owner
  "sets the owner of a particular file
 
   (set-owner \"test\" \"WRONG\")
   => (throws)"
  {:added "3.0"}
  [path owner]
  (let [path (path/path path)
        principle (lookup-owner owner)]
    (Files/setOwner path principle)))

(defn lookup-group
  "lookup the user registry for the name
 
   (lookup-group \"WRONG\")
   => (throws)"
  {:added "3.0"}
  [group]
  (-> (FileSystems/getDefault)
      (.getUserPrincipalLookupService)
      (.lookupPrincipalByGroupName group)))

(deftype Attribute [_name _value]
  FileAttribute
  (name [attr]
    _name)
  (value [attr]
    _value))

(defn attr
  "creates an attribute for input to various functions"
  {:added "3.0"}
  ([name value]
   (attr name value nil))
  ([name value prefix]
   (Attribute. (if prefix
                 (str prefix ":" name)
                 name)
               value)))

(defn attr-value
  "adjusts the attribute value for input"
  {:added "3.0"}
  [k v]
  (let [nk (name k)]
    (cond (.endsWith nk "owner") (lookup-owner v)
          (.endsWith nk "group") (lookup-group v)
          (.endsWith nk "fileKey") (throw (Exception. "Cannot make fileKey"))
          (.endsWith nk "permissions") (PosixFilePermissions/fromString v)
          (#{:ctime :last-access-time :last-modified-time :creation-time} k)
          (FileTime/fromMillis v)
          :else v)))

(defn map->attr-array
  "converts a clojure map to an array of attrs"
  {:added "3.0"}
  ([m]
   (map->attr-array m (name common/*system*)))
  ([m prefix]
   (->> m
        (reduce-kv (fn [out k v]
                     (conj out (attr (string/camel-type (name k))
                                     (attr-value k v)
                                     prefix)))
                   [])
        (into-array FileAttribute))))

(defn attrs->map
  "converts the map of attributes into a clojure map"
  {:added "3.0"}
  [attrs]
  (reduce (fn [out [k v]]
            (assoc out (string/spear-type (keyword k))
                   (cond (= k "owner") (str v)
                         (= k "group") (str v)
                         (= k "fileKey") (str v)
                         (= k "permissions") (PosixFilePermissions/toString v)
                         (instance? FileTime v) (.toMillis v)
                         :else v)))
          {}
          attrs))

(defn attributes
  "shows all attributes for a given path
 
   (attributes \"project.clj\")
   ;;    {:owner \"chris\",
   ;;     :group \"staff\",
   ;;     :permissions \"rw-r--r--\",
   ;;     :file-key \"(dev=1000004,ino=2351455)\",
   ;;     :ino 2351455,
   ;;     :is-regular-file true.
   ;;     :is-directory false, :uid 501,
   ;;     :is-other false, :mode 33188, :size 4342,
   ;;     :gid 20, :ctime 1476755481000,
   ;;     :nlink 1,
   ;;     :last-access-time 1476755481000,
   ;;     :is-symbolic-link false,
   ;;     :last-modified-time 1476755481000,
   ;;     :creation-time 1472282953000,
   ;;     :dev 16777220, :rdev 0}
   => map"
  {:added "3.0"}
  [path]
  (-> (path/path path)
      (Files/readAttributes (str (name common/*system*) ":*")
                            common/*no-follow*)
      (attrs->map)))

(defn set-attributes
  "sets all attributes for a given path
 
   (set-attributes \"project.clj\"
                   {:owner \"chris\",
                    :group \"staff\",
                    :permissions \"rw-rw-rw-\"})
   ;;=> #path:\"/Users/chris/Development/chit/lucidity/project.clj\"
   "
  {:added "3.0"}
  [path m]
  (reduce-kv (fn [_ k v]
               (-> (path/path path)
                   (Files/setAttribute (str (name common/*system*) ":"
                                            (string/camel-type (name k)))
                                       (attr-value k v)
                                       common/*no-follow*)))
             nil
             m))
