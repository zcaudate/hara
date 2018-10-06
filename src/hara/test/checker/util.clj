(ns hara.test.checker.util)

(defn contains-exact
  "checks if a sequence matches exactly
   (contains-exact [0 1 2 3] (map checker/->checker [1 2 3]))
   => true
 
   (contains-exact [0 1 2 3] (map checker/->checker [1 3]))
   => false"
  {:added "3.0"}
  [seq pattern]
  (let [len (count pattern)
        n (- (count seq) len)
        seq (vec seq)]
    (or (->> (range (+ n 1))
             (map (fn [i]
                    (->> (subvec seq i (+ i len))
                         (map #(%1 %2) pattern)
                         (every? true?))))
             (some true?))
        false)))

(defn contains-with-gaps
  "checks if a sequence matches the pattern, with gaps allowed
   (contains-with-gaps [0 1 2 3] (map checker/->checker [1 2 3]))
   => true
 
   (contains-with-gaps [0 1 2 3] (map checker/->checker [1 3]))
   => true
 
   (contains-with-gaps [0 1 2 3] (map checker/->checker [2 0]))
   => false"
  {:added "3.0"}
  [seq pattern]
  (cond (empty? pattern) true

        (empty? seq) false

        ((first pattern) (first seq))
        (recur (next seq) (next pattern))

        :else
        (recur (next seq) pattern)))

(defn perm-check
  "decide if a given vector of perms are appropriately matched
   (perm-check [#{0 1 2} #{2} #{0 2}] #{0 1 2})
   => true
 
   (perm-check [#{2} #{0 1 2} #{2}] #{0 1 2})
   => false
 
   (perm-check [#{1} #{1 0} #{0 2 1} #{1 0} #{0 2 1}] #{0 1 2})
   => true"
  {:added "3.0"}
  ([perm all]
   (perm-check perm all (zipmap all (repeat nil))))
  ([perm all selection]
   (cond (empty? all)
         true

         (->> (vals selection)
              (every? (comp nil? not)))
         true

         :else
         (let [stats (reduce (fn [out v]
                               (let [cnt (->> (keep-indexed (fn [i set]
                                                              (if (get set v) i)) perm)
                                              set)]
                                 (assoc-in out [v] cnt)))
                             {}
                             all)
               order (->> stats
                          (into [])
                          (sort-by (comp count second)))
               [i matches] (first order)
               col   (first matches)]
           (if (nil? col)
             false
             (recur (-> (mapv #(disj % i) perm)
                        (update-in [col] empty))
                    (disj all i)
                    (assoc selection i col)))))))

(defn perm-build
  "builds a perm out of a sequence and checks
   (perm-build [0 1 2 3] (map checker/->checker [1 3]))
   => [#{} #{0} #{} #{1}]
 
   (perm-build [0 1 2 3] (map checker/->checker [odd? 3 number?]))
   => [#{2} #{0 2} #{2} #{0 1 2}]"
  {:added "3.0"}
  [seq pattern]
  (let [idx (->> pattern
                 (map-indexed (fn [i pat] [i pat]))
                 (into {}))]
    (mapv (fn [ele]
            (reduce-kv (fn [acc k v]
                         (if (v ele)
                           (conj acc k)
                           acc))
                       #{}
                       idx))
          seq)))

(defn contains-any-order
  "checks if a sequence matches the pattern, with any order allowed
   (contains-any-order [0 1 2 3] (map checker/->checker [2 1 3]))
   => true
 
   (contains-any-order [0 1 2 3] (map checker/->checker [2 0 3]))
   => false"
  {:added "3.0"}
  [seq pattern]
  (let [seq (vec seq)
        len (count pattern)
        n (- (count seq) len)
        indices (->> (range (+ n 1))
                     (map (fn [i]
                            (perm-build (subvec seq i (+ i len))
                                        pattern))))]
    (or (->> indices
             (map #(perm-check % (-> len range set)))
             (some true?))
        false)))

(defn contains-all
  "checks if a sequence matches any of the checks
   (contains-all [0 1 2 3] (map checker/->checker [2 1 3]))
   => true
 
   (contains-all [0 1 2 3] (map checker/->checker [2 0 3]))
   => true
 
   (contains-all [0 1 2 3] (map checker/->checker [0 0]))
   => false"
  {:added "3.0"}
  [seq pattern]
  (let [index (perm-build seq pattern)]
    (perm-check index (-> pattern count range set))))
