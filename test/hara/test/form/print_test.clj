(ns hara.test.form.print-test
  (:use hara.test)
  (:require [hara.test.form.print :refer :all]))

^{:refer hara.test.form.print/print-success :added "3.0"}
(comment "outputs the description for a successful test")

^{:refer hara.test.form.print/print-failure :added "3.0"}
(comment "outputs the description for a failed test")

^{:refer hara.test.form.print/print-thrown :added "3.0"}
(comment "outputs the description for a form that throws an exception")

^{:refer hara.test.form.print/print-fact :added "3.0"}
(comment "outputs the description for a fact form that contains many statements")

^{:refer hara.test.form.print/print-summary :added "3.0"}
(comment "outputs the description for an entire test run")

(comment
  (hara.code/import))
