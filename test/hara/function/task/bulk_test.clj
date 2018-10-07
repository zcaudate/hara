(ns hara.function.task.bulk-test
  (:use hara.test)
  (:require [hara.function.task.bulk :refer :all]))

^{:refer hara.function.task.bulk/bulk-items :added "3.0"}
(fact "processes each item given a input")

^{:refer hara.function.task.bulk/bulk-warnings :added "3.0"}
(fact "outputs warnings that have been processed")

^{:refer hara.function.task.bulk/bulk-errors :added "3.0"}
(fact "outputs errors that have been processed")

^{:refer hara.function.task.bulk/prepare-columns :added "3.0"}
(fact "prepares columns for printing"

  (prepare-columns [{:key :name}
                    {:key :data}]
                   [{:name "Chris"
                     :data "1"}
                    {:name "Bob"
                     :data "100"}])
  => [{:key :name, :id :name, :length 7}
      {:key :data, :id :data, :length 5}])

^{:refer hara.function.task.bulk/bulk-results :added "3.0"}
(fact "outputs results that have been processed")

^{:refer hara.function.task.bulk/bulk-summary :added "3.0"}
(fact "outputs summary of processed results")

^{:refer hara.function.task.bulk/bulk-package :added "3.0"}
(fact "packages results for return")

^{:refer hara.function.task.bulk/bulk :added "3.0"}
(fact "process and output results for a group of inputs")

(comment
  (hara.module.namespace/check [*ns*] {:return :items :package :vector})
  (hara.module.namespace/check #{'hara.code} {:return :items :package :vector})
  (hara.module.namespace/random-test ['hara.code] {:print {:item true :result true}})
  (hara.module.namespace/list-aliases ['hara.code] {:print {:result true
                                                     :summary true} 
                                             :sort-by :count
                                             :package :vector})
  (hara.module.namespace/list-aliases 'hara.code {:package :map})
  (hara.module.namespace/list-aliases 'hara.code {:package :map})
  (hara.module.namespace/list-aliases ['hara.code] {:package :map}))
