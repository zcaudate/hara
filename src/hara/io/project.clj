(ns hara.io.project
  (:require [hara.io.file :as fs]
            [hara.io.project.common :as common]
            [hara.io.project.lein :as lein]
            [hara.io.project.shadow :as shadow]))

(def ^:dynamic *include* [".clj$"])

(def project-lookup
  {lein/*project-file* lein/project
   shadow/*shadow-file* shadow/project})

(defn project-file
  "returns the current project file
 
   (project-file)
   => \"project.clj\""
  {:added "3.0"}
  []
  (->> [lein/*project-file*
        shadow/*shadow-file*]
       (filter fs/exists?)
       (first)))

(def ^{:arglists '([path time])}
  project-fn
  (memoize (fn [path time]
             (let [last (-> path fs/path fs/file-name str)]
               ((project-lookup last) path)))))

(defn project
  "returns project options as a map"
  {:added "3.0"}
  ([] (project (project-file)))
  ([path]
   (project-fn path (-> (fs/path path)
                        (fs/attributes)
                        (:last-modified-time)))))

(defn project-name
  "returns the name, read from the project map
 
   (project-name)
   => 'hara/base"
  {:added "3.0"}
  ([] (:name (project)))
  ([path]
   (:name (project path))))

(defn file-namespace
  "reads the namespace of the given path
 
   (file-namespace \"src/hara/io/project.clj\")
   => 'hara.io.project"
  {:added "3.0"}
  [path]
  (try
    (->> (fs/code path)
         (filter #(-> % first (= 'ns)))
         first
         second)
    (catch Throwable t
      (println path "Cannot be loaded"))))

(defn exclude
  "helper function for excluding certain namespaces
 
   (exclude '{lucid.legacy.analyzer :a
              lucid.legacy :a
              hara.lib.aether :b}
            [\"lucid.legacy\"])
   => '{hara.lib.aether :b}"
  {:added "3.0"}
  [lookup exclusions]
  (reduce-kv (fn [out ns v]
               (let [nss (str ns)
                     exclude? (->> exclusions
                                   (map (fn [ex]
                                          (.startsWith nss ex)))
                                   (some true?))]
                 (if exclude?
                   out
                   (assoc out ns v))))
             {}

             lookup))

(defn all-files
  "returns all the clojure files in a directory
 
   (count (all-files [\"test\"]))
   => number?
 
   (-> (all-files [\"test\"])
       (get 'hara.io.project-test))
   => #(.endsWith ^String % \"/test/hara/io/project_test.clj\")"
  {:added "3.0"}
  ([] (all-files ["."]))
  ([paths] (all-files paths {}))
  ([paths opts]
   (all-files paths opts (project)))
  ([paths opts project]
   (let [filt (-> {:include *include*}
                  (merge opts)
                  (update-in [:exclude]
                             conj
                             fs/link?))
         result (->> paths
                     (map #(fs/path (:root project) %))
                     (mapcat #(fs/select % filt))
                     (map str)
                     (map (juxt file-namespace identity))
                     (into {}))]
     (dissoc result nil))))

(defn file-lookup
  "creates a lookup of namespaces and files in the project
 
   (-> (file-lookup (project))
       (get 'hara.io.project))
   => #(.endsWith ^String % \"/src/hara/io/project.clj\")"
  {:added "3.0"}
  ([] (file-lookup (project)))
  ([project]
   (all-files (concat (:source-paths project)
                      (:test-paths project))
              {}
              project)))

(defn file-suffix
  "returns the file suffix for a given type
 
   (file-suffix) => \".clj\"
 
   (file-suffix :cljs) => \".cljs\""
  {:added "3.0"}
  ([] (file-suffix common/*type*))
  ([type]
   (-> (common/type-lookup type) :extension)))

(defn test-suffix
  "returns the test suffix
 
   (test-suffix) => \"-test\""
  {:added "3.0"}
  ([] common/*test-suffix*)
  ([s] (alter-var-root #'common/*test-suffix*
                       (constantly s))))

(defn file-type
  "returns the type of file according to the suffix
   
   (file-type \"project.clj\")
   => :source
 
   (file-type \"test/hara/code_test.clj\")
   => :test"
  {:added "3.0"}
  [path]
  (cond (.endsWith (str path)
                   (str (munge (test-suffix))
                        (file-suffix)))
        :test

        :else
        :source))

(defn sym-name
  "returns the symbol of the namespace
 
   (sym-name *ns*)
   => 'hara.io.project-test
 
   (sym-name 'a)
   => 'a"
  {:added "3.0"}
  [x]
  (cond (instance? clojure.lang.Namespace x)
        (.getName x)

        (symbol? x)
        x

        :else
        (throw (ex-info "Only symbols and namespaces are supported" {:type (type x)
                                                                     :value x}))))

(defn source-ns
  "returns the source namespace
 
   (source-ns 'a) => 'a
   (source-ns 'a-test) => 'a"
  {:added "3.0"}
  [ns]
  (let [sns (str (sym-name ns))
        suffix (test-suffix)
        sns (if (.endsWith (str sns) suffix)
              (subs sns 0 (- (count sns) (count suffix)))
              sns)]
    (symbol sns)))

(defn test-ns
  "returns the test namespace
 
   (test-ns 'a) => 'a-test
   (test-ns 'a-test) => 'a-test"
  {:added "3.0"}
  [ns]
  (let [sns (str (sym-name ns))
        suffix (test-suffix)
        sns (if (.endsWith (str sns) suffix)
              sns
              (str sns suffix))]
    (symbol sns)))

(defmacro in-context
  "creates a local context for executing code functions
 
   (in-context ((fn [current params _ project]
                  [current (:name project)])))
   => '[hara.io.project-test
        hara/base]"
  {:added "3.0"}
  [[func & args]]
  (let [project `(project)
        lookup  `(all-files ["src" "test"] {} ~'project)
        current `(.getName *ns*)
        params  `{}]
    (case (count args)
      0  `(let [~'project ~project]
            (~func ~current ~params ~lookup ~'project))
      1   (if (map? (first args))
            `(let [~'project ~project]
               (~func ~current ~(first args) ~lookup ~'project))
            `(let [~'project ~project]
               (~func ~(first args) ~params ~lookup ~'project)))
      2   `(let [~'project ~project]
             (~func ~@args ~lookup ~'project)))))
