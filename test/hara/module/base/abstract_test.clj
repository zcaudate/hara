(ns hara.module.base.abstract-test
  (:use hara.test)
  (:require [hara.module.base.abstract :refer :all]))

^{:refer hara.module.base.abstract/protocol-basis :added "3.0"}
(fact "Helper function that transforms the functions in the protocol to
  the neccessary format in preparation for extend-abstract and extend-implementations"

  (defprotocol IData (-data [this]))

  (defrecord Envelope [])
  
  
  (defprotocol IVal
    (-set [this val])
    (-get [this]))

  (protocol-basis IVal '- 'pre- '-tail)
  => (contains '({:args [this], :fn pre-get-tail, :name -get}
                 {:args [this val], :fn pre-set-tail, :name -set})
               :in-any-order))

^{:refer hara.module.base.abstract/map-walk-submap :added "3.0"}
(fact "Gets a submap depending on whether it is a key or it
  is a key witthin a hashset"

  (map-walk-submap {:hello "world"} :hello)
  => "world"

  (map-walk-submap {#{:hello} "world"} :hello)
  => "world")

^{:refer hara.module.base.abstract/map-walk :added "3.0"}
(fact "Helper function for evaluation of various utility functions
  within the namespace"

  (map-walk :hello {#{:hello} (fn [k arg1] (str (name k) " world " arg1))}
            ["again"]  identity
            (fn [_ _ _] :none)
            (fn [obj func arg1] (func obj arg1)))
  => "hello world again")

^{:refer hara.module.base.abstract/protocol-default-form :added "3.0"}
(fact "creates a :default defmethod form from a protocol basis"

  (protocol-default-form '{:args [this], :fn data-env, :name -data}
                         '([this & args] (Exception. "No input")))
  => '(defmethod data-env :default [this & args] (Exception. "No input")))

^{:refer hara.module.base.abstract/protocol-multi-form :added "3.0"}
(fact "creates a defmulti form from a protocol basis"

  (protocol-multi-form '{:args [this], :fn data-env, :name -data}
                       '{#{-data} (-> % :meta :type)})
  => '(defmulti data-env (fn [this] (-> this :meta :type))))

^{:refer hara.module.base.abstract/protocol-multimethods :added "3.0"}
(fact "creates a set of defmulti and defmethods for each entry in all-basis"

  (protocol-multimethods '[{:args [this], :fn data-env, :name -data}]
                         {:defaults '([this & args] (Exception. "No input"))
                          :dispatch '(-> % :meta :type)})
  => '((defmulti data-env (fn [this] (-> this :meta :type)))
       (defmethod data-env :default [this & args] (Exception. "No input"))))

^{:refer hara.module.base.abstract/protocol-extend-type-wrappers :added "3.0"}
(fact "applies form template for simple template rewrites"

  (protocol-extend-type-wrappers '{:args [this], :fn data-env, :name -data}
                                 '{-data (process %)}
                                 '(data-env this))
  => '(process (data-env this))

  (protocol-extend-type-wrappers '{:args [this], :fn data-env, :name -data}
                                 '{-data (fn [form basis] (concat ['apply] form [[]]))}
                                 '(data-env this))
  => '(apply data-env this []))

^{:refer hara.module.base.abstract/protocol-extend-type-function :added "3.0"}
(fact "utility to create a extend-type function  with template and macros"

  (protocol-extend-type-function '{:args [this], :fn data-env, :name -data}
                                 '{-data (fn [form basis] (concat ['apply] form [[]]))})
  => '(-data [this] (apply data-env this [])))

^{:refer hara.module.base.abstract/protocol-extend-type :added "3.0"}
(fact "utility to create an extend-type form"
  (protocol-extend-type 'Type 'IProtocol
                        '[{:args [this], :fn data-env, :name -data}]
                        '{:wrappers (fn [form basis] (concat ['apply] form [[]]))})
  => '(extend-type Type IProtocol
                   (-data [this] (apply data-env this []))))

^{:refer hara.module.base.abstract/protocol-all :added "3.0"}
(fact "extends all methods on the type for a single protocol"

  (protocol-all 'Envelope
                'IData
                '{:select -
                  :suffix -env
                  :prefix nil})
  => '(let [function [(defmulti data-env (fn [this] this))]]
        (extend-type Envelope IData (-data [this] (data-env this))) function))

^{:refer hara.module.base.abstract/extend-abstract :added "3.0"}
(fact "Creates a set of abstract multimethods as well as extends a set of
  protocols to a given type"

  (extend-abstract
   Envelope [IData]
   :select -
   :suffix -env
   :prefix nil
   :wrappers   {-data (str "hello " %)}
   :dispatch   :type
   :defaults   {nil   ([this & args] (Exception. "No input"))
                -data ([this] (:hello this))})

  (data-env (map->Envelope {:hello "world"}))
  => "world"

  (-data (map->Envelope {:hello "world"}))
  => "hello world")

^{:refer hara.module.base.abstract/protocol-implementation-function :added "3.0"}
(fact "Creates a form for implementation of the protocol"

  (protocol-implementation-function (first (protocol-basis IData '- nil '-env))
                                    '{-data (fn [form basis] (concat ['apply] form [[]]))}
                                    (protocol-ns IData))
  => '(clojure.core/defn data-env [this]
        (apply hara.module.base.abstract-test/-data this [])))

^{:refer hara.module.base.abstract/protocol-ns :added "3.0"}
(fact "gets the namespace of the protocol"

  (protocol-ns IData)
  => "hara.module.base.abstract-test")

^{:refer hara.module.base.abstract/protocol-implementation :added "3.0"}
(fact "returns the protocol implementation"

  (protocol-implementation
   'IData
   {:wrappers '{-data (fn [form basis]
                        (concat ['apply] form [[]]))}})
  => '((clojure.core/defn data [this]
         (apply hara.module.base.abstract-test/-data this []))))

^{:refer hara.module.base.abstract/extend-implementations :added "3.0"}
(fact "Creates a set of implementation functions for implementation
  of protocol functionality"

  (extend-implementations
   [IData]
   :wrappers (fn [form _]
               (list 'str form " again")))

  (data (map->Envelope {:hello "world"}))
  => "hello world again")

(comment
  (hara.code/import))
