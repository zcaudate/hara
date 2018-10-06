(ns hara.object.query.input-test
  (:use hara.test)
  (:require [hara.object.query.input :refer :all]))

^{:refer hara.object.query.input/args-classify :added "3.0"}
(fact "classifies inputs into `.?` and `.*` macros through matching argument parameters to different inputs"

  (map (fn [[i x]] [i (args-classify x)])
       {0  :by-name     ;; sort - :by-params, :by-modifiers, :by-type
        1  :tag         ;; display - :name, :params, :modifiers, :type, :attributes,
                        ;;           :origins, :container, :delegate
        2  :first       ;; gets the first element
        3  :#           ;; merge all elements into a single multi element
        4  "toString"   ;; matches exact name of function
        5  #"to*"       ;; matches name containing regex
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
           :in-any-order))

^{:refer hara.object.query.input/args-convert :added "3.0"}
(fact "converts any symbol in `args` to its primitive class"

  (args-convert ['byte]) => [Byte/TYPE]
  
  (args-convert ['byte Class]) => [Byte/TYPE Class])

^{:refer hara.object.query.input/args-group :added "3.0"}
(fact "group inputs together into their respective categories"

  (args-group ["toString" :public :tag #{String}])
  => {:name ["toString"]
      :modifiers [:public]
      :select-terms [:tag]
      :origins [#{java.lang.String}]}

  (args-group ['int 3 :#])
  {:type [int], :num-params [3], :merge [:#]})
