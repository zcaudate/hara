(ns hara.deploy.main
  (:require [hara.core.encode :as encode]
            [hara.deploy.common :as common]
            [hara.deploy.package.meta :as meta]
            [hara.security.pgp :as privacy]
            [hara.security.pgp.local :as user]
            [hara.io.archive :as archive]
            [hara.io.file :as fs]
            [hara.security :as security]
            [hara.lib.aether :as aether]))

(defn package
  "given an entry, create a jar/pom package for deployment
 
   (package 'hara.function.task
            (-> (analyser/create-plan (project/project))
                (get 'hara.function.task))
            (project/project))
   => (throws)
   "
  {:added "3.0"}
  ([pkg entry project]
   (let [interim (fs/path (:root project) common/*default-interim* (str pkg))
         output  (fs/path interim common/*default-package*)
         _ (fs/create-directory output)

         results (mapv (fn [[origin out]]
                         (fs/copy-single origin
                                         (fs/path output out)
                                         {:options #{:replace-existing}})
                         out)
                       (:files entry))
         manifest (meta/generate-manifest output)
         [xml & pom]  (meta/generate-pom entry output)
         pom-name (str (:artifact entry)
                       "-"
                       (:version entry) ".pom.xml")
         _        (spit (fs/path interim pom-name) xml)
         jar-name (str (:artifact entry)
                       "-"
                       (:version entry) ".jar")
         jar-path (fs/path interim jar-name)
         _        (fs/delete jar-path)
         jar      (archive/open jar-path)
         _        (doto jar
                    (archive/archive output)
                    (.close))]
     {:package pkg
      :jar jar-name
      :pom pom-name
      :interim (str interim)
      :results (cons manifest (concat pom results))}))
  ([pkg params lookup {:keys [plan] :as project}]
   (let [entry (get plan pkg)]
     (package pkg entry project))))

(defn infer
  "given an entry, infer the jar/pom package for deployment
 
   (infer 'hara.function.task
            (-> (analyser/create-plan (project/project))
                (get 'hara.function.task))
            (project/project))
   => (throws)
   #_(contains {:package 'hara.function.task
                :jar string?
                :pom string?})"
  {:added "3.0"}
  [pkg entry project]
  (let [interim (fs/path (:root project) common/*default-interim* (str pkg))
        jar-name (str (:artifact entry)
                      "-"
                      (:version entry) ".jar")
        pom-name (str (:artifact entry)
                      "-"
                      (:version entry) ".pom.xml")]
    {:package pkg
     :jar jar-name
     :pom pom-name
     :interim (str interim)}))

(defn clean
  "cleans the interim directory
 
   (clean 'hara.function.task
          {}
         {}
          (project/project))"
  {:added "3.0"}
  [pkg {:keys [simulate] :as params} lookup {:keys [plan] :as project}]
  (let [interim (fs/path (:root project) common/*default-interim* (str pkg))]
    (fs/delete interim {:simulate simulate})))

(defn install
  "installs a package to the local `.m2` repository
 
   (install 'hara.function.task
            {}
           {}
            (assoc (project/project)
                   :plan (analyser/create-plan (project/project))
                   :aether (aether/aether)))"
  {:added "3.0"}
  [pkg {:keys [skip bulk] :as  params} lookup {:keys [plan aether] :as project}]
  (let [process (if skip infer package)
        entry  (get plan pkg)
        rep    (select-keys entry [:group :artifact :version])
        {:keys [jar pom interim]} (process pkg entry project)]
    (aether/install-artifact aether
                             rep
                             {:artifacts [{:file (fs/path interim jar)
                                           :extension "jar"}
                                          {:file (fs/path interim pom)
                                           :extension "pom"}]
                              :print {:title (not bulk)
                                      :timing (not bulk)}})))

(defn sign-file
  "signs a file with gpg
 
   (sign-file {:file \"project.clj\" :extension \"clj\"}
              {:signing (-> hara.security.pgp.local/LEIN-PROFILE
                           slurp
                            read-string
                            :user
                            :signing
                            :gpg-key)})"
  {:added "1.2"}
  ([{:keys [file extension]}
    {:keys [signing suffix ring-file]
     :or {suffix "asc"
          ring-file user/GNUPG-SECRET}}]
   (let [output (str file "." suffix)
         output-ex (str extension "." suffix)]
     (privacy/sign file output ring-file signing)
     {:file output
      :extension output-ex})))

(defn add-authentication
  "decrypts credentials.gpg and inserts the right authentication
 
   (add-authentication {:id \"clojars\"}
                       {})"
  {:added "1.2"}
  [{:keys [id] :as repository}
   {:keys [manual ring-file cred-file]
    :or {cred-file user/LEIN-CREDENTIALS-GPG
         ring-file user/GNUPG-SECRET}}]
  (cond manual
        (assoc repository :authentication manual)

        :else
        (if (and (fs/exists? cred-file)
                 (fs/exists? ring-file))
          (let [auth-map (read-string (privacy/decrypt cred-file ring-file))
                auth (->> auth-map
                          (filter (fn [[k _]]
                                    (cond (string? k)
                                          (= id k)

                                          (instance? java.util.regex.Pattern k)
                                          (re-find k id))))
                          first
                          second)]
            (assoc repository :authentication auth))
          repository)))

(defn create-digest
  "creates a digest given a file and a digest type
 
   (create-digest \"MD5\"
                  \"md5\"
                  {:file \"project.clj\"
                   :extension \"clj\"})
   => {:file \"project.clj.md5\",
       :extension \"clj.md5\"}"
  {:added "1.2"}
  [algorithm suffix {:keys [file extension] :as artifact}]
  (let [content (-> (fs/read-all-bytes file)
                    (security/digest "MD5")
                    (encode/to-hex))
        file (str file "." suffix)
        extension (str extension "." suffix)
        _ (spit file content)]
    {:file file :extension extension}))

(defn add-digest
  "adds MD5 and SHA1 digests to all artifacts
 
   (add-digest [{:file \"project.clj\",
                 :extension \"clj\"}])
   => [{:file \"project.clj.md5\", :extension \"clj.md5\"}
       {:file \"project.clj\", :extension \"clj\"}]"
  {:added "1.2"}
  [artifacts]
  (concat (mapv (partial create-digest "MD5" "md5") artifacts)
          artifacts))

(defn install-secure
  "installs a package to the local `.m2` repository with signing
 
   (install-secure 'hara.function.task
                   {}
                  {}
                   (assoc (project/project)
                          :plan (analyser/create-plan (project/project))
                          :aether (aether/aether)))"
  {:added "3.0"}
  [pkg {:keys [skip repository bulk] :as  params} lookup {:keys [plan aether] :as project}]
  (let [process (if skip infer package)
        entry  (get plan pkg)
        rep (select-keys entry [:group :artifact :version])
        {:keys [jar pom interim]} (process pkg entry project)
        artifacts  [{:file (fs/path interim jar)
                     :extension "jar"}
                    {:file (fs/path interim pom)
                     :extension "pom"}]
        signing  (-> user/LEIN-PROFILE
                     slurp
                     read-string
                     (get-in [:user :signing :gpg-key]))
        artifacts (cond-> artifacts
                    signing (->> (map #(sign-file % {:signing signing}))
                                 (concat artifacts))
                    :then   (add-digest))]
    (aether/install-artifact aether
                             rep
                             {:artifacts artifacts
                              :print {:title (not bulk)
                                      :timing (not bulk)}})))

(defn deploy
  "deploys a package 
 
   (deploy 'hara.function.task
           {:repository {:id \"hara\"
                        :url \"https://maven.hara.io\"
                         :authentication {:username \"hara\"
                                          :password \"hara\"}}}
           {}
           (assoc (project/project)
                  :plan (analyser/create-plan (project/project))
                  :aether (aether/aether)))"
  {:added "3.0"}
  [pkg {:keys [skip repository id authentication bulk] :as params} lookup {:keys [plan aether] :as project}]
  (let [id      (or id "clojars")
        process (if skip infer package)
        entry  (get plan pkg)
        rep (select-keys entry [:group :artifact :version])
        {:keys [jar pom interim]}  (process pkg entry project)
        artifacts  [{:file (fs/path interim jar)
                     :extension "jar"}
                    {:file (fs/path interim pom)
                     :extension "pom"}]
        repository (or repository
                       (->> (:repositories aether)
                            (filter #(-> % :id (= id)))
                            first))
        repository (add-authentication repository {:manual authentication})
        signing     (if (fs/exists? user/LEIN-PROFILE)
                      (-> user/LEIN-PROFILE
                          slurp
                          read-string
                          (get-in [:user :signing :gpg-key])))
        artifacts (cond-> artifacts
                    signing (->> (map #(sign-file % {:signing signing}))
                                 (concat artifacts)))]
    (aether/deploy-artifact aether
                            rep
                            {:artifacts artifacts
                             :repository repository
                             :print {:title (not bulk)
                                     :timing (not bulk)}})))
