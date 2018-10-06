(ns hara.data.base.seq.diff)


;; Concepts
;;
;; fp is a map {k -> [d edits]} from diagonal k to a pair where
;;   d is the furthest distance and
;;   edits is a vector of edit operations.
;; 
;; as, bs are sequences of arbitrary items that support equals (=)
;; av, bv are vector versions that have better count and nth performance
;; 


;; ---------------------------------------------------------------------------
;; diff


(defn- edits
  [fp k]
  (nth (get fp k [nil []]) 1))

(defn- distance
  [fp k]
  (nth (get fp k [-1]) 0))

(defn- snake
  "Advances x on the diagonal k as long as corresponding items in av
  and bv match."
  [av bv fp k]
  (let [n     (count av)
        m     (count bv)
        k+1   (inc k)
        k-1   (dec k)
        i     (inc (distance fp k-1))
        j     (distance fp k+1)
        x     (max i j)
        y     (- x k)
        ;; search for the maximum x on diagonal
        fx    (loop [x x
                     y y]
                (if (and (< x n) (< y m) (= (nth av x) (nth bv y)))
                  (recur (inc x) (inc y))
                  x))]
    [fx
     ;; add edit operation symbols
     (let [es (if (> i j)
                (conj (edits fp k-1) :-)
                (conj (edits fp k+1) :+))]
       (if (> fx x)
         (conj es (- fx x))
         es))]))

(defn- step
  "Returns the next pair of [fp p] of furthest distances."
  [av bv delta [fp p]]
  (let [p   (inc p)
        fpt (transient fp)
        fpt (loop [k (* -1 p) fpt fpt]
              (if (< k delta)
                (recur (inc k) (assoc! fpt k (snake av bv fpt k)))
                fpt))
        fpt (loop [k (+ delta p) fpt fpt]
              (if (< delta k)
                (recur (dec k) (assoc! fpt k (snake av bv fpt k)))
                fpt))
        fp  (persistent! (assoc! fpt delta (snake av bv fpt delta)))]
    [fp p]))

(defn- diff*
  "Assumes that (count as) >= (count bs)."
  [av bv]
  (let [delta (- (count av) (count bv))
        [fp p] (->> [{} -1]
                    (iterate (partial step av bv delta))
                    (drop-while (fn [[fp _]]
                                  (not= (distance fp delta) (count av))))
                    (first))]
    [(+ delta (* 2 p)) (->> (get fp delta)
                            (second)
                            (drop 1))]))

(defn- swap-insdels
  "Swaps edit operation symbols :+ <-> :-"
  [[d edits]]
  [d (map (fn [op] (case op :+ :- :- :+ op)) edits)])

(defn- editscript
  "Produces an edit script from the edits issued by diff*."
  [av bv edits]
  ;; the groups are seqs of :+'s or :-'s or one number
  (loop [groups (partition-by identity edits)
         x 0
         y 0
         script []]
    (if-let [[op & ops] (first groups)]
      (let [n (inc (count ops))]
        (case op
          :- (recur (rest groups)
                    x y
                    (conj script [:- x n]))
          :+ (recur (rest groups)
                    (+ x n) (+ y n)
                    (conj script [:+ y (subvec bv y (+ y n))]))
          (recur (rest groups)
                 (+ x op) (+ y op) ; op is the number of items to skip
                 script)))
      script)))

(defn diff
  "creates a diff of two sequences
 
   (diff [1 2 3 4 5]
         [1 2 :a 4 5])
   => [2 [[:- 2 1] [:+ 2 [:a]]]]
   
   (diff [1 2 3 4 5]
         [1 :a 3 2 5])
   => [4 [[:- 1 1]
          [:+ 1 [:a]]
          [:- 3 1]
          [:+ 3 [2]]]]"
  {:added "3.0"}
  [as bs]
  (cond
    (and (empty? as) (empty? bs))
    [0 []]
    (empty? as)
    [(count bs) [[:+ 0 bs]]]
    (empty? bs)
    [(count as) [[:- 0 (count as)]]]
    :else
    (let [av (vec as)
          bv (vec bs)
          [d edits] (if (< (count av) (count bv))
                      (swap-insdels (diff* bv av))
                      (diff* av bv))]
      [d (editscript av bv edits)])))

;; ---------------------------------------------------------------------------
;; patch


(defn- insert-at
  "Insert sequence ys at position i into xs."
  [xs i ys]
  (let [xv (vec xs)]
    (concat (subvec xv 0 i) ys (subvec xv i))))

(defn- remove-at
  "Remove n items at position i from xs."
  ([xs i]
   (remove-at xs i 1))
  ([xs i n]
   (let [xv (vec xs)]
     (concat (subvec xv 0 i) (subvec xv (+ i n))))))

(defn patch
  "uses a diff to reconcile two sequences
 
   (patch [1 2 3 4 5]
          [4 [[:- 1 1]
              [:+ 1 [:a]]
              [:- 3 1]
              [:+ 3 [2]]]])
   => [1 :a 3 2 5]"
  {:added "3.0"}
  ([as diff-result]
   (patch insert-at remove-at (vec as) diff-result))
  ([insert-f remove-f as [d es]]
   (vec (reduce (fn [bs [op & params]]
                  (case op
                    :+ (insert-f bs (first params) (second params))
                    :- (remove-f bs (first params) (second params))))
                as
                es))))
