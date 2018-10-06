(ns hara.function.base.executive-test
  (:use hara.test)
  (:require [hara.function.base.executive :refer :all]))

^{:refer hara.function.base.executive/create-invoke-form :added "3.0"}
(fact "creates an invoke form given params"

  (create-invoke-form '-hello- 'obj 3)
  => '(invoke [obj a0 a1 a2] (-hello- obj a0 a1 a2)))

^{:refer hara.function.base.executive/create-executive-form :added "3.0"}
(fact "creates the form for defexecutive"

  (create-executive-form '-EXE-
                         '[name place date]
                         {:type 'deftype
                          :tag "hello"
                          :invoke   'hello-invoke
                          :display  'hello-display
                          :print    'hello-print
                          :args-number 3}
                         [])
  => '[(deftype -EXE- [name place date]
         java.lang.Object
         (toString [hello]
           (clojure.core/str "#" "hello" (hello-display hello)))
         
         clojure.lang.IFn
         (invoke [hello] (hello-invoke hello))
         (invoke [hello a0] (hello-invoke hello a0))
         (invoke [hello a0 a1] (hello-invoke hello a0 a1))
         (applyTo [hello args] (clojure.core/apply hello-invoke hello args)))
       
       (clojure.core/defmethod clojure.core/print-method (clojure.core/resolve (quote -EXE-))
         [hello writer]
         (.write writer (clojure.core/str (hello-print hello))))])

^{:refer hara.function.base.executive/defexecutive :added "3.0"}
(fact "creates an executable data type"

  (declare hello-display hello-print)
  
  (def hello-invoke (fn [this & args]
                      (str (.name this) " " (apply + args))))
  
  (defexecutive -Hello- [name place date]
    {:tag "hello"
     :invoke   hello-invoke
     :display  hello-display
     :print    hello-print})

  ((-Hello-. "hello" nil nil) 1 2 3 4 5)
  => "hello 15")
