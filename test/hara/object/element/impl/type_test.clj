(ns hara.object.element.impl.type-test
  (:use hara.test)
  (:require [hara.object.element.impl.type :as common]
            [hara.object.query :as query]))

^{:refer hara.object.element.impl.type/set-accessible :added "3.0"}
(fact "sets the accessible flag in the class to be true"

  (common/set-accessible (.getDeclaredMethod String "charAt"
                                             (doto (make-array Class 1)
                                               (aset 0 Integer/TYPE)))
                         true))

^{:refer hara.object.element.impl.type/add-annotations :added "3.0"}
(fact "adds additional annotations to the class"

  (common/add-annotations {} String)
  => {})

^{:refer hara.object.element.impl.type/seed :added "3.0"}
(fact "returns the preliminary attributes for creating an element"

  (common/seed :class String)
  => (contains {:name "java.lang.String",
                :tag :class,
                :modifiers #{:instance :public :final :class},
                :static false,
                :delegate java.lang.String})

  (common/seed :method (.getDeclaredMethod String "charAt"
                                           (doto (make-array Class 1)
                                             (aset 0 Integer/TYPE))))
  => (contains {:name "charAt",
                :tag :method,
                :container java.lang.String,
                :modifiers #{:instance :method :public}
                :static false,
                :delegate  java.lang.reflect.Method}))

(comment
  (hara.code/import))