(ns hara.core.base.shorthand-test
  (:use hara.test)
  (:require [hara.core.base.shorthand :refer :all]))

^{:refer hara.core.base.shorthand/shorthand-form :added "3.0"}
(fact "Makes an expression using `sym`"

  (shorthand-form 'y '(str)) => '(str y)

  (shorthand-form 'x '((inc) (- 2) (+ 2)))
  => '(+ (- (inc x) 2) 2))

^{:refer hara.core.base.shorthand/shorthand-fn-expr :added "3.0"}
(fact "Makes a function expression out of the form"

  (shorthand-fn-expr '(+ 2))
  => '(fn [?] (+ ? 2)))

^{:refer hara.core.base.shorthand/fn-> :added "3.0"}
(fact "Constructs a function from a form representation."

  ((fn-> '(+ 10)) 10) => 20)

^{:refer hara.core.base.shorthand/call-> :added "3.0"}
(fact "Indirect call, takes `obj` and a list containing either a function,
   a symbol representing the function or the symbol `?` and any additional
   arguments. Used for calling functions that have been stored as symbols."

  (call-> 1 '(+ 2 3 4)) => 10

  (call-> 1 '(< 2)) => true

  (call-> {:a {:b 1}} '((get-in [:a :b]) = 1))
  => true)

^{:refer hara.core.base.shorthand/get-> :added "3.0"}
(fact "Provides a shorthand way of getting a return value.
 `sel` can be a function, a vector, or a value."

  (get-> {:a {:b {:c 1}}} :a) => {:b {:c 1}}

  (get-> {:a {:b {:c 1}}} [:a :b]) => {:c 1})

^{:refer hara.core.base.shorthand/eq-> :added "3.0"}
(fact "Compare if two vals are equal."

  (eq-> {:id 1 :a 1} {:id 1 :a 2} :id)
  => true

  (eq-> {:db {:id 1} :a 1}
        {:db {:id 1} :a 2} [:db :id])
  => true)

^{:refer hara.core.base.shorthand/check :added "3.0"}
(fact "checks"

  (check 2 2) => true

  (check 2 even?) => true

  (check 2 '(> 1)) => true

  (check {:a {:b 1}} '([:a :b] (= 1))) => true

  (check {:a {:b 1}} :a vector?) => false

  (check {:a {:b 1}} [:a :b] 1) => true)

^{:refer hara.core.base.shorthand/check-all :added "3.0"}
(fact "Returns `true` if `obj` satisfies all pairs of sel and chk"

  (check-all {:a {:b 1}}
             :a       #(instance? clojure.lang.IPersistentMap %)
             [:a :b]  1)
  => true)

^{:refer hara.core.base.shorthand/check-> :added "3.0"}
(fact "Shorthand ways of checking where `m` fits `prchk`"

  (check-> {:a 1} :a) => true

  (check-> {:a 1 :val 1} [:val 1]) => true

  (check-> {:a {:b 1}} [[:a :b] odd?]) => true)

^{:refer hara.core.base.shorthand/check?-> :added "3.0"}
(fact "Tests obj using prchk and returns `obj` or `res` if true"

  (check?-> :3 even?) => nil

  (check?-> 3 even?) => nil

  (check?-> 2 even?) => true

  (check?-> {:id :1} '[:id (= :1)]) => true)
