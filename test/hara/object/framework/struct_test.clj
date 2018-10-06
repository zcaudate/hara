(ns hara.object.framework.struct-test
  (:use hara.test)
  (:require [hara.object.framework.struct :refer :all]))

^{:refer hara.object.framework.struct/getter-function-raw :added "3.0"}
(fact "creates a getter function"

  ((getter-function-raw :bytes) "hello")
  => bytes?)

^{:refer hara.object.framework.struct/field-function-raw :added "3.0"}
(fact "creates a field access function"

  ((field-function-raw :value String) "hello")
  => bytes)

^{:refer hara.object.framework.struct/struct-getters :added "3.0"}
(fact "creates a struct given an object and a getter map"

  (struct-getters {:value [:data]
                   :message []
                   :class {:name []}}
                  (ex-info "hello" {:a 1}))
  => {:value {:a 1},
      :message "hello",
      :class {:name "clojure.lang.ExceptionInfo"}})

^{:refer hara.object.framework.struct/struct-field-functions :added "3.0"}
(fact "constructs fields access functions"
  
  (struct-field-functions [:detail-message :value]
                          clojure.lang.ExceptionInfo))

^{:refer hara.object.framework.struct/struct-fields :added "3.0"}
(fact "creates a struct given an object and a field map"

  (struct-fields {:depth []}
                 (ex-info "hello" {:a 1}))
  => (contains {:depth number?})

  (struct-fields {:msg [:detail-message :value]}
                 (ex-info "hello" {:a 1}))
  => (contains {:msg bytes?}))

^{:refer hara.object.framework.struct/struct-accessor :added "3.0"}
(fact "creates an accessor function"

  ((struct-accessor {:value [:data]
                     :msg [:detail-message :value]}
                    :field)
   (ex-info "hello" {:a 1}))
  => (contains {:value {:a 1},
                :msg bytes?}))

^{:refer hara.object.framework.struct/dir :added "3.0"}
(fact "explores the fields of a object given a path"

  (dir "string" [])
  
  (dir "string" [:hash]))

(comment
  (./import))
