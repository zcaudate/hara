(ns hara.deploy.package.meta
  (:require [hara.io.file :as fs]
            [hara.lib.jsoup :as html]
            [hara.module.artifact :as artifact]))

(def HEADER
  {"xmlns"
   "http://maven.apache.org/POM/4.0.0"

   "xmlns:xsi"
   "http://www.w3.org/2001/XMLSchema-instance"

   "xsi:schemaLocation"
   (str "http://maven.apache.org/POM/4.0.0"
        " "
        "http://maven.apache.org/xsd/maven-4.0.0.xsd")})

(defn pom-properties
  "creates a pom.properties file
 
   (pom-properties (project/project))"
  {:added "3.0"}
  [entry]
  (str "# hara.deploy\n"
       "# " (java.util.Date.) "\n"
       "version=" (:version entry) "\n"
       "groupId=" (:group entry) "\n"
       "artifactId=" (:artifact entry)))

(defn coordinate->dependency
  "creates a html tree dependency entry
 
   (coordinate->dependency '[im.chit/hara \"0.1.1\"])
   => [:dependency
       [:groupId \"im.chit\"]
       [:artifactId \"hara\"]
       [:version \"0.1.1\"]]"
  {:added "3.0"}
  [coord]
  (let [{:keys [group artifact version exclusions scope]} (artifact/artifact :rep coord)
        scope      (if scope [:scope scope])
        exclusions (if (seq exclusions)
                     (->> exclusions
                          (map (fn [exclusion]
                                 (let [{:keys [group artifact]} (artifact/artifact :rep exclusion)]
                                   [:exclusion
                                    [:groupId group]
                                    [:artifactId artifact]])))
                          (cons :exclusions)
                          (vec)))]
    (->> (concat [:dependency
                  [:groupId group]
                  [:artifactId artifact]
                  [:version version]]
                 [scope exclusions])
         (filterv identity))))

(defn pom-xml
  "creates a pom.properties file
 
   (->> (pom-xml '{:description \"task execution of and standardization\",
                   :name hara/hara.function.task,
                  :artifact \"hara.function.task\",
                   :group \"hara\",
                   :version \"3.0.1\",
                   :dependencies [[hara/hara.core \"3.0.1\"]
                                  [hara/hara.data \"3.0.1\"]]})
        (html/tree))"
  {:added "3.0"}
  [entry]
  (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
       "\n"
       (html/html
        [:entry HEADER
         [:modelVersion "4.0.0"]
         [:packaging "jar"]
         [:groupId (:group entry)]
         [:artifactId (:artifact entry)]
         [:version (:version entry)]
         [:name (str (:name entry))]
         [:description (:description entry)]
         [:url (:url entry)]
         [:licenses
          [:license
           [:name (-> entry :license :name)]
           [:url (-> entry :license :url)]]]
         [:repositories
          [:repository
           [:id "central"]
           [:url "https://repo1.maven.org/maven2/"]
           [:snapshots [:enabled "false"]]
           [:releases [:enabled "true"]]]
          [:repository
           [:id "clojars"]
           [:url "https://clojars.org/repo"]
           [:snapshots [:enabled "true"]]
           [:releases [:enabled "true"]]]
          [:repository
           [:id "hara"]
           [:url "https://maven.hara.io"]
           [:snapshots [:enabled "false"]]
           [:releases [:enabled "true"]]]]
         (apply vector :dependencies
                (map coordinate->dependency (:dependencies entry)))])))

(defn generate-manifest
  "creates a manifest.mf file for the project
 
   (generate-manifest (project/project))"
  {:added "3.0"}
  [root]
  (spit (str root "/MANIFEST.MF")
        (str "Manifest-Version: 1.0\n"
             "Built-By: hara.deploy\n"
             "Created-By: hara.deploy\n"
             "Build-Jdk: " (get (System/getProperties) "java.runtime.version")  "\n"
             "Main-Class: clojure.main\n"))
  "MANIFEST.MF")

(defn generate-pom
  "generates a pom file given an entry
 
   (generate-pom {:artifact \"hara.function.task\"
                  :group \"hara\"
                 :version \"3.0.1\"}
                 \".\")"
  {:added "3.0"}
  [entry root]
  (let [output (str "META-INF/maven/"
                    (:group entry)
                    "/"
                    (:artifact entry))
        xml    (pom-xml entry)
        _ (fs/create-directory (fs/path root output))
        _ (spit (str (fs/path root output "pom.xml")) xml)
        _ (spit (str (fs/path root output "pom.properties")) (pom-properties entry))]
    [xml
     (str output "/pom.xml")
     (str output "/pom.properties")]))
