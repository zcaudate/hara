(ns hara.function.task.process-test
  (:use hara.test)
  (:require [hara.function.task.process :refer :all]
            [hara.function.task :as task]
            [hara.function.base.arg :as arg]
            [hara.module.namespace :as ns]))

^{:refer hara.function.task.process/main-function :added "3.0"}
(fact "creates a main function to be used for execution"

  (main-function ns-aliases 1)
  => (contains [arg/vargs? false])

  (main-function ns-unmap 1)
  => (contains [arg/vargs? true]))

^{:refer hara.function.task.process/select-match :added "3.0"}
(fact "returns true if selector matches with input"

  (select-match 'hara 'hara.test) => true

  (select-match 'hara 'spirit.common) => false)

^{:refer hara.function.task.process/select-inputs :added "3.0"}
(fact "selects inputs based on matches"

  (select-inputs {:item {:list (fn [_ _] ['hara.test 'spirit.common])}}
                 {}
                 {}
                 ['hara])
  => ['hara.test])

^{:refer hara.function.task.process/wrap-execute :added "3.0"}
(fact "enables execution of task with transformations")

^{:refer hara.function.task.process/wrap-input :added "3.0"}
(fact "enables execution of task with single or multiple inputs")

^{:refer hara.function.task.process/task-inputs :added "3.0"}
(fact "constructs inputs to the task given a set of parameters"

  (task-inputs (task/task :namespace "ns-interns" ns-interns)
               'hara.function.task)
  => '[hara.function.task {} {} {}]

  (task-inputs (task/task :namespace "ns-interns" ns-interns)
               {:bulk true})
  => '[hara.function.task.process-test {:bulk true} {} {}])

^{:refer hara.function.task.process/invoke :added "3.0"}
(fact "executes the task, given functions and parameters"

  (def world nil)
  
  (invoke (task/task :namespace "ns-interns" ns-interns))
  => {'world #'hara.function.task.process-test/world})
