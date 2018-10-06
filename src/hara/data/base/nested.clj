(ns hara.data.base.nested
  (:require [clojure.set :as set]
            [hara.core.base.check :as check]
            [hara.core.base.error :as error]
            ;[hara.core.base.shorthand :as hand]
            [hara.data.base.map :as map]))

(defn keys-nested
  "The set of all nested keys in a map
 
   (keys-nested {:a {:b 1 :c {:d 1}}})
   => #{:a :b :c :d}"
  {:added "3.0"}
  ([m] (reduce-kv (fn [s k v]
                    (if (check/hash-map? v)
                      (set/union (conj s k) (keys-nested v))
                      (conj s k)))
                  #{}
                  m)))

(defn key-paths
  "The set of all paths in a map, governed by a max level of nesting
 
   (key-paths {:a {:b 1} :c {:d 1}})
   => (contains [[:c :d] [:a :b]] :in-any-order)
 
   (key-paths {:a {:b 1} :c {:d 1}} 1)
   => (contains [[:c] [:a]] :in-any-order)"
  {:added "3.0"}
  ([m] (key-paths m -1 []))
  ([m max] (key-paths m max []))
  ([m max arr]
   (reduce-kv (fn [out k v]
                (cond (and (not= max 1)
                           (check/hash-map? v))
                  (vec (concat out (key-paths v (dec max) (conj arr k))))
                  
                  :else (conj out (conj arr k))))
              []
              m)))

(defn update-keys-in
  "updates all keys in a map with given function
 
   (update-keys-in {:x {[\"a\" \"b\"] 1 [\"c\" \"d\"] 2}} [:x] string/join)
   => {:x {\"ab\" 1 \"cd\" 2}}
 
   (update-keys-in {:a {:c 1} :b {:d 2}} 2 name)
   => {:b {\"d\" 2}, :a {\"c\" 1}}"
  {:added "3.0"}
  [m arr f & args]
  (let [key-fn (fn [m]
                 (map/map-keys (fn [k] (apply f k args)) m))]
    (cond (sequential? arr)
          (if (empty? arr)
            (key-fn m)
            (update-in m arr key-fn))

          (number? arr)
          (if (= 1 arr)
            (key-fn m)
            (reduce (fn [out kpath]
                      (update-in out kpath key-fn))
                    m
                    (key-paths m (dec arr))))

          :else (throw (ex-info "`arr` has to be a seq or a number." {:input arr})))))

(defn update-vals-in
  "updates all values in a map with given function
 
   (update-vals-in {:a 1 :b 2} [] inc)
   => {:a 2 :b 3}
 
   (update-vals-in {:a {:c 1} :b 2} [:a] inc)
   => {:a {:c 2} :b 2}
 
   (update-vals-in {:a {:c 1} :b {:d 2}} 2 inc)
   => {:a {:c 2} :b {:d 3}}
 
   (update-vals-in {:a 1 :b 2} 1 inc)
   => {:a 2, :b 3}"
  {:added "3.0"}
  [m arr f & args]
  (let [val-fn (fn [m]
                 (map/map-vals (fn [v] (apply f v args)) m))]
    (cond (sequential? arr)
          (if (empty? arr)
            (val-fn m)
            (update-in m arr val-fn))

          (number? arr)
          (if (= 1 arr)
            (val-fn m)
            (reduce (fn [out kpath]
                      (update-in out kpath val-fn))
                    m
                    (key-paths m (dec arr))))

          :else (throw (ex-info "`arr` has to be a seq or a number." {:input arr})))))

(defn merge-nested
  "Merges nested values from left to right.
 
   (merge-nested {:a {:b {:c 3}}} {:a {:b 3}})
   => {:a {:b 3}}
 
   (merge-nested {:a {:b {:c 1 :d 2}}}
                 {:a {:b {:c 3}}})
   => {:a {:b {:c 3 :d 2}}}"
  {:added "3.0"}
  ([] {})
  ([m] m)
  ([m1 m2]
   (reduce-kv (fn [out k v]
                (let [v1 (get out k)]
                  (cond (nil? v1)
                        (assoc out k v)

                        (and (check/hash-map? v) (check/hash-map? v1))
                        (assoc out k (merge-nested v1 v))

                        (= v v1)
                        out

                        :else
                        (assoc out k v))))
              (or m1 {})
              m2))
  ([m1 m2 & ms]
   (apply merge-nested (merge-nested m1 m2) ms)))

(defn merge-nil-nested
  "Merges nested values from left to right, provided the merged value does not exist
 
   (merge-nil-nested {:a {:b 2}} {:a {:c 2}})
   => {:a {:b 2 :c 2}}
 
   (merge-nil-nested {:b {:c :old}} {:b {:c :new}})
   => {:b {:c :old}}"
  {:added "3.0"}
  ([] nil)
  ([m] m)
  ([m1 m2]
   (reduce-kv (fn [out k v]
                (let [v1 (get out k)]
                  (cond (nil? v1)
                        (assoc out k v)

                        (and (check/hash-map? v) (check/hash-map? v1))
                        (assoc out k (merge-nil-nested v1 v))

                        :else
                        out)))
              (or m1 {})
              m2))
  ([m1 m2 & more]
   (apply merge-nil-nested (merge-nil-nested m1 m2) more)))

(defn dissoc-nested
  "Returns `m` without all nested keys in `ks`.
 
   (dissoc-nested {:a {:b 1 :c {:b 1}}} [:b])
   => {:a {:c {}}}"
  {:added "3.0"}
  [m ks]
  (let [ks (if (set? ks) ks (set ks))]
    (reduce-kv (fn [out k v]
                 (cond (get ks k)
                       out

                       (check/hash-map? v)
                       (assoc out k (dissoc-nested v ks))

                       :else (assoc out k v)))
               {}
               m)))

(defn unique-nested
  "All nested values in `m1` that are unique to those in `m2`.
 
   (unique-nested {:a {:b 1}}
                  {:a {:b 1 :c 1}})
   => {}
 
   (unique-nested {:a {:b 1 :c 1}}
                  {:a {:b 1}})
   => {:a {:c 1}}"
  {:added "3.0"}
  [m1 m2]
  (reduce-kv (fn [out k v]
               (let [v2 (get m2 k)]
                 (cond (nil? v2)
                       (assoc out k v)

                       (and (check/hash-map? v) (check/hash-map? v2))
                       (let [subv (unique-nested v v2)]
                         (if (empty? subv)
                           out
                           (assoc out k subv))) (= v v2)
                       out

                       :else
                       (assoc out k v))))
             {}
             m1))

(defn clean-nested
  "Returns a associative with nils and empty hash-maps removed.
 
   (clean-nested {:a {:b {:c {}}}})
   => {}
 
   (clean-nested {:a {:b {:c {} :d 1 :e nil}}})
   => {:a {:b {:d 1}}}"
  {:added "3.0"}
  ([m]
   (reduce-kv (fn [out k v]
                (cond (nil? v)
                      out

                      (check/hash-map? v)
                      (let [subv (clean-nested v)]
                        (if (empty? subv)
                          out
                          (assoc out k subv)))

                      :else
                      (assoc out k v)))
              {}
              m)))
