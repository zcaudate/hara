(ns hara.function.task
  (:require [hara.data.base.nested :as nested]
            [hara.function :refer [defexecutive definvoke]]
            [hara.protocol.function :as protocol.function]
            [hara.function.task.process :as process]))

(defmulti task-defaults
  "creates default settings for task groups
 
   (task-defaults :namespace)
 
   ;;(task-defaults :project)
   "
  {:added "3.0"}
  identity)

(defmethod task-defaults :default
  [_]
  {:main {:arglists '([] [entry])}})

(declare task-display task-status)

(defexecutive Task
  "constructs a invokable Task object"
  {:added "3.0"}
  [type name main construct arglists item result summary]
  {:type    defrecord
   :tag     "task"
   :display task-display
   :invoke  process/invoke
   :status  task-status})

(defn task-status
  "displays the task-status"
  {:added "3.0"}
  [^Task task]
  (.type task))

(defn task-display
  "displays the task-body"
  {:added "3.0"}
  [^Task task]
  {:fn (symbol (.name task))})

(defn single-function-print
  "if not `:bulk`, then print function output
 
   (single-function-print {})
   => {:print {:function true}}"
  {:added "3.0"}
  [params]
  (if (and (not (:bulk params))
           (-> params :print :function nil?))
    (assoc-in params [:print :function] true)
    params))

(defn task
  "creates a task
 
   (task :namespace \"list-interns\" ns-interns)
   
   (task :namespace
         \"list-interns\"
         {:main {:fn clojure.core/ns-interns}})"
  {:added "3.0"}
  ([m]
   (map->Task m))
  ([type name arg]
   (let [[params main] (if (map? arg)
                         [arg (-> arg :main :fn)]
                         [{} arg])
         defaults     (task-defaults type)
         params       (nested/merge-nested defaults params)
         count        (or (-> params :main :argcount) 4)
         [main args?] (process/main-function main count)]
     (task (nested/merge-nested defaults
                                params
                                {:main {:fn main
                                        :args? args?}
                                 :name name
                                 :type type})))))

(defn task?
  "check if object is a task
 
   (-> (task :namespace \"list-interns\" ns-interns)
       (task?))
   => true"
  {:added "3.0"}
  [x]
  (instance? Task x))

(definvoke invoke-intern-task
  "creates a form defining a task
 
   (invoke-intern-task '-task- '{:template :namespace
                                 :main {:fn clojure.core/ns-aliases}})"
  {:added "3.0"}
  [:method {:multi protocol.function/-invoke-intern
            :val :task}]
  ([name config]
   (invoke-intern-task :task name config nil))
  ([_ name config _]
   (let [template (:template config)
         body `(task ~template ~(str name) ~config)
         arglists (or (:arglists config)
                      (-> (task-defaults template) :arglists))]
     `(doto (def ~name ~body)
        (alter-meta! merge (assoc ~config :arglists (quote ~arglists)))))))

(defmacro deftask
  "defines a top level task
   
   (deftask -list-aliases-
     {:template :namespace
      :main clojure.core/ns-aliases
      :item {:post (comp vec sort keys)}
     :doc  \"returns all aliases\"})"
  {:added "3.0"}
  [name config & body]
  (invoke-intern-task :task name config body))
