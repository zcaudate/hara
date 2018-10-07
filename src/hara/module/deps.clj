(ns hara.module.deps
  (:require [hara.string.base.ansi :as ansi]
            [hara.module :as module]
            [hara.print :as print]
            [hara.io.file :as fs]
            [hara.io.archive :as archive]
            [hara.module.artifact.common :as base]
            [hara.module.artifact :as artifact]
            [hara.module.classloader :as loader]
            [clojure.java.io :as io])
  (:import (clojure.lang IPersistentVector Symbol))
  (:refer-clojure :exclude [load resolve]))

(defn resolve-classloader
  "resolves a class or namespace to a physical location

   (resolve-classloader String)
   => (contains
       [anything #\"java/lang/String.class\"])

   (resolve-classloader 'hara.test)
   => (contains 
       [nil (str (fs/path \"src/hara/test.clj\"))])"
  {:added "3.0"}
  ([x] (resolve-classloader x loader/+base+))
  ([x loader]
   (let [resource (-> (base/resource-entry x)
                      (io/resource loader))]
     (if-let [path (if resource (.getPath resource))]
       (cond (.startsWith path "file:")
             (-> (subs path (count "file:"))
                 (hara.string/split #"\!/"))

             (.startsWith path "/")
             [nil path])))))

(defn resolve-jar-entry
  "resolves a class or namespace within a jar

   (resolve-jar-entry 'clojure.tools.reader
                      ['org.clojure/tools.reader
                       (jvm/current-version 'org.clojure/tools.reader)])
   => (contains-in [string?
                    \"clojure/tools/reader.clj\"])
 
   (resolve-jar-entry 'clojure.tools.reader
                      \"org.clojure:tools.reader:1.3.0\"
                      {:tag :coord})
   => '[[org.clojure/tools.reader \"1.3.0\"]
        \"clojure/tools/reader.clj\"]"
  {:added "3.0"}
  ([x artifact]
   (resolve-jar-entry x artifact {}))
  ([x artifact {:keys [tag] :or {tag :path}}]
   (let [path  (artifact/artifact :path artifact)
         entry (base/resource-entry x)]
     (if (archive/has? path entry)
       [(if (= :path tag)
          path
          (artifact/artifact tag path))
        entry]))))

(defn resolve
  "resolves a class or namespace within a context
 
   (resolve 'clojure.tools.reader
            ['org.clojure/tools.reader
             (jvm/current-version 'org.clojure/tools.reader)])
   => (contains [string?
                 \"clojure/tools/reader.clj\"])
 
   (resolve 'clojure.core
            [\"org.clojure:tools.reader:1.3.0\"]
            {:tag :coord})
   => nil"
  {:added "3.0"}
  ([x]
   (resolve x nil))
  ([x context]
   (resolve x context {}))
  ([x context {:keys [tag] :or {tag :path} :as opts}]
   (cond (nil? context)
         (resolve-classloader x)

         (and (vector? context)
              (not (artifact/coord? context)))
         (first (keep #(resolve x % opts)
                      context))

         :else
         (resolve-jar-entry x (artifact/artifact :path context) opts))))

(defn loaded-artifact?
  "checks if artifact has been loaded
 
   (loaded-artifact? '[org.clojure/clojure \"1.9.0\"])
   => true"
  {:added "3.0"}
  ([coord]
   (loaded-artifact? coord loader/+base+))
  ([coord loader]
   (loader/has-url? loader (artifact/artifact-path coord))))

(defn load-artifact
  "loads an artifact into the system
 
   (load-artifact '[potemkin/potemkin \"0.4.5\"])"
  {:added "3.0"}
  ([coord]
   (load-artifact coord loader/+base+))
  ([coord loader]
   (if-not (loaded-artifact? coord loader)
     (let [path (artifact/artifact-path coord)]
       (if (fs/exists? path)
         (do (loader/add-url loader path)
             coord)
         (throw (ex-info "Jar file does not exist:" {:path path
                                                     :coord coord})))))))

(defn unload-artifact
  "unloads an artifact from the system
 
   (unload-artifact '[potemkin/potemkin \"0.4.5\"])"
  {:added "3.0"}
  ([coord]
   (unload-artifact coord loader/+base+))
  ([coord loader]
   (if (loaded-artifact? coord loader)
     (do (loader/remove-url loader (artifact/artifact-path coord))
         coord))))

(defn all-loaded-artifacts
  "returns all loaded artifacts
 
   (all-loaded-artifacts)"
  {:added "3.0"}
  ([]
   (all-loaded-artifacts :default))
  ([type]
   (all-loaded-artifacts loader/+base+ type))
  ([loader type]
   (->> (loader/all-urls loader)
        (map #(.getFile %))
        (filter #(.endsWith % ".jar"))
        (map (partial artifact/artifact type)))))

(defn all-loaded
  "returns all the loaded artifacts of the same group and name
 
   (all-loaded 'org.clojure/clojure)
   ;;=> ('org.clojure:clojure:jar:1.8.0)
   => sequential?"
  {:added "3.0"}
  ([coord]
   (all-loaded coord loader/+base+))
  ([coord loader]
   (let [{:keys [group artifact version]} (artifact/artifact coord)]
     (->> (all-loaded-artifacts loader :default)
          (filter #(and (= artifact (:artifact %))
                        (= group (:group %))))))))

(defn unload
  "unloads all artifacts in list"
  {:added "3.0"}
  ([coords]
   (unload coords loader/+base+ :any))
  ([coords loader version]
   (let [all (all-loaded-artifacts loader :default)
         coords (map artifact/artifact coords)
         match-fn (fn [a0 a1] (and (= (:artifact a0) (:artifact a1))
                                   (= (:group a0) (:group a1))
                                   (case version
                                     :same (= (:version a0) (:version a1))
                                     :different (not= (:version a0) (:version a1))
                                     true)))
         artifacts (keep (fn [obj]
                           (if (some #(match-fn obj %) coords)
                             obj))
                         all)
         output (->> (keep #(unload-artifact % loader) artifacts)
                     (sort-by (juxt :group :artifact :version))
                     (vec))]
     (when-not (empty? output)
       (print/print-title (format "UNLOADED ARTIFACTS (%d)" (count output)))
       (doseq [x output]
         (println (ansi/bold (str "  " x)) "<=" (artifact/artifact-path x))))
     output)))

(defn load
  "loads all artifacts in list, unloading previous versions of the same artifact"
  {:added "3.0"}
  ([coords]
   (load coords loader/+base+))
  ([coords loader]
   (let [output (->> (keep #(load-artifact % loader) coords)
                     (sort-by (juxt :group :artifact :version))
                     (vec))]
     (when-not (empty? output)
       (print/print-title (format "LOADED ARTIFACTS (%d)" (count output)))
       (doseq [x output]
         (println (ansi/bold (str "  " x)) "<=" (artifact/artifact-path x))))
     output)))

(defn clean
  "cleans the maven entries for the artifact, `:full` deletes all the versions
   
   (clean '[hara/hara.string \"2.4.8\"]
          {:full true
           :simulate true})
   => set?"
  {:added "3.0"}
  ([artifact {:keys [full simulate]}]
   (let [path (fs/path (artifact/artifact-path artifact))
         del-path (nth (iterate fs/parent path)
                       (if full 2 1))]
     (fs/delete del-path {:simulate simulate}))))

(defn version-map
  "returns all the loaded artifacts and their versions
   (version-map)
   => map?"
  {:added "3.0"}
  []
  (->> (all-loaded-artifacts)
       (map (juxt :group :artifact :version))
       (map (fn [[group artifact version]]
              [(symbol group artifact) version]))
       (into {})))

(defn current-version
  "finds the current artifact version for a given classloader
 
   (current-version 'org.clojure/tools.reader)
   => \"1.3.0\""
  {:added "3.0"}
  ([artifact]
   (let [{:keys [group artifact] :as m} (artifact/artifact-default artifact)
         version (or ((version-map) (symbol group artifact))
                     (throw (ex-info "Cannot find the version of artifact." {:artifact artifact})))]
     version)))
