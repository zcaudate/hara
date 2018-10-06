(ns hara.print.base.progress-test
  (:use hara.test)
  (:require [hara.print.base.progress :refer :all]))

^{:refer hara.print.base.progress/replace-center :added "3.0"}
(fact "replaces the center of the background with text"

  (replace-center "=================" " hello ")
  => "===== hello =====")

^{:refer hara.print.base.progress/progress-bar-string :added "3.0"}
(fact "converts a progress percentage to a string"

  (progress-bar-string 50 100 (:bar +progress-defaults+))
  => "===================== 50/100                      ")

^{:refer hara.print.base.progress/progress-spinner-string :added "3.0"}
(fact "converts a progress to a spinner string"

  (progress-spinner-string 9 20)
  => "-")

^{:refer hara.print.base.progress/progress-eta :added "3.0"}
(fact "calculates the estimated time left for the task"

  (progress-eta 100 90 90)
  => 10)

^{:refer hara.print.base.progress/progress :added "3.0"}
(fact "creates a structure representing progress"

  (-> (progress) :state deref)
  => (contains {:total 100, :current 0, :label ""}))

^{:refer hara.print.base.progress/progress-string :added "3.0"}
(fact "creates a string representation of the current progress"

  (progress-string (-> @(:state (progress))
                       (update :update-time + 10)
                       (update :current + 9))
                   +progress-defaults+)
  => "[=====                 9/100                       ] 101s -")

^{:refer hara.print.base.progress/progress-update :added "3.0"}
(fact "updates the progress meter"
  
  (progress-update (progress) 10))

^{:refer hara.print.base.progress/progress-test :added "3.0"}
(fact "demo for how progress should work"

  (progress-test))

