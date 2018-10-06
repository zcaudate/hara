(ns hara.function.base.multi
  (:require [hara.function.base.hook :as hook]
            [hara.function.base.arg :as arg]
            [clojure.set :as set])
  (:import (clojure.lang MultiFn)))

(defn multi?
  "returns `true` if `obj` is a multimethod
 
   (multi? print-method) => true
   
   (multi? println) => false"
  {:added "3.0"}
  [obj]
  (instance? MultiFn obj))

(defn multi-clone
  "creates a multimethod from an existing one
 
   (defmulti hello :type)
   
   (defmethod hello :a
     [m] (assoc m :a 1))
 
   (def world (multi-clone hello \"world\"))
   
   (defmethod world :b
     [m] (assoc m :b 2))
   
   (world {:type :b})
   => {:type :b :b 2}
   
   ;; original method should not be changed
   (hello {:type :b})
   => (throws)"
  {:added "3.0"}
  [source name]
  (let [table (.getMethodTable source)
        clone (MultiFn. name
                        (.dispatchFn source)
                        (.defaultDispatchVal source)
                        (.hierarchy source))]
    (doseq [[dispatch-val method] table]
      (.addMethod clone dispatch-val method))
    clone))

(defn multi-keys
  "returns all keys for a given multimethod
 
   (multi-keys world)
   => #{:a :b}"
  {:added "3.0"}
  [^MultiFn multi]
  (set (keys (.getMethodTable multi))))

(defn multi-has?
  "returns `true` if the multimethod contains a value for dispatch
 
   (multi-has? print-method Class)
   => true"
  {:added "3.0"}
  [^MultiFn multi val]
  (some #(= % val) (keys (.getMethodTable multi))))

(defn multi-list
  "returns all entries in the multimethod
 
   (multi-list world)
   => (satisfies [:a :b]
                 (comp vec sort keys))"
  {:added "3.0"}
  [multi]
  (.getMethodTable multi))

(defn multi-get
  "returns all entries in the multimethod
 
   (multi-get world :b)
   => fn?"
  {:added "3.0"}
  [multi dispatch]
  (get (.getMethodTable multi) dispatch))

(defn multi-remove
  "removes an entry
 
   (multi-remove world :b)
   => fn?"
  {:added "3.0"}
  [multi dispatch]
  (let [method (multi-get multi dispatch)]
    (remove-method multi dispatch)
    method))

(defn multi-match?
  "checks if the multi dispatch matches the arguments
 
   (multi-match? (.dispatchFn string/-from-string) (fn [_ _ _]))
   => true
 
   (multi-match? (.dispatchFn string/-from-string) (fn [_]) true)
   => (throws)"
  {:added "3.0"}
  ([multi method]
   (multi-match? multi method false))
  ([multi method throw?]
   (let [multi-args   (set (arg/arg-count multi))
         multi-vargs  (arg/varg-count multi)
         method-args  (set (arg/arg-count method))
         method-vargs (arg/varg-count method)]
     (boolean (or (seq (set/intersection multi-args method-args))
                  (and multi-vargs  (some #(<= % multi-vargs) method-args))
                  (and method-vargs (some #(> % method-vargs) multi-args))
                  (and multi-vargs method-vargs (<= method-vargs multi-vargs))
                  (if throw?
                    (throw (ex-info "Function args are not the same."
                                    {:multi  {:args  multi-args
                                              :vargs multi-vargs}
                                     :method {:args  method-args
                                              :vargs method-vargs}}))))))))

(defn multi-add
  "adds an entry to the multimethod
 
   (multi-add world :c (fn [m] (assoc m :c 3)))
   => world"
  {:added "3.0"}
  [multi dispatch-val method]
  (let [dispatch-fn (.dispatchFn multi)]
    (if (multi-match? dispatch-fn method true)
      (doto multi (.addMethod dispatch-val method)))))
