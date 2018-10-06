(ns hara.core.base.result-test
  (:use hara.test)
  (:require [hara.core.base.result :refer :all]))

^{:refer hara.core.base.result/result :added "3.0"}
(fact "creates a result used for printing"

  (result {:status :warn :data [1 2 3 4]})
  ;; #result{:status :warn, :data [1 2 3 4]}
  => hara.core.base.result.Result)

^{:refer hara.core.base.result/result? :added "3.0"}
(fact "checks if an object is a result"

  (-> (result {:status :warn :data [1 2 3 4]})
      result?)
  => true)

^{:refer hara.core.base.result/->result :added "3.0"}
(fact "converts data into a result"
  
  (->result :hello [1 2 3])
  ;;#result.return{:data [1 2 3], :key :hello}
  => hara.core.base.result.Result)
