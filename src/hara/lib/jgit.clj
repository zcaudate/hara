(ns hara.lib.jgit
  (:require [hara.module :as module]
            [hara.data.base.map :as map]
            [hara.object :as object]
            [hara.lib.jgit.api.command :as command]
            [hara.lib.jgit.api.difference :deps true]
            [hara.lib.jgit.api.repository :as repository]
            [hara.lib.jgit.interop :deps true])
  (:import (org.eclipse.jgit.api Git)))

(module/include (hara.lib.jgit.api.repository
            repository repository? list-commits list-files resolve-id raw blob)

           (hara.lib.jgit.api.difference
            list-difference list-file-changes))

(defonce ^:dynamic *dir* nil)

(defn git-help
  "list all commands for the git interface
 
   (git-help (command/git-all-commands))
   => [:add :apply :archive :blame :branch :checkout
       :cherry :clean :clone :commit :describe :diff :fetch
       :gc :init :log :ls :merge :name :notes :pull :push
       :rebase :reflog :remote :reset :revert :rm :stash
       :status :submodule :tag]"
  {:added "3.0"}
  [all-commands]
  (let [out (-> all-commands keys sort vec)]
    (println "\nSubtasks for git are:\n\n")
    (println out)
    out))

(defn git-command-help
  "list all options for a given command
 
   (-> (git-command-help ((first (command/command (command/git-all-commands)
                                                  [:push]))
                          (Git. (repository))))
       keys
       sort)
   => [:add :atomic :dry-run :force :output-stream
       :progress-monitor :push-all :push-options
       :push-tags :receive-pack :ref-lease-specs
      :ref-specs :remote :thin]"
  {:added "3.0"}
  [cmd]
  (let [opts (->> (command/command-options cmd)
                  (map/map-vals command/command-input))]
    (println "Options are: " opts)
    opts))

(defn- wrap-help [f]
  (fn  [cmd inputs]
    (if (some #{:? :help} inputs)
      (git-command-help cmd)
      (f cmd inputs))))

(defn- wrap-result [f]
  (fn [cmd inputs]
    (let [res (->> (filter #(not= :& %) inputs)
                   (f cmd))]
      (if (some #{:&} inputs)
        res
        (object/to-data res)))))

(defn- run-base [cmd inputs]
  (-> ^java.util.concurrent.Callable (command/command-initialize-inputs cmd inputs)
      (.call)))

(defn run-command
  "runs the command given the type and directory
 
   (run-command (command/command (command/git-all-commands)
                                 [:status])
               \".\")"
  {:added "3.0"}
  [pair dir]
  (if (vector? pair)
    (let [[ele inputs] pair
          cmd (if (-> ele :modifiers :static)
                (ele)
                (ele (Git. (repository/repository dir))))]

      ((-> run-base
           wrap-result
           wrap-help) cmd inputs))
    pair))

(defn git
  "top level form for manipulating 
 
   (git)
   ;;=> [:add ... :tag]
 
   (git :pwd)
   ;;=> \"/Users/chris/Development/zcaudate/lucidity\"
 
   (git :init :?)
   => (contains {:bare anything :directory java.lang.String, :git-dir java.lang.String})
   
   (git :rm :?)
   => (contains {:filepattern [java.lang.String], :cached anything})
 
   (-> (git :status) keys sort)
   => [:added :changed :conflicting
       :conflicting-stage-state :ignored-not-in-index
       :missing :modified :removed :uncommitted-changes
       :untracked :untracked-folders]
 
   (first (git :branch :list))
   ;; => {:name \"refs/heads/master\",
   ;;     :object-id \"c8fa557ce252dffdfbdd5c1884cf87d41c0f222f\",
   ;;     :storage \"LOOSE\",
   ;;     :peeled? false,
   ;;     :symbolic? false}
   "
  {:added "3.0"}
  ([] (git :help))
  ([dir? & args]
   (let [all-commands (command/git-all-commands)
         curr (System/getProperty "user.dir")
         [dir [c & cs :as args]] (cond (keyword? dir?)
                                       [(or *dir* curr)
                                        (cons dir? args)]

                                       :else
                                       [(do (alter-var-root #'*dir* (fn [x] dir?))
                                            dir?)
                                        args])]
     (cond (= :help c)
           (git-help all-commands)

           (= :cd c)
           (alter-var-root #'*dir* (fn [x] (first cs)))

           (= :pwd c)
           (or *dir* curr)

           :else
           (-> (command/command all-commands args)
               (run-command dir))))))
