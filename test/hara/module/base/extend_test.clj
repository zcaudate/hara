(ns hara.module.base.extend-test
  (:use hara.test)
  (:require [hara.module.base.extend :refer :all]))

^{:refer hara.module.base.extend/extend-single :added "3.0"}
(fact "Transforms a protocol template into an extend-type expression"

  (extend-single 'Type
                 'IProtocol
                 '[(op [x y] (% x y))]
                 '[op-object])
  => '(clojure.core/extend-type Type IProtocol (op [x y] (op-object x y))))

^{:refer hara.module.base.extend/extend-entry :added "3.0"}
(fact "Helper function for extend-all "

  (extend-entry 'Magma
                '[(op ([x y] (% x y)))]
                '[Number        [op-number]])
  => '[(clojure.core/extend-type Number Magma (op ([x y] (op-number x y))))])

^{:refer hara.module.base.extend/extend-all :added "3.0"}
(fact "Transforms a protocl template into multiple extend-type expresions"

  (macroexpand-1
   '(extend-all Magma
                [(op ([x y] (% x y)))]

                Number        [op-number]
                [List Vector] [op-list]))
  => '(do (clojure.core/extend-type Number Magma (op ([x y] (op-number x y))))
          (clojure.core/extend-type List Magma (op ([x y] (op-list x y))))
          (clojure.core/extend-type Vector Magma (op ([x y] (op-list x y))))))

(comment

  (hara.code/import))