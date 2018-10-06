(ns hara.function.base.multi-test
  (:use hara.test)
  (:require [hara.function.base.multi :refer :all]
            [hara.protocol.string :as string]))

^{:refer hara.function.base.multi/multi? :added "3.0"}
(fact "returns `true` if `obj` is a multimethod"

  (multi? print-method) => true
  
  (multi? println) => false)

^{:refer hara.function.base.multi/multi-clone :added "3.0"}
(fact "creates a multimethod from an existing one"

  (defmulti hello :type)
  
  (defmethod hello :a
    [m] (assoc m :a 1))

  (def world (multi-clone hello "world"))
  
  (defmethod world :b
    [m] (assoc m :b 2))
  
  (world {:type :b})
  => {:type :b :b 2}
  
  ;; original method should not be changed
  (hello {:type :b})
  => (throws))

^{:refer hara.function.base.multi/multi-keys :added "3.0"}
(fact "returns all keys for a given multimethod"

  (multi-keys world)
  => #{:a :b})

^{:refer hara.function.base.multi/multi-has? :added "3.0"}
(fact "returns `true` if the multimethod contains a value for dispatch"

  (multi-has? print-method Class)
  => true)

^{:refer hara.function.base.multi/multi-list :added "3.0"}
(fact "returns all entries in the multimethod"

  (multi-list world)
  => (satisfies [:a :b]
                (comp vec sort keys)))

^{:refer hara.function.base.multi/multi-get :added "3.0"}
(fact "returns all entries in the multimethod"

  (multi-get world :b)
  => fn?)

^{:refer hara.function.base.multi/multi-remove :added "3.0"}
(fact "removes an entry"

  (multi-remove world :b)
  => fn?)

^{:refer hara.function.base.multi/multi-match? :added "3.0"}
(fact "checks if the multi dispatch matches the arguments"

  (multi-match? (.dispatchFn string/-from-string) (fn [_ _ _]))
  => true

  (multi-match? (.dispatchFn string/-from-string) (fn [_]) true)
  => (throws))

^{:refer hara.function.base.multi/multi-add :added "3.0"}
(fact "adds an entry to the multimethod"

  (multi-add world :c (fn [m] (assoc m :c 3)))
  => world)

(comment
  (hara.module.namespace/reset '[hara.time])
  (hara.module.namespace/reset '[hara.data])
  (hara.module.namespace/reset '[hara])
  (hook/unpatch #'clojure.core/defmethod)
  (multi-match? (fn
                  ([x y z]))
                (fn [x]))

  (multi-match? identity
                (fn [x])
                true)
  
  (multi-match? (fn
                  ([x y z]))
                (fn [x & more]))

  (multi-match? (fn
                  ([x & more]))
                (fn [x]))

  ()
  (def multi (fn
               ([x y z])))
  
  (arg-count a)(3 2)
  (varg-count a)nil

  (def b (fn
           ([x & more])
           ))

  (arg-count b) ()
  (varg-count b) => 1
  
  (args-match? (fn
                 ([x y])
                 ([x y z]))

               (fn
                 ([x & more])
                 ))
  
  
  (args-match? (fn
                 ([x y])
                 ([x y z]))

               (fn
                 ([x])
                 ))
  => false)
