(ns hara.test.form.listener-test
  (:use hara.test)
  (:require [hara.test.form.listener :refer :all]))

^{:refer hara.test.form.listener/summarise-verify :added "3.0"}
(comment "extract the comparison into a valid format ")

^{:refer hara.test.form.listener/summarise-evaluate :added "3.0"}
(comment "extract the form into a valid format")

(comment
  (hara.code/import))