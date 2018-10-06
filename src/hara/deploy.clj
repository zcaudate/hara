(ns hara.deploy
  (:require [hara.deploy.main :as main]
            [hara.deploy.analyser :as analyser]
            [hara.io.project :as project]
            [hara.function :refer [definvoke]]
            [hara.function.task :as task]
            [hara.lib.aether :as aether]))

(defonce +main+
  {:construct {:input    (fn [_] :list)
               :lookup   (fn [_ {:keys [plan] :as project}]
                           plan)
               :env      (fn [_]
                           (let [project (project/project)]
                             (assoc project
                                    :plan   (analyser/create-plan project)
                                    :aether (aether/aether))))}
   :params    {:print {:item true
                       :result true
                       :summary true}
               :return :summary}
   :main      {:arglists '([] [pkg] [pkg params] [pkg params project] [pkg params lookup project])
               :count 4}})

(defmethod task/task-defaults :deploy
  [_]
  (merge +main+
         {:item      {:list     (fn [lookup _] (vec (sort (keys lookup))))
                      :display  (fn [data] (vec (sort (map :extension data))))}
          :result    {:keys    {:artifacts (fn [data]
                                             (let [{:keys [version group artifact]} (first data)
                                                   extensions (sort (map (comp str :extension) data))]
                                               [group version extensions]))}
                      :columns [{:key    :key
                                 :align  :left}
                                {:key    :artifacts
                                 :align  :left
                                 :length 60
                                 :color  #{:bold}}
                                {:key    :time
                                 :align  :left
                                 :length 10
                                 :format "%d ms"
                                 :color  #{:bold}}]}
          :summary  {:aggregate {}}}))

(defmethod task/task-defaults :deploy.package
  [_]
  (merge +main+
         {:item      {:list     (fn [lookup _] (vec (sort (keys lookup))))
                      :display  (juxt (comp count :results) :pom)}
          :result    {:keys    {:jar :jar
                                :pom :pom
                                :files (comp count :results)}
                      :columns [{:key    :key
                                 :align  :left}
                                {:key    :files
                                 :align  :left
                                 :format "(%d)"
                                 :length 10
                                 :color  #{:bold}}
                                {:key    :jar
                                 :align  :left
                                 :length 40
                                 :color  #{:bold}}
                                {:key    :time
                                 :align  :left
                                 :length 10
                                 :format "%d ms"
                                 :color  #{:bold}}]}
          :summary  {:aggregate {:files   [:files + 0]}}}))

(definvoke clean
  "cleans the interim directory of packages
 
   (clean :all)"
  {:added "3.0"}
  [:task {:template :deploy
          :params {:title "CLEAN ALL INTERIM FILES"}
          :main {:fn main/clean}}])

(definvoke package
  "packages files in the interim directory
 
   (package '[hara])"
  {:added "3.0"}
  [:task {:template :deploy.package
          :params {:title "PACKAGE INTERIM FILES"}
          :main {:fn main/package}}])

(definvoke install
  "installs packages to the local `.m2` repository
 
   (install '[hara])"
  {:added "3.0"}
  [:task {:template :deploy
            :params {:title "INSTALL PACKAGES"}
            :main {:fn main/install}}])

(definvoke install-secure
  "installs signed packages to the local `.m2` repository"
  {:added "3.0"}
  [:task {:template :deploy
            :params {:title "INSTALL SIGNED PACKAGES"}
            :main {:fn main/install-secure}}])

(definvoke deploy
  "deploys packages to a maven repository
 
   (deploy '[spirit]
           {:repository {:id \"hara\"
                        :url \"https://maven.hara.io\"
                         :authentication {:username \"hara\"
                                          :password \"hara\"}}})"
  {:added "3.0"}
  [:task {:template :deploy
          :params {:title "DEPLOY PACKAGES"}
          :main {:fn main/deploy}}])
