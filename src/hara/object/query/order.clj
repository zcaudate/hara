(ns hara.object.query.order
  (:require [hara.object.element.common :as common]
            [hara.object.element :as element]
            [hara.object.query.filter :as filter]))

(defn sort-fn
  "returns a function taking two inputs `x` and `y`, comparing the outputs after applying `f` to both
 
   ((order/sort-fn :id) {:id 1} {:id 2}) => -1"
  {:added "3.0"}
  [f]
  (fn [x y]
    (compare (f x) (f y))))

(defn sort-terms-fn
  "This is a little broken, it is supposed to sort on various keys, but currently only works with :name
 
   ((order/sort-terms-fn {:sort-terms nil})
    [{:name 3} {:name 1} {:name 2}])
   => [{:name 1} {:name 2} {:name 3}]"
  {:added "3.0"}
  [grp]
  (let [sterms (:sort-terms grp)]
    (fn [eles]
      (cond (nil? sterms) (sort (sort-fn :name) eles)
            :else eles))))

(defn first-terms-fn
  "creates a function that returns the first element of the list
 
   ((order/first-terms-fn {:first true}) [1 2 3])
   => 1"
  {:added "3.0"}
  [grp]
  (if (:first grp) first))

(defn merge-terms-fn
  "creates a function that returns the first element of the list
 
   (-> (query/query-class (type []) [\"create\"])
       ((order/merge-terms-fn {:merge true})))
   ;;=> #[create :: ([java.util.List]), ([clojure.lang.ISeq]),
   ;;               ([clojure.lang.IReduceInit]), ([java.lang.Object[]]),
   ;;               ([java.lang.Iterable])]
 "
  {:added "3.0"}
  [grp]
  (if (:merge grp)
    (fn [eles]
      (if-let [name (-> eles first :name)]
        (let [eles (take-while #(= name (:name %)) eles)]
          (if (= 1 (count eles))
            (first eles)
            (if (every? common/element? eles)
              (element/to-element (vec eles))
              (set eles))))
        (first eles)))))

(defn select-terms-fn
  "creates a function that selects terms to output
 
   (-> (query/query-class (type []) [\"create\"])
       ((order/select-terms-fn {:select-terms [:name]})))
   => [\"create\"]"
  {:added "3.0"}
  [grp]
  (let [sterms (sort (:select-terms grp))]
    (fn [eles]
      (condp = (count sterms)
        0 eles
        1 (distinct (map (first sterms) eles))
        (map #(select-keys (get % nil) sterms) eles)))))

(defn order
  "formats an output for 
 
   (->> (query/query-class (type []) [\"create\"])
        (order/order {:select-terms [:params]}))
   ;;=> ([java.util.List] [clojure.lang.ISeq]
   ;;    [clojure.lang.IReduceInit] [[Ljava.lang.Object;]
   ;;    [java.lang.Iterable])
 "
  {:added "3.0"}
  [grp eles]
  ((comp
    (or (merge-terms-fn grp) (first-terms-fn grp) identity)
    (select-terms-fn grp)
    (sort-terms-fn grp)
    (filter/filter-terms-fn grp))
   eles))
