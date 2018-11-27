(ns hara.deploy.analyser.common
  (:require [hara.io.file :as fs]))

(defmulti -file-info
  "extendable function for `file-info`"
  {:added "3.0"}
  fs/file-type)

(defmethod -file-info :default
  [file]
  {:file file
   :exports #{}
   :imports #{}})

(defrecord FileInfo []
  Object
  (toString [this] (-> this :path)))

(defmethod print-method FileInfo [v w]
  (.write w (str v)))

(def ^{:arglists '([path time])}
  file-info-fn
  (memoize (fn [path time] (-file-info path))))

(defn file-info
  "returns the exports and imports of a given file
 
   (file-info \"src/hara/tool/deploy/analyser.clj\")
   
   => '{:exports #{[:clj hara.deploy.analyser]},
        :imports #{[:clj clojure.set]
                   [:clj hara.data.base.map]
                   [:clj hara.module.artifact]
                   [:clj hara.io.file]
                   [:clj hara.io.project]
                   [:clj hara.deploy.common]
                   [:clj hara.deploy.analyser.clj]
                   [:clj hara.deploy.analyser.cljs]
                  [:clj hara.deploy.analyser.java]
                   [:clj hara.core.sort]}}"
  {:added "3.0"}
  ([path]
   (file-info-fn path
                 (-> (fs/path path)
                     (fs/attributes)
                     (:last-modified-time)))))