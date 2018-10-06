(ns hara.data.base.map)

(defn dissoc-in
  "disassociates keys from a nested map. Setting `keep` to `true` will
   not remove a empty map after dissoc
 
   (dissoc-in {:a {:b 10 :c 20}} [:a :b])
   => {:a {:c 20}}
 
   (dissoc-in {:a {:b 10}} [:a :b])
   => {}
 
   (dissoc-in {:a {:b 10}} [:a :b] true)
   => {:a {}}"
  {:added "3.0"}
  ([m [k & ks]]
   (if-not ks
     (dissoc m k)
     (let [nm (dissoc-in (m k) ks)]
       (cond (empty? nm) (dissoc m k)
             :else (assoc m k nm)))))

  ([m [k & ks] keep]
   (if-not ks
     (dissoc m k)
     (assoc m k (dissoc-in (m k) ks keep)))))

(defn unique
  "returns a map of all key/value pairs that differ from a second map
 
   (unique {:a 1} {:a 2})
   => {:a 1}
 
   (unique {:a 1 :b 2} {:b 2})
   => {:a 1}
 
   (unique {:b 2} {:b 2 :a 1})
   => nil"
  {:added "3.0"}
  [m1 m2]
  (reduce (fn [i [k v]]
            (if (not= v (get m2 k))
              (assoc i k v)
              i))
          nil m1))

(defn assoc-if
  "assoc key/value pairs to the map only on non-nil values
 
   (assoc-if {} :a 1)
   => {:a 1}
 
   (assoc-if {} :a 1 :b nil)
   => {:a 1}"
  {:added "3.0"}
  ([m k v]
   (if (not (nil? v)) (assoc m k v) m))
  ([m k v & more]
   (apply assoc-if (assoc-if m k v) more)))

(defn assoc-in-if
  "assoc-in a nested key/value pair to a map only on non-nil values
 
   (assoc-in-if {} [:a :b] 1)
   => {:a {:b 1}}
 
   (assoc-in-if {} [:a :b] nil)
   => {}"
  {:added "3.0"}
  [m arr v]
  (if (not (nil? v)) (assoc-in m arr v) m))

(defn update-in-if
  "update-in a nested key/value pair only if the value exists
 
   (update-in-if {:a {:b 1}} [:a :b] inc)
   => {:a {:b 2}}
 
   (update-in-if {} [:a :b] inc)
   => {}"
  {:added "3.0"}
  [m arr f & args]
  (let [v (get-in m arr)]
    (if (not (nil? v))
      (assoc-in m arr (apply f v args))
      m)))

(defn merge-if
  "merges key/value pairs into a single map only if the value exists
 
   (merge-if {:a nil :b 1})
   => {:b 1}
 
   (merge-if {:a 1} {:b nil :c 2})
   => {:a 1 :c 2}
 
   (merge-if {:a 1} {:b nil} {:c 2})
   => {:a 1 :c 2}"
  {:added "3.0"}
  ([] nil)
  ([m]
   (reduce (fn [i [k v]]
             (if (not (nil? v)) (assoc i k v) i))
           {} m))
  ([m1 m2]
   (reduce (fn [i [k v]]
             (if (not (nil? v)) (assoc i k v) i))
           (merge-if m1) m2))
  ([m1 m2 & more]
   (apply merge-if (merge-if m1 m2) more)))

(defn into-if
  "like into but filters nil values for both key/value pairs
   and sequences
 
   (into-if [] [1 nil 2 3])
   => [1 2 3]
 
   (into-if {:a 1} {:b nil :c 2})
   => {:a 1 :c 2}"
  {:added "3.0"}
  [to from]
  (reduce (fn [i e]
            (if (or (and (coll? e) (not (nil? (second e))))
                    (and (not (coll? e)) (not (nil? e))))
              (conj i e)
              i))
          to from))

