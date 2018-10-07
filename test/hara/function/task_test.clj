(ns hara.function.task-test
  (:use hara.test)
  (:require [hara.function.task :refer :all]
            [hara.module.namespace :as namespace]))

^{:refer hara.function.task/task-defaults :added "3.0"}
(fact "creates default settings for task groups"

  (task-defaults :namespace)

  ;;(task-defaults :project)
  )

^{:refer hara.function.task/Task :added "3.0"}
(fact "constructs a invokable Task object")

^{:refer hara.function.task/task-status :added "3.0"}
(fact "displays the task-status")

^{:refer hara.function.task/task-display :added "3.0"}
(fact "displays the task-body")

^{:refer hara.function.task/single-function-print :added "3.0"}
(fact "if not `:bulk`, then print function output"

  (single-function-print {})
  => {:print {:function true}})

^{:refer hara.function.task/task :added "3.0"}
(fact "creates a task"

  (task :namespace "list-interns" ns-interns)
  
  (task :namespace
        "list-interns"
        {:main {:fn clojure.core/ns-interns}}))

^{:refer hara.function.task/task? :added "3.0"}
(fact "check if object is a task"

  (-> (task :namespace "list-interns" ns-interns)
      (task?))
  => true)

^{:refer hara.function.task/invoke-intern-task :added "3.0"}
(fact "creates a form defining a task"

  (invoke-intern-task '-task- '{:template :namespace
                                :main {:fn clojure.core/ns-aliases}})^:hidden
  => '(clojure.core/doto
         (def -task- (hara.function.task/task :namespace
                                              "-task-"
                                              {:template :namespace,
                                               :main {:fn clojure.core/ns-aliases}}))
       (clojure.core/alter-meta!
        clojure.core/merge
        (clojure.core/assoc {:template :namespace,
                             :main {:fn clojure.core/ns-aliases}}
                            :arglists (quote ([] [ns]))))))

^{:refer hara.function.task/deftask :added "3.0"}
(comment "defines a top level task"
  
  (deftask -list-aliases-
    {:template :namespace
     :main clojure.core/ns-aliases
     :item {:post (comp vec sort keys)}
     :doc  "returns all aliases"}))

(comment

  (deftask -inc-
    {:template :default
     :main {:fn clojure.core/inc}}))
