(ns hara.data.deque
  (:refer-clojure :exclude [empty concat])
  (:require [clojure.core.rrb-vector :as rrb]
            [hara.module :as module]))

(module/include
 (clojure.core     (create < vector)
                   (pop-right < pop)
                   (peek-right < peek))
 
 (clojure.core.rrb-vector (concat < catvec)))

(defn pop-left
  "pops an element from the left
 
   (pop-left [1 2 3 4])
   => [2 3 4]"
  {:added "3.0"}
  [v]
  (subvec v 1))

(defn peek-left
  "peeks at the first element on the left
 
   (peek-left [1 2 3 4])
   => 1"
  {:added "3.0"}
  [v]
  (first v))

(defn conj-right
  "appends elements on the right
 
   (conj-right [1] 2 3 4)
   => [1 2 3 4]"
  {:added "3.0"}
  ([v & more]
   (let [v (or v [])]
     (apply conj v more))))

(defn conj-left
  "appends elements on the left
 
   (conj-left [4] 3 2 1)
   => [1 2 3 4]"
  {:added "3.0"}
  [v & more]
  (rrb/catvec (vec (reverse more)) v))

(defn conj-both
  "appends elements on either side
 
   (conj-both 1 [2] 3)
   => [1 2 3]"
  {:added "3.0"}
  [l deque r]
  (rrb/catvec [l] deque [r]))

(defn update-left
  "updates the leftmost element
 
   (update-left [1 2 3] dec)
   => [0 2 3]"
  {:added "3.0"}
  [deque f & args]
  (conj-left (pop-left deque) (apply f (peek-left deque) args)))

(defn update-right
  "updates the rightmost element
 
   (update-right [1 2 3] inc)
   => [1 2 4]"
  {:added "3.0"}
  [deque f & args]
  (conj-right (pop-right deque) (apply f (peek-right deque) args)))
