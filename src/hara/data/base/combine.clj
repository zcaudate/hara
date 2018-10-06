(ns hara.data.base.combine
  (:require [clojure.set :as set]
            [hara.core.base.shorthand :as hand]))

(defn combine-select
  "selects an element out of the set that matches sel when it is applied
 
   (combine-select #{1 2 3} 2 identity)
   => 2
 
   (combine-select #{{:id 1 :val 2} {:id 2 :val 2}} {:id 1 :val 1} :id)
   => {:id 1 :val 2}"
  {:added "3.0"}
  [set val sel]
  (->> set
       (filter (fn [v]
                 (hand/eq-> v val sel)))
       first))

(defn combine-value
  "returns a single set, sel is used for item comparison while func
   is used as the combine function
 
   (combine-value #{{:id 1 :a 1} {:id 2 :a 2}}
                  {:id 3 :b 3}
                  :id merge)
   => #{{:id 1, :a 1} {:id 2, :a 2} {:id 3, :b 3}}
 
   (combine-value #{{:id 1 :a 1} {:id 2 :a 2}}
                  {:id 1 :b 3}
                  :id merge)
   => #{{:id 1 :a 1 :b 3} {:id 2 :a 2}}"
  {:added "3.0"}
  [set val sel func]
  (if-let [sv (combine-select set val sel)]
    (conj (disj set sv) (func sv val))
    (conj set val)))

(defn combine-set
  "Returns the combined set of `s1` and `s2` using sel for item
   comparison and func as the combine function
 
   (combine-set #{{:id 1 :val 0} {:id 2 :a 0}}
                #{{:id 1 :val 1} {:id 2 :val 2}}
                :id merge)
   => #{{:id 1 :val 1} {:id 2 :val 2 :a 0}}"
  {:added "3.0"}
  [s1 s2 sel func]
  (reduce (fn [out v]
            (combine-value out v sel func))
          s1 s2))

(defn combine-internal
  "Combines all elements in a single using sel and func
 
   (combine-internal #{{:id 1} {:id 2} {:id 1 :val 1} {:id 2 :val 2}}
                     :id merge)
   => #{{:id 1 :val 1} {:id 2 :val 2}}"
  {:added "3.0"}
  [set sel rd]
  (if-not (set? set) set
          (combine-set #{} set sel rd)))

(defn combine
  "takes `v1` and `v2`, which can be either
   values or sets of values and merges them into a new set.
 
   (combine 1 2) => #{1 2}
 
   (combine #{1} 1) => #{1}
 
   (combine #{{:id 1} {:id 2}}
            #{{:id 1 :val 1} {:id 2 :val 2}}
            :id merge)
   => #{{:id 1 :val 1} {:id 2 :val 2}}"
  {:added "3.0"}
  ([] nil)
  ([m] m)
  ([v1 v2]
   (cond (nil? v2) v1
         (nil? v1) v2

         (set? v1)
         (cond (set? v2)
               (set/union v1 v2)
               :else (conj v1 v2))

         :else
         (cond (set? v2)
               (conj v2 v1)

               (= v1 v2) v1
               :else #{v1 v2})))

  ([v1 v2 sel func]
   (-> (cond (nil? v2) v1
             (nil? v1) v2
             (set? v1)
             (cond (set? v2)
                   (combine-set v1 v2 sel func)

                   :else (combine-value v1 v2 sel func))
             :else
             (cond (set? v2)
                   (combine-value v2 v1 sel func)

                   (hand/eq-> v1 v2 sel)
                   (func v1 v2)

                   (= v1 v2) v1
                   :else #{v1 v2}))
       (combine-internal sel func))))

(defn decombine
  "takes set or value `v` and returns a set with
   elements matching sel removed
 
   (decombine 1 1) => nil
 
   (decombine 1 2) => 1
 
   (decombine #{1} 1) => nil
 
   (decombine #{1 2 3 4} #{1 2}) => #{3 4}
 
   (decombine #{1 2 3 4} even?) => #{1 3}"
  {:added "3.0"}
  [v dv]
  (cond (set? v)
        (let [res (cond (set? dv)
                        (set/difference v dv)

                        (ifn? dv)
                        (set (filter (complement dv) v))

                        :else (disj v dv))]
          (if-not (empty? res) res))
        :else
        (if-not (hand/check-> v dv) v)))
