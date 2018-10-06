(ns hara.object.query.order-test
  (:use hara.test)
  (:require [hara.object.query.order :as order]
            [hara.object.query :as query]))

^{:refer hara.object.query.order/sort-fn :added "3.0"}
(fact "returns a function taking two inputs `x` and `y`, comparing the outputs after applying `f` to both"

  ((order/sort-fn :id) {:id 1} {:id 2}) => -1)

^{:refer hara.object.query.order/sort-terms-fn :added "3.0"}
(fact "This is a little broken, it is supposed to sort on various keys, but currently only works with :name"

  ((order/sort-terms-fn {:sort-terms nil})
   [{:name 3} {:name 1} {:name 2}])
  => [{:name 1} {:name 2} {:name 3}])
  
^{:refer hara.object.query.order/first-terms-fn :added "3.0"}
(fact "creates a function that returns the first element of the list"

  ((order/first-terms-fn {:first true}) [1 2 3])
  => 1)

^{:refer hara.object.query.order/merge-terms-fn :added "3.0"}
(fact "creates a function that returns the first element of the list"

  (-> (query/query-class (type []) ["create"])
      ((order/merge-terms-fn {:merge true})))
  ;;=> #[create :: ([java.util.List]), ([clojure.lang.ISeq]),
  ;;               ([clojure.lang.IReduceInit]), ([java.lang.Object[]]),
  ;;               ([java.lang.Iterable])]
)

^{:refer hara.object.query.order/select-terms-fn :added "3.0"}
(fact "creates a function that selects terms to output"

  (-> (query/query-class (type []) ["create"])
      ((order/select-terms-fn {:select-terms [:name]})))
  => ["create"])

^{:refer hara.object.query.order/order :added "3.0"}
(fact "formats an output for "

  (->> (query/query-class (type []) ["create"])
       (order/order {:select-terms [:params]}))
  ;;=> ([java.util.List] [clojure.lang.ISeq]
  ;;    [clojure.lang.IReduceInit] [[Ljava.lang.Object;]
  ;;    [java.lang.Iterable])
)

(comment
  (hara.code/import))
