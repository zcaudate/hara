(ns hara.function.base.native
  (:require [hara.function.base.invoke :as invoke :refer [definvoke]]
            [hara.protocol.function :as protocol.function])
  (:import (java.util.function Predicate Function BiFunction
                               Supplier LongSupplier IntSupplier DoubleSupplier
                               Consumer BiConsumer LongConsumer IntConsumer UnaryOperator DoubleConsumer
                               IntUnaryOperator LongUnaryOperator DoubleUnaryOperator BinaryOperator)))

(defn fn-body-args
  "seperates elements of the function body
 
   (fn-body-args 'hello '([x] x))
   => '[hello [x] (x)]
 
   (fn-body-args '([x] x))
   => '[nil [x] (x)]"
  {:added "3.0"}
  [name? & body]
  (let [[name body]    (if (symbol? name?)
                         [name? body]
                         [nil (cons name? body)])
        [arglist body] (if (vector? (first body))
                         [(first body) (rest body)]
                         (if (= 1 (count body))
                           [(ffirst body) (rest (first body))]
                           (throw (ex-info "Cannot build body" {:input body}))))]
    [name arglist body]))

(definvoke fn-body-function
  "creates a body for type `java.util.function.Function`
 
   (fn-body-function '([x] x))
   => '(clojure.core/reify java.util.function.Function
         (toString [_] \"#native.fn([x] x)\")
         (apply [_ x] x))"
  {:added "3.0"}
  [:method {:multi protocol.function/-fn-body
            :val   :function}]
  ([body]
   (fn-body-function nil body))
  ([_ body]
   (let [[name arglist body] (fn-body-args body)]
     `(reify Function
        (~'toString ~['_] ~(str (cons arglist body)))
        (~'apply ~(vec (cons '_ arglist))
          ~@body)))))

(defmethod print-method Function
  [v w]
  (.write w (str "#native.fn" v)))

(definvoke fn-body-predicate
  "creates a body for type `java.util.function.Predicate`
 
   (fn-body-predicate '([x] (odd? x)))
   => '(clojure.core/reify java.util.function.Predicate
         (toString [_] \"#native.pred([x] (odd? x))\")
         (test [_ x] (clojure.core/boolean (do (odd? x)))))"
  {:added "3.0"}
  [:method {:multi protocol.function/-fn-body
            :val   :predicate}]
  ([body]
   (fn-body-predicate nil body))
  ([_ body]
   (let [[name arglist body] (fn-body-args body)]
     `(reify Predicate
        (~'toString ~['_] ~(str (cons arglist body)))
        (~'test ~(vec (cons '_ arglist))
         (boolean (do ~@body)))))))

(defmethod print-method Predicate
  [v w]
  (.write w (str "#native.pred" v)))

{:predicate Predicate
 :function  Function
 :bi-function BiFunction
 :consumer Consumer
 :bi-consumer BiConsumer
 :supplier  Supplier
 :unary-Operator UnaryOperator}
