(ns hara.object.framework
  (:require [hara.module :as module]
            [hara.string :as string]
            [hara.data :as data]
            [hara.function :as fn :refer [definvoke]]
            [hara.protocol.function :as protocol.function]
            [hara.protocol.object :as protocol.object]
            [hara.print :as print]
            [hara.object.query :as query]
            [hara.object.framework.map-like :as map-like]
            [hara.object.framework.read :as read]
            [hara.object.framework.string-like :as string-like]
            [hara.object.framework.vector-like :as vector-like]
            [hara.object.framework.write :as write])
  (:refer-clojure :exclude [get set get-in keys]))

(defmacro string-like
  "creates an accessibility layer for string-like objects
 
   (framework/string-like
    java.io.File
    {:tag \"path\"
     :read (fn [f] (.getPath f))
     :write (fn [^String path] (java.io.File. path))})
   
   (object/to-data (java.io.File. \"/home\"))
   => \"/home\"
 
   (object/from-data \"/home\" java.io.File)
   => java.io.File
 
   ;; Enums are automatically string-like
 
   (object/to-data java.lang.Thread$State/NEW)
   => \"NEW\""
  {:added "3.0"}
  [& {:as classes}]
  `(vector ~@(map (fn [[cls opts]]
                    `(string-like/extend-string-like ~cls ~opts))
                  classes)))

(defmacro map-like
  "creates an accessibility layer for map-like objects
 
   (framework/map-like
    org.eclipse.jgit.revwalk.RevCommit
    {:tag \"commit\"
     :include [:commit-time :name :author-ident :full-message]})
 
   (framework/map-like
    org.eclipse.jgit.lib.PersonIdent
    {:tag \"person\"
     :exclude [:time-zone]})
 
   (framework/map-like
    org.eclipse.jgit.api.Status
   {:tag \"status\"
     :display (fn [m]
                (reduce-kv (fn [out k v]
                             (if (and (or (instance? java.util.Collection v)
                                          (instance? java.util.Map v))
                                      (empty? v))
                               out
                               (assoc out k v)))
                           {}
                           m))})"
  {:added "3.0"}
  [& {:as classes}]
  `(vector ~@(map (fn [[cls opts]]
                    `(map-like/extend-map-like ~cls ~opts))
                  classes)))

(defmacro vector-like
  "creates an accessibility layer for vector-like objects
 
   (framework/vector-like
    org.eclipse.jgit.revwalk.RevWalk
   {:tag \"commits\"
     :read (fn [^org.eclipse.jgit.revwalk.RevWalk walk]
             (->> walk (.iterator) object/to-data))})"
  {:added "3.0"}
  [& {:as classes}]
  `(vector ~@(map (fn [[cls opts]]
                    `(vector-like/extend-vector-like ~cls ~opts))
                  classes)))

(defn unextend
  "unextend a given class from the object framework
 
   (framework/unextend org.eclipse.jgit.lib.PersonIdent)
   ;;=> [[#multifn[-meta-read 0x4ead3109] nil #multifn[print-method 0xcd219d4]]]
   "
  {:added "3.0"}
  [cls]
  [(fn/multi-remove protocol.object/-meta-read cls)
   (fn/multi-remove protocol.object/-meta-write cls)
   (fn/multi-remove print-method cls)
   (fn/memoize-remove read/meta-read cls)
   (fn/memoize-remove write/meta-write cls)])

(definvoke invoke-intern-object
  "creates an invoke form for an object
   
   (framework/invoke-intern-object
    '-cl-context-
    {:type com.jogamp.opencl.CLContext
     :tag \"cl.context\"
    :read :all
     :exclude [:platform :context :devices :max-flops-device]}
    '([] (com.jogamp.opencl.CLContext/create)))"
  {:added "3.0"}
  [:method {:multi protocol.function/-invoke-intern
            :val :object}]
  ([name config body]
   (invoke-intern-object :object name config body))
  ([_ name {:keys [type extend tag read write display] :as config} body]
   (let [arglists (if (seq body)
                    (fn/form-arglists body)
                    (or (:arglists config) ()))
         
         object-fn (case extend
                     :string-like `string-like/extend-string-like
                     :vector-like `vector-like/extend-vector-like
                     `map-like/extend-map-like)]
     `(do (declare ~name)
          [(~object-fn ~type ~config)
           (defn ~name ~@body)]))))