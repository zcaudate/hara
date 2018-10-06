(ns hara.protocol.function)

(defmulti -invoke-intern
  "extendable function for loading invoke form constructors
 
   (-invoke-intern :fn '-hello- nil '([x] x))"
  {:added "3.0"}
  (fn ([label name config body] label)))

(defmulti -invoke-package
  "extendable function for loading invoke-intern types"
  {:added "3.0"}
  identity)

(defmulti -fn-body
  "multimethod for defining anonymous function body
 
   (-fn-body :clojure '([x] x))
   => '(clojure.core/fn [x] x)"
  {:added "3.0"}
  (fn ([label body] label)))

(defmulti -fn-package
  "extendable function for loading fn-body types"
  {:added "3.0"}
  identity)
