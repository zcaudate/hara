(ns hara.object.query.filter
  (:require [clojure.set :as set]
            [hara.core.base.check :as check]
            [hara.object.element.common :as common]
            [hara.object.element.class :as class]))

(defn- has-predicate?
  [f value]
  (f value))

(defn- has-name?
  [name value]
  (cond (check/regexp? name)
        (not (nil? (re-find name value)))

        (string? name)
        (= name value)))

(defn- has-modifier?
  [modifier value]
  (contains? value modifier))

(defn- has-params?
  [params value]
  (= (mapv class/class-convert params) value))

(defn- has-num-params?
  [num-params value]
  (= num-params (count value)))

(defn- has-any-params?
  [any-params value]
  (if (some #((set (map class/class-convert (next any-params))) %) value)
    true false))

(defn- has-all-params?
  [all-params value]
  (if (every? #((set value) %) (map class/class-convert (next all-params)))
    true false))

(defn- has-type?
  [type value]
  (= (class/class-convert type) value))

(defn- has-origins?
  [origins value]
  (if (empty? (set/intersection origins (set value)))
    false true))

(defn- filter-by
  ([f k grp eles]
   (filter-by f k grp k eles))
  ([f kg grp ke eles]
   (if-let [chk (get grp kg)]
     (filter (fn [ele]
               (every? #(f % (if ke (get ele ke) ele)) chk))
             eles)
     eles)))

(defn filter-terms-fn
  "listing outputs based upon different predicate conditions
 
   ((filter-terms-fn {:name [\"a\"]})
    [{:name \"a\"} {:name \"b\"}])
   => [{:name \"a\"}]
 
   ((filter-terms-fn {:predicate [(fn [x] (= \"a\" (:name x)))]})
    [{:name \"a\"} {:name \"b\"}])
   => [{:name \"a\"}]
 
   ((filter-terms-fn {:origins [#{:a :b}]})
    [{:origins #{:a}} {:origins #{:c}}])
   => [{:origins #{:a}}]
 
   ((filter-terms-fn {:modifiers [:a]})
    [{:modifiers #{:a}} {:modifiers #{:c}}])
   => [{:modifiers #{:a}}]"
  {:added "3.0"}
  [grp]
  (fn [eles]
    (->> eles
         (filter-by has-name?       :name grp)
         (filter-by has-predicate?  :predicate grp nil)
         (filter-by has-origins?    :origins grp)
         (filter-by has-type?       :type grp)
         (filter-by has-params?     :params grp)
         (filter-by has-any-params? :any-params grp :params)
         (filter-by has-all-params? :all-params grp :params)
         (filter-by has-num-params? :num-params grp :params)
         (filter-by has-modifier?   :modifiers grp))))
