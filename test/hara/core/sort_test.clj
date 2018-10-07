(ns hara.core.sort-test
  (:use hara.test)
  (:require [hara.core.sort :refer :all]))

^{:refer hara.core.sort/hierarchical-top :added "3.0"}
(fact "find the top node for the hierarchy of descendants"
  
  (hierarchical-top 
    {1 #{2 3 4 5 6}
     2 #{3 5 6}
     3 #{5 6}
     4 #{}
     5 #{6}
     6 #{}}) => 1)

^{:refer hara.core.sort/hierarchical-sort :added "3.0"}
(fact "prunes a hierarchy of descendants into a directed graph"
  
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
      6 #{}})

^{:refer hara.core.sort/topological-top :added "3.0"}
(fact "nodes that have no other nodes that are dependent on them"
  (topological-top {:a #{} :b #{:a}})
  => #{:b})

^{:refer hara.core.sort/topological-sort :added "3.0"}
(fact "sorts a directed graph into its dependency order"

  (topological-sort {:a #{:b :c},
                     :b #{:d :e},
                     :c #{:e :f},
                     :d #{},
                     :e #{:f},
                     :f nil})
  => [:f :d :e :b :c :a]

  (topological-sort {:a #{:b},
                     :b #{:a}})
  => (throws))
