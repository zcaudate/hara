(ns hara.object.query.input
  (:require [hara.core.base.check :as check]
            [hara.data.base.map :as map]
            [hara.object.element.class :as class]
            [hara.core.base.primitive :as primitive]))

(def sort-terms #{:by-name :by-params :by-modifiers :by-type})

(def select-terms #{:tag
                    :name
                    :params
                    :modifiers
                    :type
                    :attributes
                    :origins
                    :container
                    :delegate})

(defn args-classify
  "classifies inputs into `.?` and `.*` macros through matching argument parameters to different inputs
 
   (map (fn [[i x]] [i (args-classify x)])
        {0  :by-name     ;; sort - :by-params, :by-modifiers, :by-type
         1  :tag         ;; display - :name, :params, :modifiers, :type, :attributes,
                         ;;           :origins, :container, :delegate
         2  :first       ;; gets the first element
         3  :#           ;; merge all elements into a single multi element
         4  \"toString\"   ;; matches exact name of function
         5  #\"to*\"       ;; matches name containing regex
        6  #(-> % :type (= :field))  ;; matches on predicate element
         7  #{Class}     ;; match origin of element
         8  [:any 'int]  ;; match any parameter type
         9  [:all 'int 'long] ;; match all parameter types
         10 ['byte 'byte] ;; match exact paramter types
         11 3             ;; match number of parameters
         13 'int          ;; match on the type of element
         14 :public       ;; match on modifiers (:public, :static, etc...)
 })
   => (just [[0 :sort-terms] [1 :select-terms] [2 :first] [3 :merge] [4 :name]
             [5 :name] [6 :predicate] [7 :origins] [8 :any-params] [9 :all-params]
             [10 :params] [11 :num-params] [13 :type] [14 :modifiers]]
            :in-any-order)"
  {:added "3.0"}
  [arg]
  (cond (sort-terms arg)                 :sort-terms
        (select-terms arg)               :select-terms
        (= :first arg)                   :first
        (or (= :# arg) (= :merge arg))   :merge

        (or (string? arg) (check/regexp? arg))  :name
        (fn? arg)                        :predicate
        (set? arg)                       :origins
        (and (vector? arg)
             (= :any (first arg)))       :any-params
        (and (vector? arg)
             (= :all (first arg)))       :all-params
        (number? arg)                    :num-params
        (vector? arg)                    :params

        (or (class? arg) (symbol? arg))  :type

        (keyword? arg)                   :modifiers
        (check/hash-map? arg)            :attribute))

(defn args-convert
  "converts any symbol in `args` to its primitive class
 
   (args-convert ['byte]) => [Byte/TYPE]
 
   (args-convert ['byte Class]) => [Byte/TYPE Class]"
  {:added "3.0"}
  [args]
  (let [single-fn #(or (class/class-convert %) %)]
    (mapv (fn [v]
            (cond (vector? v)
                  (mapv single-fn v)

                  (set? v)
                  (set (map single-fn v))

                  (symbol? v)
                  (or (primitive/primitive-type v :class)
                      v)
                  :else v))
          args)))

(defn args-group
  "group inputs together into their respective categories
 
   (args-group [\"toString\" :public :tag #{String}])
   => {:name [\"toString\"]
       :modifiers [:public]
       :select-terms [:tag]
       :origins [#{java.lang.String}]}
 
   (args-group ['int 3 :#])
   {:type [int], :num-params [3], :merge [:#]}"
  {:added "3.0"}
  [args]
  (-> (group-by args-classify args)
      (map/update-in-if [:params] args-convert)
      (map/update-in-if [:type] args-convert)))
