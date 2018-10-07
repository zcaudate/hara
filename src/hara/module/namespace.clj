(ns hara.module.namespace
  (:require [hara.module :as module]
            [hara.function :as fn :refer [definvoke]]
            [hara.function.task :as task]
            [hara.module.namespace.common :as common]
            [hara.module.namespace.eval]
            [hara.module.namespace.resolve]))

(module/include
 (hara.module.namespace.eval      eval-ns
                                  with-ns
                                  eval-temp-ns)
 (hara.module.namespace.resolve   resolve-ns
                                  ns-vars))

(def namespace-template
  {:construct {:input    (fn [_] (.getName *ns*))
               :lookup   (fn [_ _] {})
               :env      (fn [_] {})}
   :arglists '([] [ns])
   :main      {:argcount 1}
   :params    {:print {:function false
                       :item false
                       :result false
                       :summary false}}
   :warning   {:output  :data}
   :error     {:output  :data}
   :item      {:list    (fn [_ _]   (common/ns-list))
               :pre     (fn [input] (.getName (the-ns input)))
               :post    identity
               :output  identity
               :display identity}
   :result    {:keys    {:count count}
               :ignore  empty?
               :output  identity
               :columns [{:key    :key
                          :align  :left}
                         {:key    :count
                          :length 7
                          :format "(%s)"
                          :align  :center
                          :color  #{:bold}}
                         {:key    :data
                          :align  :left
                          :length 60
                          :color  #{:yellow}}
                         {:key    :time
                          :align  :left
                          :length 10
                          :color  #{:bold}}]}
   :summary   {:aggregate {:total [:count + 0]}}})

(defmethod task/task-defaults :namespace
  [_]
  namespace-template)

(defmethod task/task-defaults :namespace.memory
  [_]
  (-> namespace-template
      (update-in [:item] merge {:output  common/group-in-memory
                                :display common/group-in-memory})
      (assoc :params    {:print {:function false
                                 :item     false
                                 :result   true
                                 :summary  true}}
             :result {:keys    {:count     count
                                :functions common/group-in-memory}
                      :output  common/group-in-memory
                      :columns [{:key    :key
                                 :align  :left}
                                {:key    :count
                                 :length 7
                                 :format "(%s)"
                                 :align  :right
                                 :color  #{:bold}}
                                {:key    :functions
                                 :align  :left
                                 :length 80
                                 :color  #{:yellow}}]}
             :summary   {:aggregate {:objects [:count + 0]
                                     :functions [:functions #(+ %1 (count %2)) 0]}})))

(defmethod task/task-defaults :namespace.count
  [_]
  (-> namespace-template
      (assoc :result  {:output  identity
                       :columns [{:key    :key
                                  :align  :left}
                                 {:key    :data
                                  :align  :left
                                  :length 80
                                  :color  #{:yellow}}]}
             :summary   {:aggregate {:total [:data (fn [v _] (inc v)) 0]}})))

(def return-keys (comp vec sort keys))

(definvoke list-aliases
  "namespace list all aliases task 

   (ns/list-aliases '[hara.module.namespace])"
  {:added "3.0"}
  [:task {:template :namespace
          :params {:title "NAMESPACE ALIASES"}
          :main   {:fn clojure.core/ns-aliases}
          :item   {:post return-keys}}])

(definvoke clear-aliases
  "removes all namespace aliases"
  {:added "3.0"}
  [:task {:template :namespace
          :params {:title "CLEAR NAMESPACE ALIASES"}
          :main {:fn common/ns-clear-aliases}}])

(definvoke list-imports
  "namespace list all imports task
 
   (ns/list-imports '[hara.module.namespace] {:return :summary})
   ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 482}
   => map?"
  {:added "3.0"}
  [:task {:template :namespace
          :params {:title "NAMESPACE IMPORTS"}
          :main {:fn clojure.core/ns-imports}
          :item {:post return-keys}}])

(definvoke list-external-imports
  "namespace list all imports task
 
   (ns/list-imports '[hara.module.namespace] {:return :summary})
   ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 482}
   => map?"
  {:added "3.0"}
  [:task {:template :namespace
          :params {:title "NAMESPACE IMPORTS"}
          :main {:fn common/ns-list-external-imports}
          :item {:post return-keys}}])

(definvoke clear-external-imports
  "removes all namespace aliases"
  {:added "3.0"}
  [:task {:template :namespace
          :params {:title "CLEAR NAMESPACE ALIASES"}
          :main {:fn common/ns-clear-external-imports}}])

(definvoke list-mappings
  "namespace list all mappings task
 
   (ns/list-mappings '[hara.module.namespace] {:return :summary})
   ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 3674}
   => map?"
  {:added "3.0"}
  [:task {:template :namespace
          :params {:title "NAMESPACE MAPPINGS"}
          :main {:fn clojure.core/ns-map}
          :item {:post return-keys}}])

(definvoke clear-mappings
  "removes all mapped vars in the namespace"
  {:added "3.0"}
  [:task {:template :namespace
          :params {:title "CLEAR NAMESPACE MAPPINGS"}
          :main {:fn common/ns-clear-mappings}}])

(definvoke list-interns
  "namespace list all interns task
 
   (ns/list-interns '[hara.module.namespace] {:return :summary})
   ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 43}
   => map?"
  {:added "3.0"}
  [:task {:template :namespace
          :params {:title "NAMESPACE INTERNS"}
          :main {:fn clojure.core/ns-interns}
          :item {:post return-keys}}])

(definvoke list-refers
  {:added "3.0"}
  [:task {:template :namespace
          :params {:title "NAMESPACE REFERS"}
          :main {:fn clojure.core/ns-refers}
          :item {:post return-keys}}])

(definvoke clear-interns
  "clears all interned vars in the namespace
 
   (ns/clear-interns)
   "
  {:added "3.0"}
  [:task {:template :namespace
          :params {:title "CLEAR NAMESPACE INTERNS"}
          :main {:fn common/ns-clear-interns}}])

(definvoke clear-refers
  {:added "3.0"}
  [:task {:template :namespace
          :params {:title "CLEAR NAMESPACE refers"}
          :main {:fn common/ns-clear-refers}}])

(definvoke list-publics
  "namespace list all publics task
 
   (ns/list-publics '[hara.module.namespace] {:return :summary})
   ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 43}
   => map?"
  {:added "3.0"}
  [:task {:template :namespace
          :params {:title "NAMESPACE PUBLICS"}
          :main {:fn clojure.core/ns-publics}
          :item {:post return-keys}}])

(definvoke list-refers
  "namespace list all refers task
 
   (ns/list-refers '[hara.module.namespace] {:return :summary})
   ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 3149}
   => map?"
  {:added "3.0"}
  [:task {:template :namespace
          :params {:title "NAMESPACE REFERS"}
          :main {:fn clojure.core/ns-refers}
          :item {:post return-keys}}])

(definvoke clear
  "namespace clear all mappings and aliases task
 
   (ns/clear #{*ns*})
   ;; { .... }
   => map?"
  {:added "3.0"}
  [:task {:template :namespace
          :params {:title "CLEAR NAMESPACE ALIASES AND MAPPINGS"}
          :main {:fn common/ns-clear}}])

(definvoke list-in-memory
  "namespace list all objects in memory task
 
   (ns/list-in-memory 'hara.module.namespace)
 
   (ns/list-in-memory '[hara.module.namespace] {:print {:result false :summary false}
                                      :return :summary})
   ;;{:errors 0, :warnings 0, :items 5, :results 5, :objects 306, :functions 22}
   => map?"
  {:added "3.0"}
  [:task {:template :namespace.memory
          :params {:title "NAMESPACE MEMORY OBJECTS"}
          :main   {:fn common/raw-in-memory}}])

(definvoke loaded?
  "namespace check if namespace is loaded task
 
   (ns/loaded? 'hara.module.namespace) => true
 
   (ns/loaded? '[hara.module.namespace])
   => map?"
  {:added "3.0"}
  [:task {:template :namespace.count
          :params {:title "NAMESPACE LOADED?"}
          :main {:fn common/ns-loaded?}}])

(definvoke reset
  "deletes all namespaces under the root namespace"
  {:added "3.0"}
  [:task {:template :namespace.memory
          :params {:title "RESET NAMESPACE"
                   :print {:item true
                           :summary true}}
          :main {:fn common/ns-delete}}])

(definvoke unmap
  "namespace unmap task
   
   (ns/unmap :args 'something)
   
   (ns/unmap 'hara.module.namespace :args '[something more])"
  {:added "3.0"}
  [:task {:template :namespace
          :params {:title "UNMAP NAMESPACE SYMBOL"}
          :arglists '([:args syms] [<ns> :args syms])
          :main {:fn common/ns-unmap}}])

(definvoke unalias
  "namespace unalias task
 
   (ns/unalias :args 'something)
 
   (ns/unalias 'hara.module.namespace :args '[something more])"
  {:added "3.0"}
  [:task {:template :namespace
          :params {:title "UNALIAS NAMESPACE SYMBOL"
                   :print {:result false
                           :summary false}}
          :arglists '([:args syms] [<ns> :args syms])
          :main {:fn common/ns-unalias}}])

(comment
  (loaded? '[hara.module.namespace])
  (unmap '[hara.module.namespace] :args '[ns])
  (list-aliases '[hara.module.namespace])
  (list-aliases 'hara.module.namespace)
  (list-in-memory '[hara.module.namespace])
  (reset '[hara.module.namespace-test])
  (list-aliases ['hara] {:print {:item true}})
  (random-test '[hara])
  (check '[*ns*])
  (hara.code/scaffold))
  
