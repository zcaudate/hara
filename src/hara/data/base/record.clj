(ns hara.data.base.record
  (:refer-clojure :exclude [empty]))

(defn empty
  "creates an empty record from an existing one
 
   (defrecord Database [host port])
 
   (record/empty (Database. \"localhost\" 8080))
   => (just {:host nil :port nil})"
  {:added "3.0"}
  [v]
  (.invoke ^java.lang.reflect.Method
   (.getMethod ^Class (type v) "create"
               (into-array Class [clojure.lang.IPersistentMap]))
           nil
           (object-array [{}])))
