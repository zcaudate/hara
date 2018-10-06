(ns hara.object.framework.print-test
  (:use hara.test)
  (:require [hara.object.framework.print :as print]))

^{:refer hara.object.framework.print/assoc-print-vars :added "3.0"}
(fact "helper to assoc print vars in options"

  (print/assoc-print-vars {} {:tag "hello"})
  => {:tag "hello"})

^{:refer hara.object.framework.print/format-value :added "3.0"}
(fact "formats the object into a readable string"

  (print/format-value (test.Cat. "fluffy")
                      {:tag "cat"})
  => "#cat{:name \"fluffy\"}")

^{:refer hara.object.framework.print/extend-print :added "3.0"}
(fact "extend `print-method` function for a particular class"

  (macroexpand-1 '(print/extend-print test.Cat)))

(comment
  (hara.code/import))