(ns hara.deploy.analyser
  (:require [clojure.set :as set]
            [hara.deploy.common :as common]
            [hara.deploy.analyser.clj]
            [hara.deploy.analyser.cljs]
            [hara.deploy.analyser.java]
            [hara.io.file :as fs]
            [hara.io.project :as project]
            [hara.module.artifact :as artifact]
            [hara.module.deps :as deps]
            [hara.data.base.map :as map]
            [hara.core.base.sort :as sort]))

(defn create-lookups
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
                common/*file-types*))

(defn init-groups
  "creates a list of associated namespaces
   
   (-> (init-groups (common/all-packages {:root \".\"})
                    (project/all-files [\"src\"]))
       (select-keys '[hara.function.task hara.io.base.watch]))
   => '{hara.function.task [hara.function.task
                            hara.function.task.process
                            hara.function.task.bulk],
        hara.io.base.watch [hara.io.base.watch]}"
  {:added "3.0"}
  ([packages lookup]
   (let [pkgs (keys packages)
         tagged  (->> (keys lookup)
                      (map (fn [ns]
                             [ns (keep (fn [pkg]
                                         (if (.startsWith (str ns)
                                                          (str pkg))
                                           pkg))
                                       pkgs)])))
         missing (filter (fn [[_ v]] (not= 1 (count v))) tagged)

         _ (comment (if-not (empty? missing)
                      (throw (ex-info "Existing packages not accounted for."
                                      {:missing missing}))))]
     (reduce (fn [out [ns [group]]]
               (update-in out [group] (fnil #(conj % ns) [])))
             {}
             tagged))))

(defn collect-groups
  "collect and merge all `file-info` of namespaces for each group
 
   (collect-groups '{hara.io.base.watch [hara.io.base.watch]}
                   (project/all-files [\"src\"]))
   => '{hara.io.base.watch {:exports #{[:clj hara.io.base.watch]
                                       [:class hara.io.base.watch.Watcher]},
                            :imports #{[:clj clojure.string]
                                       [:clj clojure.java.io]
                                       [:clj hara.protocol.watch]
                                       [:clj hara.data.base.map]
                                      [:class java.util.concurrent.TimeUnit]
                                       [:class java.nio.file.StandardWatchEventKinds]
                                       [:class java.nio.file.WatchService]
                                       [:class java.nio.file.Paths]
                                       [:class java.nio.file.FileSystems]}}}"
  {:added "3.0"}
  [groups lookup]
  (map/map-vals (fn [nss]
                  (->> (map (comp common/file-info lookup) nss)
                       (apply merge-with set/union)))
                groups))

(defn collect-types
  "collect `file-info` for all `:clj`, `:cljs` and `:cljc` types
 
   (-> (collect-types (common/all-packages {:root \".\"})
                      (create-lookups (project/project)))
       (select-keys '[hara.function.task]))
   => '{hara.function.task {:exports #{[:class hara.function.task.Task]
                                       [:clj hara.function.task]
                                       [:clj hara.function.task.process]
                                       [:clj hara.function.task.bulk]},
                            :imports #{[:clj hara.core.base.result]
                                      [:clj hara.function]
                                       [:clj clojure.set]
                                       [:clj hara.data.base.nested]
                                       [:clj hara.data.base.map]
                                       [:clj hara.data.base.seq]
                                       [:clj hara.print]
                                       [:clj hara.function.task.process]
                                       [:clj hara.function.task.bulk]}}}"
  {:added "3.0"}
  [packages lookups]
  (->> lookups
       (map (fn [[_ lookup]]
              (let [groups    (init-groups packages lookup)
                    collected (collect-groups groups lookup)]
                collected)))
       (apply merge-with (fn [& args] (apply merge-with set/union args)))))

(defn collect-files
  "collects all files for a given package
 
   (-> (collect-files (common/all-packages {:root \".\"})
                      (create-lookups (project/project)))
       (select-keys '[hara.function.task]))
   => (contains-in
       {'hara.function.task [[:clj 'hara.function.task string?]
                             [:clj 'hara.function.task.process string?]
                             [:clj 'hara.function.task.bulk string?]]})"
  {:added "3.0"}
  [packages lookups]
  (->> lookups
       (map (fn [[type lookup]]
              (let [groups    (init-groups packages lookup)
                    collected (map/map-vals (fn [nss]
                                              (map #(vector type % (lookup %)) nss))
                                            groups)]
                collected)))
       (apply merge-with concat)))

(defn internal-deps
  "finds internal dependencies given collected information
 
   (-> (internal-deps (collect-types (common/all-packages {:root \".\"})
                                     (create-lookups (project/project))))
       (select-keys '[hara.function.task hara.io.base.watch]))
   => '{hara.function.task #{hara.data hara.core},
        hara.io.base.watch #{hara.data hara.protocol}}"
  {:added "3.0"}
  [collected]
  (map/map-entries (fn [[ns {:keys [imports]}]]
                     [ns (->> (dissoc collected ns)
                              (keep (fn [[ins {:keys [exports]}]]
                                      (if-not (empty? (set/intersection imports exports))
                                        ins)))
                              (set))])
                   collected))

(defn process-additions
  "allows additional files to be included for packaging
 
   (->> (process-additions [{:include [\"hara/string/mustache\"]
                             :path \"target/classes\"}]
                           (project/project))
        sort
        (mapv second))
   => [\"hara/string/mustache/Context.class\"
       \"hara/string/mustache/Mustache.class\"
       \"hara/string/mustache/ParserException.class\"
      \"hara/string/mustache/Scanner.class\"
       \"hara/string/mustache/Token.class\"]"
  {:added "3.0"}
  [additions project]
  (mapcat (fn [{:keys [include path]}]
            (mapcat (fn [inc]
                      (let [base (fs/path (:root project) path)]
                        (->> (fs/select (fs/path base inc)
                                        {:include [fs/file?]})
                             (map (juxt str #(str (fs/relativize base %)))))))
                    include))
          additions))

(defn add-version
  "finds out the version of the artifact in use
 
   (add-version 'org.clojure/clojure)
   => (contains ['org.clojure/clojure string?])"
  {:added "3.0"}
  [artifact]
  (let [rep  (artifact/artifact :rep artifact)
        version (deps/current-version rep)]
    (artifact/artifact :coord (assoc rep :version version))))

(defn create-plan
  "creates a deployment plan
 
   (-> (create-plan (project/project))
       (select-keys ['hara.function.task]))
   => (contains-in
       {'hara.function.task {:description \"task execution of and standardization\"
                             :name 'hara/hara.function.task
                             :artifact \"hara.function.task\"
                             :group \"hara\"
                             :version string?
                            :dependencies [['hara/hara.core string?]
                                            ['hara/hara.data string?]]
                             :files [[string? \"hara/io/task.clj\"]
                                     [string? \"hara/io/task/process.clj\"]
                                     [string? \"hara/io/task/bulk.clj\"]]
                             :url string?
                             :license anything}})"
  {:added "3.0"}
  [{:keys [version group url license] :as project}]
  (let [packages  (common/all-packages project)
        lookups   (create-lookups project)
        files     (collect-files packages lookups)
        internal  (-> (collect-types packages lookups)
                      (internal-deps)
                      (dissoc nil))
        _         (sort/topological-sort internal) ;; check for circular dependencies
        file-fn   (fn [[type ns origin]]
                    [origin (str (fs/ns->file ns) "." (name type))])]
    (->> packages
         (map/map-entries (fn [[k {:keys [additions dependencies] :as entry}]]
                            (let [others (process-additions additions project)
                                  dependencies (mapv add-version dependencies)
                                  all (->> (sort (get internal k))
                                           (mapv #(vector (symbol group (str %)) version))
                                           (concat dependencies)
                                           (vec))]
                              [k (merge entry
                                        {:name (symbol group (str k))
                                         :artifact (str k)
                                         :group    group
                                         :version  version
                                         :dependencies all
                                         :files (->> (get files k)
                                                     (map file-fn)
                                                     (concat others)
                                                     vec)
                                         :url      url
                                         :license  license})]))))))
