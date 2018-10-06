(ns hara.test-test
  (:use hara.test))

^{:refer hara.test/run :added "3.0"}
(fact "runs all tests"

  (run :list)

  (run 'hara.core.base.util)
  ;; {:files 1, :thrown 0, :facts 8, :checks 18, :passed 18, :failed 0}
  => map?)

^{:refer hara.test/print-options :added "3.0"}
(fact "output options for test results"

  (print-options)
  => #{:disable :default :all :current :help}

  (print-options :default)
  => #{:print-bulk :print-failure :print-thrown}^:hidden

  (print-options :all)
  => #{:print-bulk
       :print-facts-success
       :print-failure
       :print-thrown
       :print-facts
       :print-success})

^{:refer hara.test/run-errored :added "3.0"}
(fact "runs only the tests that have errored")

^{:refer hara.test/-main :added "3.0"}
(fact "main entry point for leiningen")
