(ns hara.test.checker.collection
  (:require [hara.data.base.map :as map]
            [hara.test.checker.base :as base]
            [hara.test.checker.util :as util]
            [hara.test.common :as common]))

(defn verify-map
  "takes two maps and determines if they fit
   (verify-map {:a (base/satisfies odd?)
                :b (base/satisfies even?)}
               {:a 1 :b 2})
   => true"
  {:added "3.0"}
  [ck data]
  (->> (reduce-kv (fn [out k sck]
                    (conj out (base/verify sck (get data k))))
                  []
                  ck)
       (every? base/succeeded?)))

(defn verify-seq
  "takes two seqs and determines if they fit
   (verify-seq [(base/satisfies 1) (base/satisfies 2)]
               [2 1]
               #{:in-any-order})
   => true
 
   (verify-seq [(base/satisfies 1) (base/satisfies 2)]
               [2 3 1]
               #{:in-any-order :gaps-ok})
   => true"
  {:added "3.0"}
  [ck data modifiers]
  (cond (= #{} modifiers)
        (util/contains-exact data ck)

        (= #{:in-any-order} modifiers)
        (util/contains-any-order data ck)

        (= #{:gaps-ok} modifiers)
        (util/contains-with-gaps data ck)

        (= #{:in-any-order :gaps-ok} modifiers)
        (util/contains-all data ck)

        :else
        (throw (Exception. "modifiers can only be :gaps-only and :in-any-order"))))

(defn contains-map
  "map check helper function for `contains`"
  {:added "3.0"}
  [x]
  (let [ck (map/map-vals base/->checker x)]
    (common/map->Checker
     {:tag :contains
      :doc "Checks if the result is a map having the following conditions:"
      :fn  (fn [res]
             (let [data (common/->data res)]
               (and (map? data)
                    (verify-map ck data))))
      :expect ck})))

(defn contains-vector
  "vector check helper function for `contains`"
  {:added "3.0"}
  ([x] (contains-vector x #{}))
  ([x modifiers]
   (let [ck (mapv base/->checker x)]
     (common/map->Checker
      {:tag :contains
       :doc "Checks if the result is a sequential"
       :fn  (fn [res]
              (let [data (common/->data res)]
                (cond (set? data)
                      (verify-seq ck data (conj modifiers :in-any-order))

                      (sequential? data)
                      (verify-seq ck data modifiers))))
       :expect ck}))))

(defn contains-set
  "set check helper function for `contains`
 
   ((contains-set #{1 2 3}) [1 2 3 4 5])
   => true
 
   ((contains-set #{1 2 4}) [1 2 3 4 5])
   => false"
  {:added "3.0"}
  ([x] (contains-vector x #{:in-any-order})))

(defn contains
  "checker for maps and vectors
 
   ((contains {:a odd? :b even?}) {:a 1 :b 4})
   => true
 
   ((contains {:a 1 :b even?}) {:a 2 :b 4})
   => false
 
   ((contains [1 2 3]) [1 2 3 4])
   => true
 
   ((contains [1 3]) [1 2 3 4])
   => false
 
   "
  {:added "3.0"}
  [x & modifiers]
  (cond (map? x)
        (contains-map x)

        (set? x)
        (contains-set x)

        (sequential? x)
        (contains-vector x (set modifiers))

        :else
        (throw (Exception. "Cannot create contains checker"))))

(defn just-map
  "map check helper function for `just`"
  {:added "3.0"}
  [x]
  (let [ck (map/map-vals base/->checker x)]
    (common/map->Checker
     {:tag :just
      :doc "Checks if the result is a map having strictly the following conditions:"
      :fn  (fn [res]
             (let [data (common/->data res)]
               (and (map? data)
                    (= (set (keys data))
                       (set (keys ck)))
                    (verify-map ck data))))
      :expect ck})))

(defn just-vector
  "vector check helper function for `just`"
  {:added "3.0"}
  ([x] (just-vector x #{}))
  ([x modifiers]
   (let [ck (mapv base/->checker x)]
     (common/map->Checker
      {:tag :just
       :doc "Checks if the result is a sequential having strictly the following conditions:"
       :fn  (fn [res]
              (let [data (common/->data res)]
                (and (= (count data)
                        (count ck))
                     (cond (set? data)
                           (verify-seq ck data (conj modifiers :in-any-order))

                           (sequential? data)
                           (verify-seq ck data modifiers)))))
       :expect ck}))))

(defn just-set
  "set check helper function for `just`
 
   ((just-set #{1 2 3}) [1 2 3])
   => true"
  {:added "3.0"}
  ([x] (just-vector x #{:in-any-order})))

(defn just
  "combination checker for both maps and vectors
 
   ((just {:a odd? :b even?}) {:a 1 :b 4})
   => true
 
   ((just {:a 1 :b even?}) {:a 1 :b 2 :c 3})
   => false
 
   ((just [1 2 3 4]) [1 2 3 4])
   => true
 
   ((just [1 2 3]) [1 2 3 4])
   => false
 
   ((just [3 2 4 1] :in-any-order) [1 2 3 4])
   => true"
  {:added "3.0"}
  [x & modifiers]
  (cond (map? x)
        (just-map x)

        (set? x)
        (just-set x)

        (vector? x)
        (just-vector x (set modifiers))

        :else
        (throw (Exception. "Cannot create just checker"))))

(defmacro contains-in
  "shorthand for checking nested maps and vectors
 
   ((contains-in {:a {:b {:c odd?}}}) {:a {:b {:c 1 :d 2}}})
   => true
 
   ((contains-in [odd? {:a {:b even?}}]) [3 {:a {:b 4 :c 5}}])
   => true"
  {:added "3.0"}
  [x]
  "A macro for nested checking of data in the `contains` form"
  (cond (map? x)
        `(contains ~(map/map-vals (fn [v] `(contains-in ~v)) x))
        (set? x)
        `(contains ~(mapv (fn [v] `(contains-in ~v)) x)
                   :in-any-order)

        (vector? x)
        `(contains ~(mapv (fn [v] `(contains-in ~v)) x))
        :else x))

(defmacro just-in
  "shorthand for exactly checking nested maps and vectors
 
   ((just-in {:a {:b {:c odd?}}}) {:a {:b {:c 1 :d 2}}})
   => false
 
   ((just-in [odd? {:a {:b even?}}]) [3 {:a {:b 4}}])
 
   ((just-in [odd? {:a {:b even?}}]) [3 {:a {:b 4}}])
   => true"
  {:added "3.0"}
  [x]
  "A macro for nested checking of data in the `just` form"
  (cond (map? x)
        `(just ~(map/map-vals (fn [v] `(just-in ~v)) x))

        (set? x)
        `(just ~(mapv (fn [v] `(just-in ~v)) x)
               :in-any-order)

        (vector? x)
        `(just ~(mapv (fn [v] `(just-in ~v)) x))

        :else x))

(defn throws-info
  "checker that determines if an `ex-info` has been thrown
 
   ((throws-info {:a \"hello\" :b \"there\"})
    (common/evaluate '(throw (ex-info \"hello\" {:a \"hello\" :b \"there\"}))))
   => true"
  {:added "3.0"}
  ([]  (throws-info {}))
  ([m]
   (common/checker
    {:tag :raises
     :doc "Checks if an issue has been raised"
     :fn (fn [{:keys [^Throwable data type]}]
           (and (= :exception type)
                (instance? clojure.lang.ExceptionInfo data)
                ((contains m) (ex-data data))))
     :expect {:exception clojure.lang.ExceptionInfo :data m}})))
