(ns hara.module.namespace.common-test
  (:use hara.test)
  (:require [hara.module.namespace.common :refer :all])
  (:refer-clojure :exclude [ns-unmap ns-unalias]))

^{:refer hara.module.namespace.common/ns-unalias :added "3.0"}
(comment "removes given aliases in namespaces"

  (ns-unalias *ns* '[hello world])
  => '[hello world])

^{:refer hara.module.namespace.common/ns-unmap :added "3.0"}
(comment "removes given mapped elements in namespaces"

  (ns-unmap *ns* '[hello world])
  => '[hello world])

^{:refer hara.module.namespace.common/ns-clear-aliases :added "3.0"}
(comment "removes all namespace aliases"

  ;; require clojure.string
  (require '[clojure.string :as string])
  => nil

  ;; error if a new namespace is set to the same alias
  (require '[clojure.set :as string])
  => (throws) ;;  Alias string already exists in namespace

  ;; clearing all aliases
  (ns-clear-aliases)

  (ns-aliases *ns*)
  => {}

  ;; okay to require
  (require '[clojure.set :as string])
  => nil)

^{:refer hara.module.namespace.common/ns-list-external-imports :added "3.0"}
(fact "lists all external imports"

  (import java.io.File)
  (ns-list-external-imports *ns*)
  => '(File))

^{:refer hara.module.namespace.common/ns-clear-external-imports :added "3.0"}
(fact "clears all external imports"

  (ns-clear-external-imports *ns*)
  (ns-list-external-imports *ns*)
  => ())

^{:refer hara.module.namespace.common/ns-clear-mappings :added "3.0"}
(comment "removes all mapped vars in the namespace"

  ;; require `join`
  (require '[clojure.string :refer [join]])

  ;; check that it runs
  (join ["a" "b" "c"])
  => "abc"

  ;; clear mappings
  (ns-clear-mappings)

  ;; the mapped symbol is gone
  (join ["a" "b" "c"])
  => (throws) ;; "Unable to resolve symbol: join in this context"
  )

^{:refer hara.module.namespace.common/ns-clear-interns :added "3.0"}
(fact "clears all interns in a given namespace"
  
  (ns-clear-interns *ns*))

^{:refer hara.module.namespace.common/ns-clear-refers :added "3.0"}
(fact "clears all refers in a given namespace")

^{:refer hara.module.namespace.common/ns-clear :added "3.0"}
(comment "clears all mappings and aliases in a given namespace"

  (ns-clear))

^{:refer hara.module.namespace.common/group-in-memory :added "3.0"}
(fact "creates human readable results from the class list"

  (group-in-memory ["hara.code$add$1" "hara.code$add$2"
                    "hara.code$sub$1" "hara.code$sub$2"])
  => '[[add 2]
       [sub 2]])

^{:refer hara.module.namespace.common/raw-in-memory :added "3.0"}
(fact "returns a list of keys representing objects"

  (raw-in-memory 'hara.code)
  ;;("hara.code$eval6411" "hara.code$eval6411$loading__5569__auto____6412")
  => coll?)

^{:refer hara.module.namespace.common/ns-in-memory :added "3.0"}
(fact "retrieves all the clojure namespaces currently in memory"
  
  (ns-in-memory 'hara.code)
  ;;[[EVAL 2]]
  => coll?)

^{:refer hara.module.namespace.common/ns-loaded? :added "3.0"}
(fact "checks if the namespaces is currently active"

  (ns-loaded? 'hara.module.namespace.common)
  => true)

^{:refer hara.module.namespace.common/ns-delete :added "3.0"}
(comment "clears all namespace mappings and remove namespace from clojure environment"

  (ns-delete 'hara.module.namespace)

  (ns-delete 'hara.module.namespace-test))

^{:refer hara.module.namespace.common/ns-list :added "3.0"}
(fact "returns all existing clojure namespaces"

  (ns-list)
  => coll?)

(comment
  (hara.code/import))
