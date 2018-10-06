(ns hara.data.base.diff
  (:require [hara.core.base.check :as check]
            [hara.data.base.map :as map]
            [hara.data.base.nested :as nested]))

(defn diff-changes
  "Finds changes in nested maps, does not consider new elements
 
   (diff-changes {:a 2} {:a 1})
   => {[:a] 2}
 
   (diff-changes {:a {:b 1 :c 2}} {:a {:b 1 :c 3}})
   => {[:a :c] 2}
 
   "
  {:added "3.0"}
  ([m1 m2]
   (diff-changes m1 m2 []))
  ([m1 m2 arr]
   (reduce-kv (fn [out k1 v1]
                (if (contains? m2 k1)
                  (let [v2 (get m2 k1)]
                    (cond (and (check/hash-map? v1) (check/hash-map? v2))
                          (merge out (diff-changes v1 v2 (conj arr k1)))

                          (= v1 v2)
                          out

                          :else
                          (assoc out (conj arr k1) v1)))
                  out))
              {}
              m1)))

(defn diff-new
  "Finds new elements in nested maps, does not consider changes
 
   (diff-new {:a 2} {:a 1})
   => {}
 
   (diff-new {:a {:b 1}} {:a {:c 2}})
   => {[:a :b] 1}
 
   "
  {:added "3.0"}
  ([m1 m2]
   (diff-new m1 m2 []))
  ([m1 m2 arr]
   (reduce-kv (fn [out k1 v1]
                (let [v2 (get m2 k1)]
                  (cond (and (check/hash-map? v1) (check/hash-map? v2))
                        (merge out (diff-new v1 v2 (conj arr k1)))

                        (not (contains? m2 k1))
                        (assoc out (conj arr k1) v1)

                        :else out)))
              {}
              m1)))

(defn diff
  "Finds the difference between two maps
 
   (diff {:a 2} {:a 1})
   => {:+ {} :- {} :> {[:a] 2}}
 
   (diff {:a {:b 1 :d 3}} {:a {:c 2 :d 4}} true)
   => {:+ {[:a :b] 1}
       :- {[:a :c] 2}
       :> {[:a :d] 3}
       :< {[:a :d] 4}}"
  {:added "3.0"}
  ([m1 m2] (diff m1 m2 false))
  ([m1 m2 reversible]
   (let [diff (hash-map :+ (diff-new m1 m2)
                        :- (diff-new m2 m1)
                        :> (diff-changes m1 m2))]
     (if reversible
       (assoc diff :< (diff-changes m2 m1))
       diff))))

(defn merge-or-replace
  "If both are maps then merge, otherwis replace
 
   (merge-or-replace {:a {:b {:c 2}}} {:a {:b {:c 3 :d 4}}})
   => {:a {:b {:c 3 :d 4}}}"
  {:added "3.0"}
  [x v]
  (cond (and (check/hash-map? x)
             (check/hash-map? v))
        (nested/merge-nested x v)

        :else v))

(defn changed
  "Outputs what has changed between the two maps
 
   (changed {:a {:b {:c 3 :d 4}}}
            {:a {:b {:c 3}}})
   => {:a {:b {:d 4}}}"
  {:added "3.0"}
  [new old]
  (->> (diff new old)
       ((juxt :> :+))
       (apply merge)
       (reduce-kv (fn [out ks v]
                    (assoc-in out ks v))
                  {})))

(defn patch
  "Use the diff to convert one map to another in the forward
   direction based upon changes between the two.
 
   (let [m1  {:a {:b 1 :d 3}}
         m2  {:a {:c 2 :d 4}}
         df  (diff m2 m1)]
     (patch m1 df))
   => {:a {:c 2 :d 4}}"
  {:added "3.0"}
  [m diff]
  (->> m
       (#(reduce-kv (fn [m arr v]
                      (update-in m arr merge-or-replace v))
                    %
                    (merge (:+ diff) (:> diff))))
       (#(reduce (fn [m arr]
                   (map/dissoc-in m arr))
                 %
                 (keys (:- diff))))))

(defn unpatch
  "Use the diff to convert one map to another in the reverse
   direction based upon changes between the two.
 
   (let [m1  {:a {:b 1 :d 3}}
         m2  {:a {:c 2 :d 4}}
         df  (diff m2 m1 true)]
     (unpatch m2 df))
   => {:a {:b 1 :d 3}}"
  {:added "3.0"}
  [m diff]
  (->> m
       (#(reduce-kv (fn [m arr v]
                      (update-in m arr merge-or-replace v))
                    %
                    (merge (:- diff) (:< diff))))
       (#(reduce (fn [m arr]
                   (map/dissoc-in m arr))
                 %
                 (keys (:+ diff))))))
