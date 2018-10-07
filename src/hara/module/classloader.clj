(ns hara.module.classloader
  (:require [clojure.java.io :as io]
            [hara.core.environment :as env]
            [hara.function :refer [definvoke]]
            [hara.protocol.classloader :as protocol.classloader]
            [hara.module.classloader.url-classloader :deps true]
            [hara.module.classloader.common :as common]
            [hara.module.artifact :as artifact]
            [hara.module.artifact.common :as base]
            [hara.object.query :as query])
  (:import (clojure.lang DynamicClassLoader RT)
           (java.net URL URLClassLoader)))

(env/init [[:java :newer {:major 1 :minor 8}]]
  (:require [hara.module.classloader.system-classloader :as system]))

(defonce +base+ (.getClassLoader RT))

(defn has-url?
  "checks whether the classloader has the following url
 
   (has-url? (fs/path \"src\"))
   => true"
  {:added "3.0"}
  ([path]
   (has-url? +base+ path))
  ([loader path]
   (protocol.classloader/-has-url? loader path)))

(defn get-url
  "returns the required url
 
   (get-url (fs/path \"src\"))
   ;;#object[java.net.URL 0x3d202d52 \"file:/Users/chris/Development/hara/hara/src/\"]
   => java.net.URL"
  {:added "3.0"}
  ([path]
   (get-url +base+ path))
  ([loader path]
   (protocol.classloader/-get-url loader path)))

(defn all-urls
  "returns all urls contained by the loader
 
   (all-urls)"
  {:added "3.0"}
  ([]
   (all-urls +base+))
  ([loader]
   (protocol.classloader/-all-urls loader)))

(defn add-url
  "adds a classpath to the loader
 
   (add-url (fs/path \"path/to/somewhere\"))"
  {:added "3.0"}
  ([path]
   (add-url +base+ path))
  ([loader path]
   (protocol.classloader/-add-url loader path)))

(defn remove-url
  "removes url from classloader
 
   (do (add-url (fs/path \"path/to/somewhere\"))
       (has-url? (fs/path \"path/to/somewhere\")))
   => true
 
   (remove-url (fs/path \"path/to/somewhere\"))
   (has-url? (fs/path \"path/to/somewhere\"))
   => false"
  {:added "3.0"}
  ([path]
   (remove-url +base+ path))
  ([loader path]
   (protocol.classloader/-remove-url loader path)))

(defn delegation
  "returns a list of classloaders in order of top to bottom
 
   (-> (Thread/currentThread)
       (.getContextClassLoader)
       (delegation))
   => list?"
  {:added "3.0"}
  [cl]
  (->> cl
       (iterate (fn [^ClassLoader cl] (.getParent cl)))
       (take-while #(satisfies? protocol.classloader/ILoader %))
       (reverse)))

(defn classpath
  "returns the classpath for the loader, including parent loaders
 
   (classpath)"
  {:added "3.0"}
  ([]
   (classpath +base+))
  ([loader]
   (->> (delegation loader)
        (mapcat all-urls)
        (map #(.getFile %)))))

(defonce +clojure-jar+
  (->> (classpath)
       (filter #(.contains ^String % "/org/clojure/clojure/"))
       (first)))

(defonce +spec-jar+
  (->> (classpath)
       (filter #(.contains ^String % "/org/clojure/spec"))
       (first)))

(defn all-jars
  "gets all jars on the classloader
 
   (all-jars)
   => seq?"
  {:added "3.0"}
  ([] (all-jars +base+))
 ([loader]
  (->> (classpath loader)
       (filter #(.endsWith % ".jar")))))

(defn all-paths
  "gets all paths on the classloader
 
   (all-paths)
   => seq?"
  {:added "3.0"}
  ([] (all-paths +base+))
 ([loader]
  (->> (classpath loader)
       (remove #(.endsWith % ".jar")))))

(defn url-classloader
  "returns a `java.net.URLClassLoader` from a list of strings
 
   (->> (url-classloader [\"/dev/null/\"])
        (.getURLs)
        (map str))
   => [\"file:/dev/null/\"]"
  {:added "3.0"}
  ([urls]
   (url-classloader urls +base+))
  ([urls parent]
   (URLClassLoader. (->> urls
                         (map common/to-url)
                         (into-array URL))
                    parent)))

(defn dynamic-classloader
  "creates a dynamic classloader instance
 
   (dynamic-classloader [])
   => clojure.lang.DynamicClassLoader"
  {:added "3.0"}
  ([]
   (dynamic-classloader []))
  ([urls]
   (dynamic-classloader urls +base+))
  ([urls parent]
   (DynamicClassLoader.
    (url-classloader (->> urls
                          (map common/to-url)
                          (into-array URL))
                     parent))))

(defonce +class-cache+
  (query/apply-element clojure.lang.DynamicClassLoader "classCache" []))

(defonce +rq+
  (query/apply-element clojure.lang.DynamicClassLoader "rq" []))

(defn load-class
  "loads class from an external source
   
   (.getName (load-class \"target/classes/test/Cat.class\"
                         {:name \"test.Cat\"}))
   => \"test.Cat\""
  {:added "3.0"}
  ([x]
   (load-class x {}))
  ([x opts]
   (load-class x opts (dynamic-classloader)))
  ([x opts loader]
   (protocol.classloader/-load-class x loader opts)))

(defn unload-class
  "unloads a class from the current namespace
 
   (unload-class \"test.Cat\")
   ;; #object[java.lang.ref.SoftReference 0x10074132
   ;;         \"java.lang.ref.SoftReference@10074132\"]
   "
  {:added "3.0"}
  [name]
 (.remove +class-cache+ name)
 (clojure.lang.Util/clearCache +rq+ +class-cache+))

(defmulti to-bytes
  "opens `.class` file from an external source
   (to-bytes \"target/classes/test/Dog.class\")
   => check/bytes?"
  {:added "3.0"}
  (fn [x] (type x)))

(defmethod to-bytes java.io.InputStream [stream]
  (let [o (java.io.ByteArrayOutputStream.)]
    (io/copy stream o)
    (.toByteArray o)))

(defmethod to-bytes String [path]
  (to-bytes (io/input-stream path)))

(definvoke any-load-class
  "loads a class, storing class into the global cache
 
   (any-load-class test.Cat nil nil)
   => test.Cat"
  {:added "3.0"}
  [:method {:multi protocol.classloader/-load-class
            :val [Class ClassLoader]}]
  ([cls _ _]
   (let [ref (java.lang.ref.SoftReference. cls +rq+)]
     (.put +class-cache+ (.getName cls) ref))
   cls))

(definvoke dynamic-load-bytes
  "loads a class from bytes
 
   (dynamic-load-bytes (to-bytes \"target/classes/test/Cat.class\")
                       (dynamic-classloader)
                       {:name \"test.Cat\"})
   => test.Cat"
  {:added "3.0"}
  [:method {:multi protocol.classloader/-load-class
            :val [(Class/forName "[B") DynamicClassLoader]}]
  ([bytes loader {:keys [name source] :as opts}]
   (let [_   (clojure.lang.Util/clearCache +rq+ +class-cache+)
         cls (.defineClass loader name bytes source)]
     (any-load-class cls loader opts))))

(definvoke dynamic-load-string
  "loads a class from a path string
   
   (dynamic-load-string \"<.m2>/org/yaml/snakeyaml/1.5/snakeyaml-1.5.jar\"
                        (dynamic-classloader)
                        {:name \"org.yaml.snakeyaml.Dumper\"
                         :entry-path \"org/yaml/snakeyaml/Dumper.class\"})
   => org.yaml.snakeyaml.Dumper"
  {:added "3.0"}
  [:method {:multi protocol.classloader/-load-class
            :val [String DynamicClassLoader]}]
  ([path loader {:keys [entry-path] :as opts}]
   (cond (.endsWith path ".class")
         (-> (to-bytes path)
             (dynamic-load-bytes loader opts))
         
         (or (.endsWith path ".war")
             (.endsWith path ".jar"))
         (let [resource-name (base/resource-entry entry-path)
               rt    (java.util.jar.JarFile. path)
               entry  (.getEntry rt resource-name)
               stream (.getInputStream rt entry)]
           (-> stream
               (to-bytes)
               (dynamic-load-bytes loader opts))))))

(definvoke dynamic-load-coords
  "loads a class from a coordinate
   
   (.getName (dynamic-load-coords '[org.yaml/snakeyaml \"1.5\"]
                                  (dynamic-classloader)
                                  {:name \"org.yaml.snakeyaml.Dumper\"
                                   :entry-path \"org/yaml/snakeyaml/Dumper.class\"}))
   => \"org.yaml.snakeyaml.Dumper\""
  {:added "3.0"}
  [:method {:multi protocol.classloader/-load-class
            :val [clojure.lang.PersistentVector DynamicClassLoader]}]
  ([coordinates loader {:keys [entry-path] :as opts}]
   (dynamic-load-string (artifact/artifact :path coordinates)
                        loader
                        opts)))

