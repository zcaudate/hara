(ns hara.module.namespace.eval-test
  (:use hara.test)
  (:require [hara.module.namespace.eval :refer :all]))

^{:refer hara.module.namespace.eval/eval-ns :added "3.0"}
(fact "Evaluates a list of forms in an existing namespace"
  (eval-ns 'hara.core.base.check
           '[(long? 1)])
  => true)

^{:refer hara.module.namespace.eval/with-ns :added "3.0"}
(fact "Evaluates `body` forms in an existing namespace given by `ns`."

  (require '[hara.core.base.check])
  (with-ns 'hara.core.base.check
    (long? 1)) => true)

^{:refer hara.module.namespace.eval/eval-temp-ns :added "3.0"}
(fact "Evaluates a list of forms in a temporary namespace"
  (eval-temp-ns
   '[(def  inc1 inc)
     (defn inc2 [x] (+ 1 x))
     (-> 1 inc1 inc2)])
  => 3

  "All created vars will be destroyed after evaluation."

  (resolve 'inc1) => nil)

^{:refer hara.module.namespace.eval/with-temp-ns :added "3.0"}
(fact "Evaluates `body` forms in a temporary namespace."

  (with-temp-ns
    (def  inc1 inc)
    (defn inc2 [x] (+ 1 x))
    (-> 1 inc1 inc2))
  => 3

  "All created vars will be destroyed after evaluation."

  (resolve 'inc1) => nil)