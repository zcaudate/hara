(ns hara.core.zip-test
  (:use hara.test)
  (:require [hara.core.zip :refer :all])
  (:refer-clojure :exclude [find get]))

^{:refer hara.core.zip/check-context :added "3.0"}
(fact "checks that the zipper contains valid functions"

  (check-context {})
  => (throws))

^{:refer hara.core.zip/check-optional :added "3.0"}
(fact "checks that the meta contains valid functions"

  (check-optional {})
  => (throws))

^{:refer hara.core.zip/zipper? :added "3.0"}
(fact "checks to see if an object is a zipper"

  (zipper? 1)
  => false)

^{:refer hara.core.zip/zipper :added "3.0"}
(fact "constructs a zipper")

^{:refer hara.core.zip/left-element :added "3.0"}
(fact "element directly left of current position"

  (-> (vector-zip [1 2 3 4])
      (step-inside))
  
  
  (-> (from-status '[1 2 3 | 4])
      (left-element))
  => 3)

^{:refer hara.core.zip/right-element :added "3.0"}
(fact "element directly right of current position"

  (-> (from-status '[1 2 3 | 4])
      (right-element))
  => 4)

^{:refer hara.core.zip/left-elements :added "3.0"}
(fact "all elements left of current position"

  (-> (from-status '[1 2 | 3 4])
      (left-elements))
  => '(1 2))

^{:refer hara.core.zip/right-elements :added "3.0"}
(fact "all elements right of current position"

  (-> (from-status '[1 2 | 3 4])
      (right-elements))
  => '(3 4))

^{:refer hara.core.zip/current-elements :added "3.0"}
(fact "all elements left and right of current position"

  (-> (from-status '[1 2 | 3 4])
      (current-elements))
  => '(1 2 3 4)

  (-> (from-status '[1 [2 | 3] 4])
      (current-elements))
  => '(2 3))

^{:refer hara.core.zip/is :added "3.0"}
(fact "checks zip given a predicate"

  (-> (vector-zip [0 1 2 3 4])
      (step-inside)
      (is zero?))
  => true)

^{:refer hara.core.zip/get :added "3.0"}
(fact "gets the value of the zipper"

  (-> (vector-zip [0 1 2 3 4])
      (step-inside)
      (get))
  => 0)

^{:refer hara.core.zip/is-container? :added "3.0"}
(fact "checks if node on either side is a container"

  (-> (vector-zip [1 2 3])
      (is-container? :right))
  => true

  (-> (vector-zip [1 2 3])
      (is-container? :left))
  => false)

^{:refer hara.core.zip/is-empty-container? :added "3.0"}
(fact "check if current container is empty"

  (-> (vector-zip [])
      (is-empty-container?))
  => true)

^{:refer hara.core.zip/at-left-most? :added "3.0"}
(fact "check if at left-most point of a container"

  (-> (from-status [1 2 ['| 3 4]])
      (at-left-most?))
  => true)

^{:refer hara.core.zip/at-right-most? :added "3.0"}
(fact "check if at right-most point of a container"

  (-> (from-status '[1 2 [3 4 |]])
      (at-right-most?))
  => true)

^{:refer hara.core.zip/at-inside-most? :added "3.0"}
(fact "check if at inside-most point of a container"

  (-> (from-status '[1 2 [3 4 |]])
      (at-inside-most?))
  => true)

^{:refer hara.core.zip/at-inside-most-left? :added "3.0"}
(fact "check if at inside-most left point of a container"

  (-> (from-status '[1 2 [ | 1 2]])
      (at-inside-most-left?))
  => true)

^{:refer hara.core.zip/at-outside-most? :added "3.0"}
(fact "check if at outside-most point of the tree"

  (-> (from-status [1 2 [3 4 '|]])
      (at-outside-most?))
  => false

  (-> (from-status '[1 2 [3 4 |]])
      (step-outside)
      (step-outside)
      (at-outside-most?))
  => true)

^{:refer hara.core.zip/seq-zip :added "3.0"}
(fact "constructs a sequence zipper"

  (seq-zip '(1 2 3 4 5))
  => (contains {:left (),
                :right '((1 2 3 4 5))}))

^{:refer hara.core.zip/vector-zip :added "3.0"}
(fact "constructs a vector based zipper"

  (vector-zip [1 2 3 4 5])
  => (contains {:left (),
                :right '([1 2 3 4 5])}))

^{:refer hara.core.zip/list-child-elements :added "3.0"}
(fact "lists elements of a container "
  
  (-> (vector-zip [1 2 3])
      (list-child-elements :right))
  => '(1 2 3)

  (-> (vector-zip 1)
      (list-child-elements :right))
  => (throws))

^{:refer hara.core.zip/update-child-elements :added "3.0"}
(fact "updates elements of a container"

  (-> (vector-zip [1 2])
      (update-child-elements [1 2 3 4] :right)
      (right-element))
  => [1 2 3 4])

^{:refer hara.core.zip/can-step-left? :added "3.0"}
(fact "check if can step left from current status"

  (-> (from-status '[1 2 [3 | 4]])
      (can-step-left?))
  => true

  (-> (from-status '[1 2 [| 3 4]])
      (can-step-left?))
  => false)

^{:refer hara.core.zip/can-step-right? :added "3.0"}
(fact "check if can step right from current status"

  (-> (from-status '[1 2 [3 | 4]])
      (can-step-right?))
  => true

  (-> (from-status '[1 2 [3 4 |]])
      (can-step-right?))
  => false)

^{:refer hara.core.zip/can-step-inside? :added "3.0"}
(fact "check if can step down from current status"

  (-> (from-status '[1 2 [3 4 |]])
      (can-step-inside?))
  => false

  (-> (from-status '[1 2 | [3 4]])
      (can-step-inside?))
  => true)

^{:refer hara.core.zip/can-step-inside-left? :added "3.0"}
(fact "check if can step left inside a container"

  (-> (from-status '[[3 4] |])
      (can-step-inside-left?))
  => true)

^{:refer hara.core.zip/can-step-outside? :added "3.0"}
(fact "check if can step up from current status"

  (-> (from-status '[1 2 [3 4 |]])
      (can-step-outside?))
  => true)

^{:refer hara.core.zip/step-left :added "3.0"}
(fact "step left from current status"

  (-> (from-status '[1 2 [3 4 |]])
      (step-left)
      (status))
  => '([1 2 [3 | 4]]))

^{:refer hara.core.zip/step-right :added "3.0"}
(fact "step right from current status"

  (-> (from-status '[1 2 [| 3 4]])
      (step-right)
      (status))
  => '([1 2 [3 | 4]]))

^{:refer hara.core.zip/step-inside :added "3.0"}
(fact "step down from current status"

  (-> (from-status '[1 2 | [3 4]])
      (step-inside)
      (status))
  => '([1 2 [| 3 4]]))

^{:refer hara.core.zip/step-inside-left :added "3.0"}
(fact "steps into the form on the left side"
  
  (-> (from-status '[[1 2] |])
      (step-inside-left)
      (status))
  => '([[1 2 |]]))

^{:refer hara.core.zip/step-outside :added "3.0"}
(fact "step out to the current container"

  (-> (from-status '[1 2 [| 3 4]])
      (step-outside)
      (status))
  => '([1 2 | [3 4]]))

^{:refer hara.core.zip/step-outside-right :added "3.0"}
(fact "the right of the current container"

  (-> (from-status '[1 2 [| 3 4]])
      (step-outside-right)
      (status))
  => '([1 2 [3 4] |]))

^{:refer hara.core.zip/step-left-most :added "3.0"}
(fact "step to left-most point of current container"

  (-> (from-status '[1 2 [3 4 |]])
      (step-left-most)
      (status))
  => '([1 2 [| 3 4]]))

^{:refer hara.core.zip/step-right-most :added "3.0"}
(fact "step to right-most point of current container"

  (-> (from-status '[1 2 [| 3 4]])
      (step-right-most)
      (status))
  => '([1 2 [3 4 |]]))

^{:refer hara.core.zip/step-inside-most :added "3.0"}
(fact "step to at-inside-most point of current container"

  (-> (from-status '[1 2 | [[3] 4]])
      (step-inside-most)
      (status))
  => '([1 2 [[| 3] 4]]))

^{:refer hara.core.zip/step-inside-most-left :added "3.0"}
(fact "steps all the way inside to the left side"
  
  (-> (from-status '[[1 [2]] | 3 4])
      (step-inside-most-left)
      (status))
  => '([[1 [2 |]] 3 4]))

^{:refer hara.core.zip/step-outside-most :added "3.0"}
(fact "step to outside-most point of the tree"

  (-> (from-status '[1 2 [| 3 4]])
      (step-outside-most)
      (status))
  => '(| [1 2 [3 4]]))

^{:refer hara.core.zip/step-outside-most-right :added "3.0"}
(fact "step to outside-most point of the tree to the right"
  
  (-> (from-status '[1 2 [| 3 4]])
      (step-outside-most-right)
      (status))
  => '([1 2 [3 4]] |))

^{:refer hara.core.zip/step-end :added "3.0"}
(fact "steps status to container directly at end"
  
  (->> (from-status '[1 | [[]]])
       (step-end)
       (status))
  => '([1 [[|]]]))

^{:refer hara.core.zip/insert-left :added "3.0"}
(fact "insert element/s left of the current status"

  (-> (from-status '[1 2  [[| 3] 4]])
      (insert-left 1 2 3)
      (status))
  => '([1 2 [[1 2 3 | 3] 4]]))

^{:refer hara.core.zip/insert-right :added "3.0"}
(fact "insert element/s right of the current status"

  (-> (from-status '[| 1 2 3])
      (insert-right 1 2 3)
      (status))
  => '([| 3 2 1 1 2 3]))

^{:refer hara.core.zip/delete-left :added "3.0"}
(fact "delete element/s left of the current status"

  (-> (from-status '[1 2 | 3])
      (delete-left)
      (status))
  => '([1 | 3]))

^{:refer hara.core.zip/delete-right :added "3.0"}
(fact "delete element/s right of the current status"

  (-> (from-status '[1 2 | 3])
      (delete-right)
      (status))
  => '([1 2 |]))

^{:refer hara.core.zip/replace-left :added "3.0"}
(fact "replace element left of the current status"

  (-> (from-status '[1 2 | 3])
      (replace-left "10")
      (status))
  => '([1 "10" | 3]))

^{:refer hara.core.zip/replace-right :added "3.0"}
(fact "replace element right of the current status"

  (-> (from-status '[1 2 | 3])
      (replace-right "10")
      (status))
  => '([1 2 | "10"]))

^{:refer hara.core.zip/hierarchy :added "3.0"}
(fact "replace element right of the current status"

  (->> (from-status '[1 [[|]]])
       (hierarchy)
       (map right-element))
  => [[] [[]] [1 [[]]]])

^{:refer hara.core.zip/at-end? :added "3.0"}
(fact "replace element right of the current status"

  (->> (from-status '[1 [[|]]])
       (at-end?))
  => true

  (->> (from-status '[1 [[[2 |]] 3]])
       (at-end?))
  => false)

^{:refer hara.core.zip/surround :added "3.0"}
(fact "nests elements in current block within another container"

  (-> (vector-zip 3)
      (insert-left 1 2)
      (surround)
      (status))
  => '(| [1 2 3])
  
  (->> (from-status '[1 [1 2 | 3 4]])
       (surround)
       (status))
  => '([1 [| [1 2 3 4]]]))

^{:refer hara.core.zip/root-element :added "3.0"}
(fact "accesses the top level node"

  (-> (vector-zip [[[3] 2] 1])
      (step-inside-most)
      (root-element))
  => [[[3] 2] 1])

^{:refer hara.core.zip/status :added "3.0"}
(fact "returns the form with the status showing"

  (-> (vector-zip [1 [[2] 3]])
      (step-inside)
      (step-right)
      (step-inside)
      (step-inside)
      (status))
  => '([1 [[| 2] 3]]))

^{:refer hara.core.zip/status-string :added "3.0"}
(fact "returns the string form of the status"

  (-> (vector-zip [1 [[2] 3]])
      (step-inside)
      (step-right)
      (status-string))
  => "[1 | [[2] 3]]")

^{:refer hara.core.zip/step-next :added "3.0"}
(fact "step status through the tree in depth first order"

  (->> (from-status '[| 1 [2 [6 7] 3] [4 5]])
       (iterate step-next)
       (take-while identity)
       (map right-element))
  => '(1 [2 [6 7] 3] 2 [6 7] 6 7 3 [4 5] 4 5))

^{:refer hara.core.zip/step-prev :added "3.0"}
(fact "step status in reverse through the tree in depth first order"

  (->> (from-status '[1 [2 [6 7] 3] [4 | 5]])
       (iterate step-prev)
       (take 10)
       (map right-element))
  => '(5 4 [4 5] 3 7 6 [6 7] 2 [2 [6 7] 3] 1))

^{:refer hara.core.zip/find :added "3.0"}
(fact "helper function for the rest of the `find` series")

^{:refer hara.core.zip/find-left :added "3.0"}
(fact "steps status left to search predicate"
  
  (-> (from-status '[0 1 [2 3] [4 5] 6 |])
      (find-left odd?)
      (status))
  => '([0 | 1 [2 3] [4 5] 6])

  (-> (from-status '[0 1 [2 3] [4 5] 6 |])
      (find-left keyword?))
  => nil)

^{:refer hara.core.zip/find-right :added "3.0"}
(fact "steps status right to search for predicate"

  (-> (from-status '[0 | 1 [2 3] [4 5] 6])
      (find-right even?)
      (status))
  => '([0 1 [2 3] [4 5] | 6]))

^{:refer hara.core.zip/find-next :added "3.0"}
(fact "step status through the tree in depth first order to the first matching element"

  (-> (vector-zip [1 [2 [6 7] 3] [4 5]])
      (find-next #(= 7 %))
      (status))
  => '([1 [2 [6 | 7] 3] [4 5]])

  (-> (vector-zip [1 [2 [6 7] 3] [4 5]])
      (find-next keyword))
  => nil)

^{:refer hara.core.zip/find-prev :added "3.0"}
(fact "step status through the tree in reverse order to the last matching element"

  (-> (from-status '[1 [2 [6 | 7] 3] [4 5]])
      (find-prev even?)
      (status))
  => '([1 [2 [| 6 7] 3] [4 5]]))

^{:refer hara.core.zip/from-status :added "3.0"}
(fact "returns a zipper given a data structure with | as the status"

  (from-status '[1 2 3 | 4])
  => (contains {:left '(3 2 1),
                :right '(4)}))

^{:refer hara.core.zip/prewalk :added "3.0"}
(fact "emulates clojure.walk/prewalk behavior with zipper"
  
  (-> (vector-zip [[1 2] [3 4]])
      (prewalk (fn [v] (if (vector? v)
                         (conj v 100)
                         (+ v 100))))
      (root-element))
  => [[101 102 200] [103 104 200] 200])

^{:refer hara.core.zip/postwalk :added "3.0"}
(fact "emulates clojure.walk/postwalk behavior with zipper"

  (-> (vector-zip [[1 2] [3 4]])
      (postwalk (fn [v] (if (vector? v)
                          (conj v 100)
                          (+ v 100))))
      (root-element))
  => [[101 102 100] [103 104 100] 100])

^{:refer hara.core.zip/matchwalk :added "3.0"}
(fact "performs a match at each level"
  
  (-> (matchwalk (vector-zip [1 [2 [3 [4]]]])
                 [(fn [zip]
                    (= 2 (first (right-element zip))))
                  (fn [zip]
                    (= 4 (first (right-element zip))))]
                 delete-left)
      (root-element))
  => [1 [2 [[4]]]])

^{:refer hara.core.zip/levelwalk :added "3.0"}
(fact "performs a match at the same level"

  (-> (vector-zip [1 2 3 4])
      (step-inside)
      (levelwalk [(fn [zip]
                    (odd? (right-element zip)))]
                 delete-right)
      (root-element))
  => [2 4])
