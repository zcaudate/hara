(ns hara.function.base.invoke-test
  (:use hara.test)
  (:require [hara.function.base.invoke :refer :all]
            [hara.protocol.function :as protocol.function])
  (:refer-clojure :exclude [fn]))

^{:refer hara.function.base.invoke/form-arglists :added "3.0"}
(fact "returns the arglists of a form"

  (form-arglists '([x] x))
  => '(quote ([x]))

  (form-arglists '(([x] x) ([x y] (+ x y))))
  => '(quote ([x] [x y])))

^{:refer hara.function.base.invoke/resolve-method :added "3.0"}
(fact "resolves a package related to a label"

  (resolve-method protocol.function/-invoke-intern
                  protocol.function/-invoke-package
                  :fn
                  +default-packages+)
  => nil^:hidden
  
  (resolve-method protocol.function/-invoke-intern
                  protocol.function/-invoke-package
                  :error
                  +default-packages+)
  => throws)

^{:refer hara.function.base.invoke/invoke-intern-method :added "3.0"}
(fact "creates a `:method` form, similar to `defmethod`"

  (defmulti -hello-multi- identity)
  (invoke-intern-method '-hello-method-
                        {:multi '-hello-multi-
                         :val :apple}
                        '([x] x)))

^{:refer hara.function.base.invoke/invoke-intern :added "3.0"}
(fact "main function to call for `definvoke`"

  (invoke-intern :method
                 '-hello-method-
                 {:multi '-hello-multi-
                  :val :apple}
                 '([x] x))^:hidden
  => '(clojure.core/let [v (clojure.core/doto
                               (def -hello-method- (clojure.core/fn -hello-method- [x] x))
                             (clojure.core/alter-meta! clojure.core/merge
                                                       {:multi -hello-multi-, :val :apple}
                                                       {:arglists (quote ([x]))}))]
        [(hara.function.base.multi/multi-add -hello-multi- :apple -hello-method-) v]))

^{:refer hara.function.base.invoke/definvoke :added "3.0"}
(fact "customisable invocation forms"

  (definvoke -another-
    [:compose {:val (partial + 10)
               :arglists '([& more])}]))

^{:refer hara.function.base.invoke/invoke-intern-fn :added "3.0"}
(fact "method body for `:fn` invoke"

  (invoke-intern-fn :fn '-fn-form- {} '([x] x))^:hidden
  => (do (def -fn-form-
           (clojure.core/fn -fn-form- [x] x))
         (clojure.core/doto (var -fn-form-)
           (clojure.core/alter-meta! clojure.core/merge {} {:arglists (quote ([x]))})))

  (definvoke -add10-
    [:fn]
    ([& args]
     (apply + 10 args)))

  (-add10- 1 2 3)
  => 16)

^{:refer hara.function.base.invoke/invoke-intern-multi :added "3.0"}
(fact "method body for `:multi` form"

  (invoke-intern-multi :multi '-multi-form- {} '([x] x))^:hidden
  => '(do ((clojure.core/deref (var clojure.core/check-valid-options)) {} :default :hierarchy)
          (clojure.core/let [v (def -multi-form-)]
            (if (clojure.core/or
                 (clojure.core/not
                  (clojure.core/and (.hasRoot v)
                                    (clojure.core/instance? clojure.lang.MultiFn (clojure.core/deref v)))) nil)
              (clojure.core/doto (def -multi-form- (new clojure.lang.MultiFn
                                                        "-multi-form-"
                                                        (clojure.core/fn -multi-form- [x] x)
                                                        :default (var clojure.core/global-hierarchy)))
                (clojure.core/alter-meta! clojure.core/merge {} {:arglists (quote ([x]))})))))

  
  (definvoke lookup-label
    [:multi {:refresh true}]
    [x] x)
  
  (definvoke lookup-label-ref
    [:method {:multi lookup-label
              :val :ref}]
    ([_] clojure.lang.Ref))
  
  (definvoke lookup-label
    
    [:method {:val :atom}]
    ([_] clojure.lang.Atom)))

^{:refer hara.function.base.invoke/invoke-intern-lookup :added "3.0"}
(fact "method body for `:lookup` form"

  (invoke-intern-lookup :lookup
                        '-lookup-form-
                        {:table {:a 1 :b 2}} nil)^:hidden
  => '(clojure.core/doto (def -lookup-form- {:a 1, :b 2})
        (clojure.core/alter-meta! clojure.core/merge {} {:arglists (quote ([k]))}))

  (definvoke ->city
    "hello there"
    [:lookup {:table {:kunming :china
                      :melbourne :australia}
              :in keyword}])
  
  (:arglists (meta #'->city))
  => '([k])

  (->city "kunming")
  => :china)

^{:refer hara.function.base.invoke/invoke-intern-compose :added "3.0"}
(fact "method body for `:compose` form"

  (invoke-intern-compose :compose
                         '-compose-form-
                         {:val '(partial + 1 2)
                          :arglists ''([& more])} nil)^:hidden
  => '(clojure.core/doto (def -compose-form- (partial + 1 2))
        (clojure.core/alter-meta! clojure.core/merge {:arglists (quote ([& more]))}))

  (definvoke ->symbol
    [:compose {:arglists '([k])
               :val (comp symbol name)}])

  (:arglists (meta #'->symbol))
  => '([k])
  
  (->symbol :hello)
  => 'hello)


^{:refer hara.function.base.invoke/invoke-intern-macro :added "3.0"}
(fact "method body for `:macro` form"

  (defn -macro-fn- [] '[(fn [x] x)
                        {:arglists ([x])}])
  
  (invoke-intern-macro :macro
                       '-macro-form-
                       {:fn '-macro-fn-
                        :args []}
                       nil)
  => '(clojure.core/doto
          (def -macro-form- (fn [x] x))
        (clojure.core/alter-meta! clojure.core/merge {} {:arglists ([x])})))

^{:refer hara.function.base.invoke/fn-body :added "3.0"}
(fact "creates the anonymous function body"

  (fn-body :function '([x] x))
  => '(clojure.core/reify java.util.function.Function
        (toString [_] "([x] x)")
        (apply [_ x] x)))

^{:refer hara.function.base.invoke/fn-body-clojure :added "3.0"}
(fact "creates the anonymous function body for a clojure fn"
  
  (fn-body-clojure '([x] x))
  => '(clojure.core/fn [x] x))

^{:refer hara.function.base.invoke/fn :added "3.0"}
(fact "macro for an extensible `fn` form"

  (fn [x] x)
  => fn?

  ^{:type :function}
  (fn [x] x)
  => java.util.function.Function

  ^{:type :predicate}
  (fn [x] true)
  => java.util.function.Predicate)

(comment
  (./code:incomplete '[hara])
  (./code:scaffold '[hara.function])
  (./code:import)
  (require 'hara.boot))

