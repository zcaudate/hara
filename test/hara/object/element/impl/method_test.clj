(ns hara.object.element.impl.method-test
  (:use hara.test)
  (:require [hara.object.element.impl.method :as method]
            [hara.object.query :as query]))

^{:refer hara.object.element.impl.method/invoke-static-method :added "3.0"}
(fact "invoke function for a static method"

  (-> (query/query-class clojure.java.api.Clojure ["read" :#])
      (method/invoke-static-method ["{:a 1}"]))
  => {:a 1})

^{:refer hara.object.element.impl.method/invoke-instance-method :added "3.0"}
(fact "invoke function for an instance method"

  (-> (query/query-class String ["charAt" :#])
      (method/invoke-instance-method ["0123" 1]))
  => \1)

^{:refer hara.object.element.impl.method/to-static-method :added "3.0"}
(fact "creates the parameters for a static method"

  (-> (query/query-class clojure.java.api.Clojure ["read" :#])
      :delegate
      (method/to-static-method {}))
  => {:params [String]
      :origins [clojure.java.api.Clojure]})

^{:refer hara.object.element.impl.method/to-instance-method :added "3.0"}
(fact "creates the parameters for an instance method"

  (-> (query/query-class String ["charAt" :#])
      :delegate
      (method/to-instance-method {:container String}))
  => {:container String
      :params [String Integer/TYPE]
      :origins [CharSequence String]})

^{:refer hara.object.element.impl.method/to-pre-element :added "3.0"}
(fact "creates the parameters for methods"

  (-> (query/query-class String ["charAt" :#])
      :delegate
      (method/to-pre-element))
  => (contains {:name "charAt"
                :tag :method
                :container String
                :modifiers #{:instance :method :public}
                :static false
                :delegate java.lang.reflect.Method
                :params [String Integer/TYPE]
                :origins [CharSequence String]}))

(comment
  (hara.code/import))