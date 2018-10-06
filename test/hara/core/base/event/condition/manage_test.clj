(ns hara.core.base.event.condition.manage-test
  (:use hara.test)
  (:require [hara.core.base.event.condition.manage :refer :all]))

^{:refer hara.core.base.event.condition.manage/manage-apply :added "3.0"}
(fact "helper function to manage-condition")

^{:refer hara.core.base.event.condition.manage/manage-condition :added "3.0"}
(fact "allows conditionals to be run with :choose and :catch options")

(comment
  (hara.code/import))