(defn select-keys-if
  "selects only the non-nil key/value pairs from a map
 
   (select-keys-if {:a 1 :b nil} [:a :b])
   => {:a 1}
 
   (select-keys-if {:a 1 :b nil :c 2} [:a :b :c])
   => {:a 1 :c 2}"
  {:added "3.0"}
  [m ks]
  (reduce (fn [i k]
            (let [v (get m k)]
              (if (not (nil? v))
                (assoc i k v)
                i)))
          nil ks))

(defn merge-nil
  "only merge if the value in the original map is nil
 
   (merge-nil {:a 1} {:b 2})
   => {:a 1 :b 2}
 
   (merge-nil {:a 1} {:a 2})
   => {:a 1}"
  {:added "3.0"}
  ([] nil)
  ([m] m)
  ([m1 m2]
   (reduce (fn [i [k v]]
             (if (not (nil? (get i k)))
               i
               (assoc i k v)))
           m1 m2))
  ([m1 m2 & more]
   (apply merge-nil (merge-nil m1 m2) more)))

(defn assoc-nil
  "only assoc if the value in the original map is nil
 
   (assoc-nil {:a 1} :b 2)
   => {:a 1 :b 2}
 
   (assoc-nil {:a 1} :a 2 :b 2)
   => {:a 1 :b 2}"
  {:added "3.0"}
  ([m k v]
   (if (not (nil? (get m k))) m (assoc m k v)))
  ([m k v & more]
   (apply assoc-nil (assoc-nil m k v) more)))

(defn assoc-in-nil
  "only assoc-in if the value in the original map is nil
 
   (assoc-in-nil {} [:a :b] 2)
   => {:a {:b 2}}
 
   (assoc-in-nil {:a {:b 1}} [:a :b] 2)
   => {:a {:b 1}}"
  {:added "3.0"}
  [m ks v]
  (if (not (nil? (get-in m ks))) m (assoc-in m ks v)))

(defn transform-in
  "moves values around in a map according to a table
 
   (transform-in {:a 1 :b 2}
                 {[:c :d] [:a]})
   => {:b 2, :c {:d 1}}"
  {:added "3.0"}
  [m rels]
  (reduce (fn [out [to from]]
            (let [v (get-in m from)]
              (-> out
                  (assoc-in-if to v)
                  (dissoc-in from))))
          m
          rels))

(defn retract-in
  "reversed the changes by transform-in
 
   (retract-in {:b 2, :c {:d 1}}
               {[:c :d] [:a]})
   => {:a 1 :b 2}"
  {:added "3.0"}
  [m rels]
  (reduce (fn [out [to from]]
            (let [v (get-in m to)]
              (-> out
                  (assoc-in-if from v)
                  (dissoc-in to))))
          m
          (reverse rels)))

(defn map-keys
  "changes the keys of a map
   
   (map-keys inc {0 :a 1 :b 2 :c})
   => {1 :a, 2 :b, 3 :c}"
  {:added "3.0"}
  [f m]
  (reduce (fn [out [k v]]
            (assoc out (f k) v))
          {}
          m))

(defn map-vals
  "changes the values of a map
 
   (map-vals inc {:a 1 :b 2 :c 3})
   => {:a 2, :b 3, :c 4}"
  {:added "3.0"}
  [f m]
  (reduce (fn [out [k v]]
            (assoc out k (f v)))
          {}
          m))

(defn map-entries
  "manipulates a map given the function
 
   (map-entries (fn [[k v]]
                  [(keyword (str v)) (name k)])
                {:a 1 :b 2 :c 3})
   => {:1 \"a\", :2 \"b\", :3 \"c\"}"
  {:added "3.0"}
  [f m]
  (->> (map f m)
       (into {})))

(defn transpose
  "sets the vals and keys and vice-versa
   
   (transpose {:a 1 :b 2 :c 3})
   => {1 :a, 2 :b, 3 :c}"
  {:added "3.0"}
  [m]
  (reduce (fn [out [k v]]
            (assoc out v k))
          {}
          m))
