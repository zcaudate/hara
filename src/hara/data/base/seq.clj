(ns hara.data.base.seq)

(defn positions
  "find positions of elements matching the predicate
 
   (positions even? [5 5 4 4 3 3 2 2])
   => [2 3 6 7]"
  {:added "3.0"}
  [pred coll]
  (keep-indexed (fn [idx x]
                  (when (pred x)
                    idx))
                coll))

(defn remove-index
  "removes element at the specified index
 
   (remove-index [:a :b :c :d] 2)
   => [:a :b :d]"
  {:added "3.0"}
  [coll i]
  (cond (vector? coll)
        (reduce conj
                (subvec coll 0 i)
                (subvec coll (inc i) (count coll)))

        :else
        (keep-indexed #(if (not= %1 i) %2) coll)))

(defn index-of
  "finds the index of the first matching element in an array
 
   (index-of even? [1 2 3 4]) => 1
 
   (index-of keyword? [1 2 :hello 4]) => 2"
  {:added "3.0"}
  [pred coll]
  (loop [[x & more :as coll] coll
         i 0]
    (cond (empty? coll) -1

          (pred x) i

          :else
          (recur more (inc i)))))

(defn element-of
  "finds the element within an array
 
   (element-of keyword? [1 2 :hello 4])
   => :hello"
  {:added "3.0"}
  [pred coll]
  (loop [[x & more :as coll] coll]
    (cond (empty? coll) nil

          (pred x) x

          :else
          (recur more))))

(defn flatten-all
  "flattens all elements the collection
 
   (flatten-all [1 2 #{3 {4 5}}])
   => [1 2 3 4 5]"
  {:added "3.0"}
  [x]
  (filter (complement coll?)
          (rest (tree-seq coll? seq x))))
