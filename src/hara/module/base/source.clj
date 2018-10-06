(ns hara.module.base.source
  (:require [hara.core.base.error :as error]
            [hara.function.base.macro :as macro]
            [hara.string.base.common :as string]))

(def ^:dynamic *suffix* ".clj")

(defn resource-path
  "converts a namespace to a resource path
 
   (resource-path 'hara.test)
   => \"hara/test.clj\""
  {:added "3.0"}
  [ns]
  (-> (str ns)
      (munge)
      (.replaceAll "\\." string/*sep*)
      (str *suffix*)))
  
(defn ^java.net.URL resource-url
  "returns a resource url
 
   (resource-url \"hara/test.clj\")
   => java.net.URL"
  {:added "3.0"}
  ([n] (resource-url n (.getContextClassLoader (Thread/currentThread))))
  ([n ^ClassLoader loader] (.getResource loader n)))

(defn ns-metadata
  "returns the metadata associated with a given namespace
 
   (ns-metadata 'hara.test)
   => '{print-options {:arglists ([] [opts])},
        -main {:arglists ([& args])}}"
  {:added "3.0"}
  [ns]
  (->> (resource-path ns)
       (resource-url)
       (slurp)
       (error/suppress)
       (#(str "[" % "]"))
       (read-string)
       (filter (comp '#{defn
                        defmacro} first))
       (map (fn [[fsym name & more]]
              (let [[_ _ arglist & more] (macro/create-args more)
                    arglists (if (vector? arglist)
                               (list arglist)
                               (map first (cons arglist more)))]
                [name (cond-> {:arglists arglists}
                        (= fsym 'defmacro) (assoc :macro true))])))
       (into {})))
