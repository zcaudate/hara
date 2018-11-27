(ns hara.deploy.package
  (:require [clojure.set :as set]
            [hara.core.sort :as sort]
            [hara.data.base.map :as map]
            [hara.deploy.analyser.common :as common]
            [hara.deploy.analyser.clj]
            [hara.deploy.analyser.cljs]
            [hara.deploy.analyser.java]
            [hara.io.file :as fs]
            [hara.io.project :as project]
            [hara.module.artifact :as artifact]
            [hara.module.deps :as deps]))

(def +default-config-dir+  "config")

(def +default-config-name+ "deploy.edn")

(def +default-interim-dir+ "target/interim")

(def +default-package-dir+ "package")

(def +suffix-types+ {:clj  ".clj$"
                     :cljs ".cljs$"
                     :cljc ".cljc$"})

(defn read-packages
  "lists all packages to be deployed
 
   (-> (all-packages {:root \".\"})
       keys
      (sort))"
  {:added "3.0"}
  [{:keys [root config]}]
  (let [dir  (or (:dir config)  +default-config-dir+)
        name (or (:name config) +default-config-name+)]
    (->> (read-string (slurp (fs/path root dir name)))
         (map (fn [[k entry]]
                (assoc entry :name k))))))

(defn create-file-lookups
  "creates a series of code lookup maps
 
   (create-lookups (project/project))
   => (contains {:clj map?
                 :cljs map?
                 :cljc map?})"
  {:added "3.0"}
  [project]
  (map/map-vals (fn [suffix]
                  (project/all-files (:source-paths project)
                                     {:include [suffix]}
                                     project))
                +suffix-types+))

(defn collect-entries-single
  ([package lookup]
   (let [nsps (keys lookup)]
     (mapcat (fn [[ns type]]
               (case type
                 :base      (filter (fn [sym] (or (= sym ns)
                                                  (.startsWith (str sym)
                                                               (str ns ".base."))))
                                    nsps)
                 :complete  (filter (fn [sym] (or (= sym ns)
                                                  (.startsWith (str sym)
                                                               (str ns "."))))
                                    nsps)
                 (throw (ex-info "Not supported." {:type type
                                                   :options [:base :complete]}))))
             (:include package)))))

(defn collect-entries
  ([packages lookups]
   (map (fn [pkg]
          (->> lookups
               (mapcat (fn [[suffix lookup]]
                         (->> (collect-entries-single pkg lookup)
                              (map (partial vector suffix)))))
               (set)
               (assoc pkg :entries)))
        packages)))

(comment
  (def -pkgs-    (read-packages {}))
  (def -lookups- (create-file-lookups (project/project)))
  
  (collect-entries-single (first -pkgs-)
                          (:clj -lookups-))
  => '(hara.core.environment hara.core.version)

  (:entries (first (collect-entries -pkgs- -lookups-)))
  => '([:clj hara.core.environment] [:clj hara.core.version]))

(defn overlapped-entries-single
  [x heap]
  (keep (fn [{:keys [name entries]}]
          (let [ol (set/intersection (:entries x)
                                     entries)]
            (if (seq ol)
              [#{name (:name x)} ol])))
        heap))

(defn overlapped-entries
  [packages]
  (loop [[x & rest :as packages] packages
         heap     []
         overlaps []]
    (cond (empty? packages)
          overlaps
          
          :else
          (let [ols (overlapped-entries-single x heap)]
            (recur rest
                   (conj heap x)
                   (concat overlaps ols))))))

(defn missing-entries
  [packages lookups]
  (reduce (fn [lookups {:keys [entries]}]
            (reduce (fn [lookups entry]
                      (map/dissoc-in lookups entry))
                    lookups
                    entries))
          (reduce-kv (fn [out k v]
                       (if (empty? v) out (assoc out k v)))
                     {}
                     lookups)
          packages))

(defn collect-dependencies
  [packages]
  (map (fn [{:keys [dependencies] :as package}]
         (->> dependencies
              (map (fn [artifact]
                     (let [rep     (artifact/artifact :rep artifact)
                           version (if (empty? (:version rep))
                                     (deps/current-version rep)
                                     (:version rep))]
                       (artifact/artifact :coord (assoc rep :version version)))))
              (assoc package :dependencies)))
       packages))

(defn collect-transfers
  [packages lookup project]
  (map (fn [{:keys [entries bundle] :as package}]
         (let [efiles (map    (fn [[suffix ns :as entry]]
                                (let [file (get-in lookup entry)]
                                  [file (str (-> (str ns)
                                                 (.replaceAll  "\\." "/")
                                                 (.replaceAll  "-" "_"))
                                             "."
                                             (name suffix))]))
                              entries)
               bfiles (mapcat (fn [{:keys [include path]}]
                                (mapcat (fn [inc]
                                          (let [base (fs/path (:root project) path)]
                                            (->> (fs/select (fs/path base inc)
                                                            {:include [fs/file?]})
                                                 (map (juxt str #(str (fs/relativize base %)))))))
                                        include))
                              bundle)]
           (assoc package :files (concat efiles bfiles))))
       packages))

;; collect-entries *
;; - display overlapped and missing entries
;; collect-dependencies *
;; 
;; collect-transfers



;; generate-manifest
;; generate-interim-dir
;; generate-metadata
;; generate-jar (name description entries files dependencies repositories)


(comment

  (empty? (:version (artifact/artifact :rep 'hello)))
  (def -ipkgs-  (collect-entries -pkgs- -lookups-))
  (def -ipkgs-  (collect-files -ipkgs- -lookups- (project/project)))
  (filter :bundle -ipkgs-)
  
  (overlapped-entries -ipkgs-) => ()
  (missing-entries -ipkgs- -lookups-) => {}

  (collect-dependencies -ipkgs-)
  (collect-files )
  )







