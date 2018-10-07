(ns hara.module.namespace-test
  (:use hara.test)
  (:require [hara.module.namespace :as ns]
            [hara.core.base.result :as result]
            [hara.function :refer [definvoke]]))

^{:refer hara.module.namespace/list-aliases :added "3.0"}
(fact "namespace list all aliases task"

  (ns/list-aliases '[hara.module.namespace]))

^{:refer hara.module.namespace/clear-aliases :added "3.0"}
(comment "removes all namespace aliases"^:hidden

  ;; require clojure.string
  (require '[clojure.string :as string])
  => nil

  ;; error if a new namespace is set to the same alias
  (require '[clojure.set :as string])
  => (throws) ;;  Alias string already exists in namespace

  ;; clearing all aliases
  (clear-aliases)

  (ns-aliases *ns*)
  => {}

  ;; okay to require
  (require '[clojure.set :as string])
  => nil)

^{:refer hara.module.namespace/list-imports :added "3.0"}
(fact "namespace list all imports task"

  (ns/list-imports '[hara.module.namespace] {:return :summary})
  ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 482}
  => map?)

^{:refer hara.module.namespace/list-external-imports :added "3.0"}
(fact "lists all external imports")

^{:refer hara.module.namespace/clear-external-imports :added "3.0"}
(fact "clears all external imports")

^{:refer hara.module.namespace/list-mappings :added "3.0"}
(fact "namespace list all mappings task"

  (ns/list-mappings '[hara.module.namespace] {:return :summary})
  ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 3674}
  => map?)

^{:refer hara.module.namespace/clear-mappings :added "3.0"}
(comment "removes all mapped vars in the namespace"^:hidden

  ;; require `join`
  (require '[clojure.string :refer [join]])

  ;; check that it runs
  (join ["a" "b" "c"])
  => "abc"

  ;; clear mappings
  (ns/clear-mappings)

  ;; the mapped symbol is gone
  (join ["a" "b" "c"])
  => (throws) ;; "Unable to resolve symbol: join in this context"
  )

^{:refer hara.module.namespace/list-interns :added "3.0"}
(fact "namespace list all interns task"

  (ns/list-interns '[hara.module.namespace] {:return :summary})
  ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 43}
  => map?)

^{:refer hara.module.namespace/clear-interns :added "3.0"}
(comment "clears all interned vars in the namespace"

  (ns/clear-interns))

^{:refer hara.module.namespace/clear-refers :added "3.0"}
(fact "clears all refers in a namespace")

^{:refer hara.module.namespace/list-publics :added "3.0"}
(fact "namespace list all publics task"

  (ns/list-publics '[hara.module.namespace] {:return :summary})
  ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 43}
  => map?)

^{:refer hara.module.namespace/list-refers :added "3.0"}
(fact "namespace list all refers task"

  (ns/list-refers '[hara.module.namespace] {:return :summary})
  ;;{:errors 0, :warnings 0, :items 5, :results 5, :total 3149}
  => map?)

^{:refer hara.module.namespace/clear :added "3.0"}
(comment "namespace clear all mappings and aliases task"

  (ns/clear #{*ns*})
  ;; { .... }
  => map?)

^{:refer hara.module.namespace/list-in-memory :added "3.0"}
(fact "namespace list all objects in memory task"

  (ns/list-in-memory 'hara.module.namespace)

  (ns/list-in-memory '[hara.module.namespace] {:print {:result false :summary false}
                                     :return :summary})
  ;;{:errors 0, :warnings 0, :items 5, :results 5, :objects 306, :functions 22}
  => map?)

^{:refer hara.module.namespace/loaded? :added "3.0"}
(fact "namespace check if namespace is loaded task"

  (ns/loaded? 'hara.module.namespace) => true

  (ns/loaded? '[hara.module.namespace])
  => map?)

^{:refer hara.module.namespace/reset :added "3.0"}
(comment "deletes all namespaces under the root namespace"^:hidden

  (ns/reset 'hara)

  (ns/reset '[hara.module.namespace]))

^{:refer hara.module.namespace/unmap :added "3.0"}
(comment "namespace unmap task"
  
  (ns/unmap :args 'something)
  
  (ns/unmap 'hara.module.namespace :args '[something more]))

^{:refer hara.module.namespace/unalias :added "3.0"}
(comment "namespace unalias task"

  (ns/unalias :args 'something)

  (ns/unalias 'hara.module.namespace :args '[something more]))

(definvoke check
  "check for namespace task group"
  {:added "3.0"}
  [:task {:template :namespace
          :main {:fn (fn [input] (result/result {:status :return
                                                 :data [:ok]}))}
          :params {:title "CHECK (task::namespace)"
                   :print {:item true
                           :result true
                           :summary true}}
          :item   {:output  :data}
          :result {:keys    nil
                   :output  :data
                   :columns [{:key    :id
                              :align  :left}
                             {:key    :data
                              :align  :left
                              :length 80
                              :color  #{:yellow}}]}
          :summary nil}])
  

(definvoke random-test
  "check for namespace task group"
  {:added "3.0"}
  [:task {:template :namespace
          :params {:title "RANDOM TEST (task::namespace)"
                   :print {:item true
                           :result true
                           :summary true}}
          :main {:fn (fn [input]
                       (if (< 0.5 (rand))
                         (result/result {:status ((fn [] (rand-nth [:info :warn :error :critical])))
                                         :data   :message})
                         (result/result {:status ((fn [] (rand-nth [:return :highlight])))
                                         :data   (vec (range (rand-int 40)))})))}}])
  
(comment
  (hara.code/import {:write true})
  
  (random-test '[hara])
  (check '[hara]))
