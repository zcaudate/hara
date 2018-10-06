(ns hara.function.base.arg
  (:import (clojure.lang Fn RestFn)
           (java.lang.reflect Method)))

(defn vargs?
  "checks that function contain variable arguments
 
   (vargs? (fn [x])) => false
 
   (vargs? (fn [x & xs])) => true"
  {:added "3.0"}
  [^Fn f]
  (if (some (fn [^Method mthd]
              (= "getRequiredArity" (.getName mthd)))
            (.getDeclaredMethods (class f)))
    true
    false))

(defn varg-count
  "counts the number of arguments types before variable arguments
 
   (varg-count (fn [x y & xs])) => 2
 
   (varg-count (fn [x])) => nil"
  {:added "3.0"}
  [f]
  (if (some (fn [^Method mthd]
              (= "getRequiredArity" (.getName mthd)))
            (.getDeclaredMethods (class f)))
    (.getRequiredArity ^RestFn f)))

(defn arg-count
  "counts the number of non-varidic argument types
 
   (arg-count (fn [x])) => [1]
 
   (arg-count (fn [x & xs])) => []
 
   (arg-count (fn ([x]) ([x y]))) => [1 2]"
  {:added "3.0"}
  [f]
  (let [ms (filter (fn [^Method mthd]
                     (= "invoke" (.getName mthd)))
                   (.getDeclaredMethods (class f)))
        ps (map (fn [^Method m]
                  (.getParameterTypes m)) ms)]
    (map alength ps)))

(defn arg-check
  "counts the number of non-varidic argument types
 
   (arg-check (fn [x]) 1) => true
 
   (arg-check (fn [x & xs]) 1) => true
 
   (arg-check (fn [x & xs]) 0)
   => (throws Exception \"Function must accomodate 0 arguments\")"
  {:added "3.0"}
  [f num]
  (or (if-let [vc (varg-count f)]
        (<= vc num))
      (some #(= num %) (arg-count f))
      (throw (ex-info (str "Function must accomodate " num " arguments")
                      {:function f}))))
