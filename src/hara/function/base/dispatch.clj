(ns hara.function.base.dispatch
  (:require [hara.function.base.arg :as arg]))

;; ## Calling Conventions
;;
;; Adds more flexibility to how functions can be called.
;; `call` adds a level of indirection and allows the function
;; to not be present, returning nil instead. `msg` mimicks the way
;; that object-orientated languages access their functions.
;;

(defn invoke
  "Executes `(f v1 ... vn)` if `f` is not nil
 
   (invoke nil 1 2 3) => nil
 
   (invoke + 1 2 3) => 6"
  {:added "3.0"}
  ([f] (if-not (nil? f) (f)))
  ([f v1] (if-not (nil? f) (f v1)))
  ([f v1 v2] (if-not (nil? f) (f v1 v2)))
  ([f v1 v2 v3] (if-not (nil? f) (f v1 v2 v3)))
  ([f v1 v2 v3 v4] (if-not (nil? f) (f v1 v2 v3 v4)))
  ([f v1 v2 v3 v4 & vs] (if-not (nil? f) (apply f v1 v2 v3 v4 vs))))

(defn call
  "like `invoke` but reverses the function and first argument
 
   (call 2) => 2
   
   (call 2 + 1 2 3) => 8"
  {:added "3.0"}
  ([obj] obj)
  ([obj f] (if (nil? f) obj (f obj)))
  ([obj f v1] (if (nil? f) obj (f obj v1)))
  ([obj f v1 v2] (if (nil? f) obj (f obj v1 v2)))
  ([obj f v1 v2 v3] (if (nil? f) obj (f obj v1 v2 v3)) )
  ([obj f v1 v2 v3 v4] (if (nil? f) obj (f obj v1 v2 v3 v4)))
  ([obj f v1 v2 v3 v4 & vs] (if (nil? f) obj (apply invoke f obj v1 v2 v3 v4 vs))))

(defn message
  "Message dispatch for object orientated type calling convention.
 
   (def obj {:a 10
             :b 20
             :get-sum (fn [this]
                        (+ (:b this) (:a this)))})
 
   (message obj :get-sum) => 30"
  {:added "3.0"}
  ([obj kw] (invoke (obj kw) obj))
  ([obj kw v1] (invoke (obj kw) obj v1))
  ([obj kw v1 v2] (invoke (obj kw) obj v1 v2))
  ([obj kw v1 v2 v3] (invoke (obj kw) obj v1 v2 v3))
  ([obj kw v1 v2 v3 v4] (invoke (obj kw) obj v1 v2 v3 v4))
  ([obj kw v1 v2 v3 v4 & vs] (apply invoke (obj kw) obj v1 v2 v3 v4 vs)))

(defn op
  "loose version of apply. Will adjust the arguments to put into a function
 
   (op + 1 2 3 4 5 6) => 21
 
   (op (fn [x] x) 1 2 3) => 1
 
   (op (fn [_ y] y) 1 2 3) => 2
 
   (op (fn [_] nil)) => (throws Exception)"
  {:added "3.0"}
  [f & args]
  (let [nargs (count args)
        vargs (arg/varg-count f)]
    (if (and vargs (>= nargs vargs))
      (apply f args)
      (let [fargs (arg/arg-count f)
            candidates (filter #(<= % nargs) fargs)]
        (if (empty? candidates)
          (throw (Exception. (str "arguments have to be of at least length " (apply min fargs))))
          (let [cnt (apply max candidates)]
            (apply f (take cnt args))))))))
