(ns hara.print.pretty.compare
  (:refer-clojure :exclude [compare]))

(defn type-priority
  "creates a ordered list of items in an unordered collection
 
   (type-priority 1) => 3
 
   (type-priority :hello) => 6
 
   (type-priority {}) => 11"
  {:added "3.0"}
  [x]
  (let [predicates [nil? false? true? number? char? string?
                    keyword? symbol? list? vector? set? map?]
        priority (->> predicates
                      (map vector (range))
                      (some (fn [[i p]] (when (p x) i))))]
    (or priority (count predicates))))

(defn compare-seqs
  "compares two sequences
   
   (compare-seqs compare [1 2 3] [4 5 6])
   => -1"
  {:added "3.0"}
  [compare xs ys]
  (or (first (remove zero? (map compare xs ys)))
      (- (count xs) (count ys))))

(defn compare
  "compares any two values
 
   (rank 1 :hello)
   => -1
 
   (rank  {:a 1} 3)
   => 1"
  {:added "3.0"}
  [a b]
  (if (= a b)
    0
    (let [pri-a (type-priority a)
          pri-b (type-priority b)]
      (cond
        (< pri-a pri-b) -1
        (> pri-a pri-b)  1

        (some #(% a) #{number? char? string? keyword? symbol?})
        (clojure.core/compare a b)

        (map? a)
        (compare-seqs compare
          (sort-by first compare (seq a))
          (sort-by first compare (seq b)))

        (set? a)
        (let [size-diff (- (count a) (count b))]
          (if (zero? size-diff)
            (compare-seqs compare a b)
            size-diff))

        (coll? a)
        (compare-seqs compare a b)

        :else
        (let [class-diff (compare (.getName (type a))
                                  (.getName (type b)))]
          (if (zero? class-diff)
            (if (instance? java.lang.Comparable a)
              (clojure.core/compare a b)
              (clojure.core/compare (str a) (str b)))
            class-diff))))))
