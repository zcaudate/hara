(ns hara.core.sort
  (:require [clojure.set :as set]))

(defn hierarchical-top
  "find the top node for the hierarchy of descendants
   
   (hierarchical-top 
     {1 #{2 3 4 5 6}
      2 #{3 5 6}
      3 #{5 6}
      4 #{}
      5 #{6}
      6 #{}}) => 1"
  {:added "3.0"}
  [idx]
  (let [rest (apply set/union (vals idx))]
    (ffirst (filter (fn [[k v]]
                      (not-empty (set/difference (conj v k) rest)))
                    idx))))

(defn hierarchical-sort
  "prunes a hierarchy of descendants into a directed graph
   
   (hierarchical-sort {1 #{2 3 4 5 6}
                       2 #{3 5 6}
                       3 #{5 6}
                       4 #{}
                       5 #{6}
                       6 #{}})
   => {1 #{4 2}
       2 #{3}
       3 #{5}
       4 #{}
      5 #{6}
       6 #{}}"
  {:added "3.0"}
  [idx]
  (let [top (hierarchical-top idx)]
    (loop [out {}
           candidates (dissoc idx top)
           level #{top}]
      (if (empty? level)
        out
        (let [base  (apply set/union (vals candidates))
              out   (reduce (fn [out i]
                              (assoc out i (set/difference (get idx i) base)))
                            out
                            level)
              nlevel (mapcat #(get out %) level)
              ncandidates (apply dissoc idx (concat (keys out) nlevel))]
          (recur out
                 ncandidates
                 nlevel))))))

(defn topological-top
  "nodes that have no other nodes that are dependent on them
   (topological-top {:a #{} :b #{:a}})
   => #{:b}"
  {:added "3.0"}
  [g]
  (let [nodes (set (keys g))
        dependent-nodes (apply set/union (vals g))]
    (set/difference nodes dependent-nodes)))

(defn topological-sort
  "sorts a directed graph into its dependency order
 
   (topological-sort {:a #{:b :c},
                      :b #{:d :e},
                      :c #{:e :f},
                      :d #{},
                      :e #{:f},
                      :f nil})
   => [:f :d :e :b :c :a]
 
   (topological-sort {:a #{:b},
                      :b #{:a}})
   => (throws)"
  {:added "3.0"}
  ([g]
   (let [g (let [dependent-nodes (apply set/union (vals g))]
             (reduce #(if (get % %2) % (assoc % %2 #{})) g dependent-nodes))]
     (topological-sort g () (topological-top g))))
  ([g l s]
   (cond (empty? s)
         (if (every? empty? (vals g))
           l
           (throw (ex-info "Graph Contains Circular Dependency."
                           {:data (->> g
                                       (filter (fn [[k v]] (-> v empty? not)))
                                       (into {}))
                            :list l})))

         :else
         (let [[n s*] (if-let [item (first s)]
                        [item (set/difference s #{item})])
               m (g n)
               g* (reduce #(update-in % [n] set/difference #{%2}) g m)]
           (recur g* (cons n l) (set/union s* (set/intersection (topological-top g*) m)))))))
