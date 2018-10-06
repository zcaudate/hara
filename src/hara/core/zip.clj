(ns hara.core.zip
  (:require [hara.core.base.event :as event]
            [hara.function :as fn])
  (:refer-clojure :exclude [find get]))

(defrecord Zipper [context prefix display]
  Object
  (toString [obj]
    (str (or prefix "#zip")
         (if display
           (display obj)
           (into {} (dissoc obj :context :prefix :display))))))

(defmethod print-method Zipper
  [v ^java.io.Writer w]
  (.write w (str v)))

(defn check-context
  "checks that the zipper contains valid functions
 
   (check-context {})
   => (throws)"
  {:added "3.0"}
  [context]
  (let [missing (->> [:create-container
                      :create-element
                      :cursor
                      :is-container?
                      :is-empty-container?
                      :is-element?
                      :list-elements
                      :update-elements
                      :add-element
                      :at-left-most? 
                      :at-right-most?
                      :at-inside-most? 
                      :at-inside-most-left?
                      :at-outside-most?]
                     (remove context))]
    (if (seq missing)
      (throw (ex-info "Missing keys." {:keys missing}))
      context)))

(defn check-optional
  "checks that the meta contains valid functions
 
   (check-optional {})
   => (throws)"
  {:added "3.0"}
  [context]
  (let [missing (->> [:update-step-left
                      :update-step-right
                      :update-step-inside
                      :update-step-inside-left
                      :update-step-outside
                      :update-delete-left
                      :update-delete-right
                      :update-insert-left
                      :update-insert-right]
                     (remove context))]
    (if (seq missing)
      (throw (ex-info "Missing keys." {:keys missing}))
      context)))

(defn zipper?
  "checks to see if an object is a zipper
 
   (zipper? 1)
   => false"
  {:added "3.0"}
  [x]
  (instance? Zipper x))

(defn zipper
  "constructs a zipper"
  {:added "3.0"}
  ([root context]
   (zipper root context {}))
  ([root context opts]
   (map->Zipper (merge opts
                       {:left    ()
                        :right   (list root)
                        :context (check-context context)}))))


(defn left-element
  "element directly left of current position
 
   (-> (vector-zip [1 2 3 4])
       (step-inside))
   
   
   (-> (from-status '[1 2 3 | 4])
       (left-element))
   => 3"
  {:added "3.0"}
  [zip]
  (first (:left zip)))

(defn right-element
  "element directly right of current position
 
   (-> (from-status '[1 2 3 | 4])
       (right-element))
   => 4"
  {:added "3.0"}
  [zip]
  (first (:right zip)))

(defn left-elements
  "all elements left of current position
 
   (-> (from-status '[1 2 | 3 4])
       (left-elements))
   => '(1 2)"
  {:added "3.0"}
  [zip]
  (reverse (:left zip)))

(defn right-elements
  "all elements right of current position
 
   (-> (from-status '[1 2 | 3 4])
       (right-elements))
   => '(3 4)"
  {:added "3.0"}
  [zip]
  (:right zip))

(defn current-elements
  "all elements left and right of current position
 
   (-> (from-status '[1 2 | 3 4])
       (current-elements))
   => '(1 2 3 4)
 
   (-> (from-status '[1 [2 | 3] 4])
       (current-elements))
   => '(2 3)"
  {:added "3.0"}
  [{:keys [left right] :as zip}]
  (concat (reverse left) right))

(defn is
  "checks zip given a predicate
 
   (-> (vector-zip [0 1 2 3 4])
       (step-inside)
       (is zero?))
   => true"
  {:added "3.0"}
  ([zip pred]
   (is zip pred :right))
  ([zip pred step]
   (let [elem (first (clojure.core/get zip step))]
     (try
       (or  (= elem pred)

            (if (and (ifn? pred)
                     (not (coll? pred)))
              (pred elem))
            
            (zero? (compare elem pred)))
       (catch Throwable t
         false)))))

(defn get
  "gets the value of the zipper
 
   (-> (vector-zip [0 1 2 3 4])
       (step-inside)
       (get))
   => 0"
  {:added "3.0"}
  ([zip]
   (get zip identity :right))
  ([zip arg]
   (if (or (= arg :left)
           (= arg :right))
     (get zip identity arg)
     (get zip arg :right)))
  ([zip func step]
   (let [elem (first (clojure.core/get zip step))]
     (func elem))))

(defn is-container?
  "checks if node on either side is a container
 
   (-> (vector-zip [1 2 3])
       (is-container? :right))
   => true
 
   (-> (vector-zip [1 2 3])
       (is-container? :left))
   => false"
  {:added "3.0"}
  ([zip]
   (is-container? zip :right))
  ([{:keys [context] :as zip} step]
   (is zip (-> context :is-container?) step)))

(defn is-empty-container?
  "check if current container is empty
 
   (-> (vector-zip [])
       (is-empty-container?))
   => true"
  {:added "3.0"}
  ([zip]
   (is-empty-container? zip :right))
  ([{:keys [context] :as zip} step]
   (is zip (-> context :is-empty-container?) step)))

(defn at-left-most?
  "check if at left-most point of a container
 
   (-> (from-status [1 2 ['| 3 4]])
       (at-left-most?))
   => true"
  {:added "3.0"}
  [zip]
  (empty? (:left zip)))

(defn at-right-most?
  "check if at right-most point of a container
 
   (-> (from-status '[1 2 [3 4 |]])
       (at-right-most?))
   => true"
  {:added "3.0"}
  [zip]
  (empty? (:right zip)))

(defn at-inside-most?
  "check if at inside-most point of a container
 
   (-> (from-status '[1 2 [3 4 |]])
       (at-inside-most?))
   => true"
  {:added "3.0"}
  [zip]
  (or (empty? (:right zip))
      (not (is-container? zip :right))))

(defn at-inside-most-left?
  "check if at inside-most left point of a container
 
   (-> (from-status '[1 2 [ | 1 2]])
       (at-inside-most-left?))
   => true"
  {:added "3.0"}
  [zip]
  (or (empty? (:left zip))
      (not (is-container? zip :left))))

(defn at-outside-most?
  "check if at outside-most point of the tree
 
   (-> (from-status [1 2 [3 4 '|]])
       (at-outside-most?))
   => false
 
   (-> (from-status '[1 2 [3 4 |]])
       (step-outside)
       (step-outside)
       (at-outside-most?))
   => true"
  {:added "3.0"}
  [zip]
  (nil? (:parent zip)))

(defonce +base+
  {:cursor               '|
   :at-left-most?        at-left-most?
   :at-right-most?       at-right-most?
   :at-inside-most?      at-inside-most?
   :at-inside-most-left? at-inside-most-left?
   :at-outside-most?     at-outside-most?})

(defn seq-zip
  "constructs a sequence zipper
 
   (seq-zip '(1 2 3 4 5))
   => (contains {:left (),
                 :right '((1 2 3 4 5))})"
  {:added "3.0"}
  ([root]
   (seq-zip root nil))
  ([root opts]
   (zipper root
           (merge {:create-container    list
                   :create-element      identity
                   :is-container?       seq?
                   :is-empty-container? empty?
                   :is-element?         (complement nil?)
                   :list-elements       identity
                   :update-elements     (fn [container new-elements] (apply list new-elements))
                   :add-element         (fn [container element] (concat container [element]))}
                  +base+)
           opts)))

(defn vector-zip
  "constructs a vector based zipper
 
   (vector-zip [1 2 3 4 5])
   => (contains {:left (),
                 :right '([1 2 3 4 5])})"
  {:added "3.0"}
  ([root]
   (vector-zip root nil))
  ([root opts]
   (zipper root
           (merge {:create-container  vector
                   :create-element    identity
                   :is-container?     vector?
                   :is-empty-container? empty?
                   :is-element?         (complement nil?)
                   :list-elements     seq
                   :update-elements   (fn [_ new-elements] (vec new-elements))
                   :add-element       conj}
                  +base+)
           opts)))

(defn list-child-elements
  "lists elements of a container 
   
   (-> (vector-zip [1 2 3])
       (list-child-elements :right))
   => '(1 2 3)
 
   (-> (vector-zip 1)
       (list-child-elements :right))
   => (throws)"
  {:added "3.0"}
  ([zip]
   (list-child-elements zip :right))
  ([{:keys [context] :as zip} direction]
   (let [elem (first (clojure.core/get zip direction))
         check-fn  (:is-container? context)
         list-fn   (:list-elements context)]
     (if (check-fn elem)
       (list-fn elem)
       (throw (ex-info "Not a container." {:element elem}))))))

(defn update-child-elements
  "updates elements of a container
 
   (-> (vector-zip [1 2])
       (update-child-elements [1 2 3 4] :right)
       (right-element))
   => [1 2 3 4]"
  {:added "3.0"}
  ([zip child-elements]
   (update-child-elements zip child-elements :right))
  ([{:keys [context] :as zip} child-elements direction] 
   (update-in zip
              [direction]
              (fn [elements]
                (let [check-fn  (:is-container? context)
                      update-fn (:update-elements context)
                      old   (first elements)
                      _     (if-not (check-fn old)
                              (throw (ex-info "Not a container." {:element old})))
                      new   (update-fn old
                                       child-elements)]
                  (cons new (rest elements)))))))

(defn can-step-left?
  "check if can step left from current status
 
   (-> (from-status '[1 2 [3 | 4]])
       (can-step-left?))
   => true
 
   (-> (from-status '[1 2 [| 3 4]])
       (can-step-left?))
   => false"
  {:added "3.0"}
  [{:keys [context] :as zip}]
  (not ((:at-left-most? context) zip)))

(defn can-step-right?
  "check if can step right from current status
 
   (-> (from-status '[1 2 [3 | 4]])
       (can-step-right?))
   => true
 
   (-> (from-status '[1 2 [3 4 |]])
       (can-step-right?))
   => false"
  {:added "3.0"}
  [{:keys [context] :as zip}]
  (not ((:at-right-most? context) zip)))

(defn can-step-inside?
  "check if can step down from current status
 
   (-> (from-status '[1 2 [3 4 |]])
       (can-step-inside?))
   => false
 
   (-> (from-status '[1 2 | [3 4]])
       (can-step-inside?))
   => true"
  {:added "3.0"}
  [{:keys [context] :as zip}]
  (not ((:at-inside-most? context) zip)))

(defn can-step-inside-left?
  "check if can step left inside a container
 
   (-> (from-status '[[3 4] |])
       (can-step-inside-left?))
   => true"
  {:added "3.0"}
  [{:keys [context] :as zip}]
  (not ((:at-inside-most-left? context) zip)))

(defn can-step-outside?
  "check if can step up from current status
 
   (-> (from-status '[1 2 [3 4 |]])
       (can-step-outside?))
   => true"
  {:added "3.0"}
  [{:keys [context] :as zip}]
  (not ((:at-outside-most? context) zip)))

(defn step-left
  "step left from current status
 
   (-> (from-status '[1 2 [3 4 |]])
       (step-left)
       (status))
   => '([1 2 [3 | 4]])"
  {:added "3.0"}
  ([{:keys [left context right] :as zip}]
   (cond ((:at-left-most? context) zip)
         (event/raise {:fn  :step-left
                       :op  :step
                       :tag :at-left-most
                       :zip zip}
                      "At left-most status."
                      (option :zip [] zip)
                      (default :zip))
         
         :else
         (let [elem (first left)]
           (-> zip
               (assoc :left (rest left))
               (assoc :right (cons elem right))
               (fn/call (:update-step-left context) elem)))))
  ([zip n]
   (nth (iterate step-left zip) n)))

(defn step-right
  "step right from current status
 
   (-> (from-status '[1 2 [| 3 4]])
       (step-right)
       (status))
   => '([1 2 [3 | 4]])"
  {:added "3.0"}
  ([{:keys [left right context] :as zip}]
   (cond ((:at-right-most? context) zip)
         (event/raise {:fn  :step-right
                       :op  :step
                       :tag :at-right-most
                       :zip zip}
                      "At right-most status."
                      (option :zip [] zip)
                      (default :zip))
         
         :else
         (let [elem (first right)]
           (-> zip
               (assoc :left  (cons elem left))
               (assoc :right (rest right))
               (fn/call (:update-step-right context) elem)))))
  ([zip n]
   (nth (iterate step-right zip) n)))

(defn step-inside
  "step down from current status
 
   (-> (from-status '[1 2 | [3 4]])
       (step-inside)
       (status))
   => '([1 2 [| 3 4]])"
  {:added "3.0"}
  ([{:keys [context] :as zip}]
   (cond ((:at-right-most? context) zip)
         (event/raise {:fn  :step-inside
                       :op  :step
                       :tag :at-right-most
                       :zip zip}
                      "At right-most status."
                      (option :zip [] zip)
                      (default :zip))

         ((:at-inside-most? context) zip)
         (event/raise {:fn  :step-inside
                       :op  :step
                       :tag :at-inside-most
                       :zip zip}
                      "Cannot step inside right element."
                      (option :zip [] zip)
                      (default :zip))
         
         :else
         (let [elem     (right-element zip)
               children (list-child-elements zip :right)]
           (-> zip
               (assoc :left ()
                      :right children
                      :parent zip)
               (fn/call (:update-step-inside context) elem)))))
  ([zip n]
   (nth (iterate step-inside zip) n)))

(defn step-inside-left
  "steps into the form on the left side
   
   (-> (from-status '[[1 2] |])
       (step-inside-left)
       (status))
   => '([[1 2 |]])"
  {:added "3.0"}
  ([{:keys [context] :as zip}]
   (cond ((:at-left-most? context) zip)
         (event/raise {:fn  :step-inside-left
                       :op  :step
                       :tag :at-left-most
                       :zip zip}
                      "At left-most status."
                      (option :zip [] zip)
                      (default :zip))

         ((:at-inside-most-left? context) zip)
         (event/raise {:fn  :step-inside-left
                       :op  :step
                       :tag :at-inside-most-left
                       :zip zip}
                      "Cannot step inside left element."
                      (option :zip [] zip)
                      (default :zip))
         
         :else
         (let [elem     (left-element zip)
               children (list-child-elements zip :left)
               {:keys [left right]} zip
               parent (assoc zip
                             :left (rest left)
                             :right (cons (first left) right))]
           (-> zip
               (assoc :left (reverse children) :right () :parent parent)
               (fn/call (:update-step-inside-left context) elem)))))
  ([zip n]
   (nth (iterate step-inside-left zip) n)))

(defn step-outside
  "step out to the current container
 
   (-> (from-status '[1 2 [| 3 4]])
       (step-outside)
       (status))
   => '([1 2 | [3 4]])"
  {:added "3.0"}
  ([zip]
   (let [{:keys [context left right parent]} zip]
     (cond ((:at-outside-most? context) zip)
           (event/raise {:fn  :step-outside
                         :op  :step
                         :tag :at-outside-most
                         :zip zip}
                        "At outside-most status."
                        (option :zip [] zip)
                        (default :zip))
           
           :else
           (let [elements (concat (reverse left) right)
                 body  {:left   (:left parent)
                        :right  (:right parent)
                        :parent (:parent parent)}]
             (cond-> (merge zip body)
               (:changed? zip)  (update-child-elements elements)
               :then (fn/call (:update-step-outside context) left))))))
  ([zip n]
   (nth (iterate step-outside zip) n)))

(defn step-outside-right
  "the right of the current container
 
   (-> (from-status '[1 2 [| 3 4]])
       (step-outside-right)
       (status))
   => '([1 2 [3 4] |])"
  {:added "3.0"}
  ([zip]
   (-> zip
       (step-outside)
       (step-right)))
  ([zip n]
   (nth (iterate step-outside-right zip) n)))

(defn step-left-most
  "step to left-most point of current container
 
   (-> (from-status '[1 2 [3 4 |]])
       (step-left-most)
       (status))
   => '([1 2 [| 3 4]])"
  {:added "3.0"}
  [{:keys [context] :as zip}]
  (if ((:at-left-most? context) zip)
    zip
    (recur (step-left zip))))

(defn step-right-most
  "step to right-most point of current container
 
   (-> (from-status '[1 2 [| 3 4]])
       (step-right-most)
       (status))
   => '([1 2 [3 4 |]])"
  {:added "3.0"}
  [{:keys [context] :as zip}]
  (if ((:at-right-most? context) zip)
    zip
    (recur (step-right zip))))

(defn step-inside-most
  "step to at-inside-most point of current container
 
   (-> (from-status '[1 2 | [[3] 4]])
       (step-inside-most)
       (status))
   => '([1 2 [[| 3] 4]])"
  {:added "3.0"}
  [{:keys [context] :as zip}]
  (if ((:at-inside-most? context) zip)
    zip
    (recur (step-inside zip))))

(defn step-inside-most-left
  "steps all the way inside to the left side
   
   (-> (from-status '[[1 [2]] | 3 4])
       (step-inside-most-left)
       (status))
   => '([[1 [2 |]] 3 4])"
  {:added "3.0"}
  [{:keys [context] :as zip}]
  (if ((:at-inside-most-left? context) zip)
    zip
    (recur (step-inside-left zip))))

(defn step-outside-most
  "step to outside-most point of the tree
 
   (-> (from-status '[1 2 [| 3 4]])
       (step-outside-most)
       (status))
   => '(| [1 2 [3 4]])"
  {:added "3.0"}
  [{:keys [context] :as zip}]
  (if ((:at-outside-most? context) zip)
    zip
    (recur (step-outside zip))))

(defn step-outside-most-right
  "step to outside-most point of the tree to the right
   
   (-> (from-status '[1 2 [| 3 4]])
       (step-outside-most-right)
       (status))
   => '([1 2 [3 4]] |)"
  {:added "3.0"}
  [{:keys [context] :as zip}]
  (if ((:at-outside-most? context) zip)
    zip
    (step-right (step-outside-most zip))))

(defn step-end
  "steps status to container directly at end
   
   (->> (from-status '[1 | [[]]])
        (step-end)
        (status))
   => '([1 [[|]]])"
  {:added "3.0"}
  [{:keys [context] :as zip}]
  (-> zip
      (step-outside-most)
      (step-right-most)
      (step-inside-most-left)))

(defn insert-left
  "insert element/s left of the current status
 
   (-> (from-status '[1 2  [[| 3] 4]])
       (insert-left 1 2 3)
       (status))
   => '([1 2 [[1 2 3 | 3] 4]])"
  {:added "3.0"}
  ([{:keys [context] :as zip} data]
   (let [create-fn (:create-element context)
         elem (create-fn data)]
     (-> zip
         (update-in [:left] #(cons elem %))
         (assoc :changed? true)
         (fn/call (:update-insert-left context) elem))))
  ([{:keys [context] :as zip} data & more]
   (apply insert-left (insert-left zip data) more)))

(defn insert-right
  "insert element/s right of the current status
 
   (-> (from-status '[| 1 2 3])
       (insert-right 1 2 3)
       (status))
   => '([| 3 2 1 1 2 3])"
  {:added "3.0"}
  ([{:keys [context] :as zip} data]
   (let [create-fn (:create-element context)
        elem (create-fn data)]
     (-> zip
         (update-in [:right] #(cons elem %))
         (assoc :changed? true)
         (fn/call (:update-insert-right context) elem))))
  ([{:keys [context] :as zip} data & more]
   (apply insert-right (insert-right zip data) more)))

(defn delete-left
  "delete element/s left of the current status
 
   (-> (from-status '[1 2 | 3])
       (delete-left)
       (status))
   => '([1 | 3])"
  {:added "3.0"}
  ([{:keys [context left] :as zip}]
   (cond ((:at-left-most? context) zip)
         (event/raise {:fn  :delete-left
                       :op  :delete
                       :tag :at-left-most
                       :zip zip}
                      "At left-most status.")
         
         :else
         (let [elem (first left)]
           (-> zip
               (update-in [:left] rest)
               (assoc :changed? true)
               (fn/call (:update-delete-left context) elem)))))
  ([{:keys [context] :as zip} n]
   (nth (iterate delete-left zip) n)))

(defn delete-right
  "delete element/s right of the current status
 
   (-> (from-status '[1 2 | 3])
       (delete-right)
       (status))
   => '([1 2 |])"
  {:added "3.0"}
  ([{:keys [context right] :as zip}]
   (cond ((:at-right-most? context) zip)
         (event/raise {:fn  :delete-right
                       :op  :delete
                       :tag :at-right-most
                       :zip zip}
                      "At right-most status.")
         
         :else
         (let [elem (first right)]
           (-> zip
               (update-in [:right] rest)
               (assoc :changed? true)
               (fn/call (:update-delete-right context) elem)))))
  ([{:keys [context] :as zip} n]
   (nth (iterate delete-right zip) n)))

(defn replace-left
  "replace element left of the current status
 
   (-> (from-status '[1 2 | 3])
       (replace-left \"10\")
       (status))
   => '([1 \"10\" | 3])"
  {:added "3.0"}
  [zip data]
  (-> zip
      (delete-left)
      (insert-left data)))

(defn replace-right
  "replace element right of the current status
 
   (-> (from-status '[1 2 | 3])
       (replace-right \"10\")
       (status))
   => '([1 2 | \"10\"])"
  {:added "3.0"}
  [zip data]
  (-> zip
      (delete-right)
      (insert-right data)))

(defn hierarchy
  "replace element right of the current status
 
   (->> (from-status '[1 [[|]]])
        (hierarchy)
        (map right-element))
   => [[] [[]] [1 [[]]]]"
  {:added "3.0"}
  [{:keys [context] :as zip}]
  (loop [out []
         zip (step-outside zip)]
    (if ((:at-outside-most? context) zip)
      (conj out zip)
      (recur (conj out zip) (step-outside zip)))))

(defn at-end?
  "replace element right of the current status
 
   (->> (from-status '[1 [[|]]])
        (at-end?))
   => true
 
   (->> (from-status '[1 [[[2 |]] 3]])
        (at-end?))
   => false"
  {:added "3.0"}
  [{:keys [context] :as zip}]
  (and ((:at-right-most? context) zip)
       (->> (hierarchy zip)
            (map step-right)
            (every? (:at-right-most? context)))))

(defn surround
  "nests elements in current block within another container
 
   (-> (vector-zip 3)
       (insert-left 1 2)
       (surround)
       (status))
   => '(| [1 2 3])
   
   (->> (from-status '[1 [1 2 | 3 4]])
        (surround)
        (status))
   => '([1 [| [1 2 3 4]]])"
  {:added "3.0"}
  [{:keys [context parent left] :as zip}]
  (let [list-fn    (:list-elements context)
        update-fn  (:update-elements context)
        add-fn     (:add-element context)
        empty-elem ((:create-container context))
        new-elem   (->> (update-fn empty-elem (current-elements zip))
                        (add-fn empty-elem))]
    (cond (nil? parent)
          (let [elem (list-fn new-elem)]
            (-> zip
                (assoc :left ()
                       :right elem
                       :changed? true)
                (fn/call (:update-step-outside context) left)))
          
          :else
          (-> (step-outside zip)
              (replace-right new-elem)
              (step-inside)))))

(defn root-element
  "accesses the top level node
 
   (-> (vector-zip [[[3] 2] 1])
       (step-inside-most)
       (root-element))
   => [[[3] 2] 1]"
  {:added "3.0"}
  [zip]
  (-> zip
      (step-outside-most)
      (step-left-most)
      (right-element)))

(defn status
  "returns the form with the status showing
 
   (-> (vector-zip [1 [[2] 3]])
       (step-inside)
       (step-right)
       (step-inside)
       (step-inside)
       (status))
   => '([1 [[| 2] 3]])"
  {:added "3.0"}
  [{:keys [context] :as zip}]
  (->> (insert-left zip (:cursor context))
       (step-outside-most)
       (current-elements)
       (apply list)))

(defn status-string
  "returns the string form of the status
 
   (-> (vector-zip [1 [[2] 3]])
       (step-inside)
       (step-right)
       (status-string))
   => \"[1 | [[2] 3]]\""
  {:added "3.0"}
  [zip]
  (->> (status zip)
       (apply pr-str)))

(defn step-next
  "step status through the tree in depth first order
 
   (->> (from-status '[| 1 [2 [6 7] 3] [4 5]])
        (iterate step-next)
        (take-while identity)
        (map right-element))
   => '(1 [2 [6 7] 3] 2 [6 7] 6 7 3 [4 5] 4 5)"
  {:added "3.0"}
  [{:keys [context] :as zip}]
  (cond (nil? zip) nil

        (and (can-step-inside? zip)
             (not (is-empty-container? zip)))
        (let [zip (step-inside zip)
              check-fn (:is-element? context)]
          (if (check-fn (right-element zip))
            zip
            (recur (step-right zip))))

        (can-step-right? (step-right zip))
        (let [zip (step-right zip)]
          (if ((:is-element? context) (right-element zip))
            zip
            (recur zip)))
        
        :else
        (loop [zip (step-outside-right zip)]
          (cond ((:at-outside-most? context) zip)
                nil
                
                ((:is-element? context) (right-element zip))
                zip
                
                (can-step-right? (step-right zip))
                (step-next (step-right zip))
                
                :else
                (recur (step-outside-right zip))))))

(defn step-prev
  "step status in reverse through the tree in depth first order
 
   (->> (from-status '[1 [2 [6 7] 3] [4 | 5]])
        (iterate step-prev)
        (take 10)
        (map right-element))
   => '(5 4 [4 5] 3 7 6 [6 7] 2 [2 [6 7] 3] 1)"
  {:added "3.0"}
  [{:keys [context] :as zip}]
  (cond (nil? zip) nil

        ((:at-outside-most? context) zip)
        nil
        
        (can-step-left? zip)
        (cond (can-step-inside-left? zip)
              (recur (step-inside-left zip))

              ((:is-element? context) (left-element zip))
              (step-left zip)

              :else
              (recur (step-left zip)))
        
        :else
        (step-outside zip)))

(defn find
  "helper function for the rest of the `find` series"
  {:added "3.0"}
  [zip move pred]
  (->> (iterate move zip)
       (drop 1)
       (take-while right-element)
       (filter #(try (pred (right-element %))
                     (catch Throwable t)))
       (first)))

(defn find-left
  "steps status left to search predicate
   
   (-> (from-status '[0 1 [2 3] [4 5] 6 |])
       (find-left odd?)
       (status))
   => '([0 | 1 [2 3] [4 5] 6])
 
   (-> (from-status '[0 1 [2 3] [4 5] 6 |])
       (find-left keyword?))
   => nil"
  {:added "3.0"}
  [zip pred]
  (event/manage
   (find zip step-left pred)
   (on {:tag :at-left-most}
       _
       (event/continue nil))))

(defn find-right
  "steps status right to search for predicate
 
   (-> (from-status '[0 | 1 [2 3] [4 5] 6])
       (find-right even?)
       (status))
   => '([0 1 [2 3] [4 5] | 6])"
  {:added "3.0"}
  [zip pred]
  (event/manage
   (find zip step-right pred)
   (on {:tag :at-right-most}
       _
       (event/continue nil))))

(defn find-next
  "step status through the tree in depth first order to the first matching element
 
   (-> (vector-zip [1 [2 [6 7] 3] [4 5]])
       (find-next #(= 7 %))
       (status))
   => '([1 [2 [6 | 7] 3] [4 5]])
 
   (-> (vector-zip [1 [2 [6 7] 3] [4 5]])
       (find-next keyword))
   => nil"
  {:added "3.0"}
  [zip pred]
  (find zip step-next pred))

(defn find-prev
  "step status through the tree in reverse order to the last matching element
 
   (-> (from-status '[1 [2 [6 | 7] 3] [4 5]])
       (find-prev even?)
       (status))
   => '([1 [2 [| 6 7] 3] [4 5]])"
  {:added "3.0"}
  [zip pred]
  (find zip step-prev pred))

(defn from-status
  "returns a zipper given a data structure with | as the status
 
   (from-status '[1 2 3 | 4])
   => (contains {:left '(3 2 1),
                 :right '(4)})"
  {:added "3.0"}
  ([data]
   (from-status data vector-zip))
  ([data zipper-fn]
   (let [{:keys [context] :as zip} (zipper-fn data)]
     (if-let [nzip (-> zip
                       (find-next #(zero? (compare (:cursor context) %))))]
       (delete-right nzip)
       zip))))

(defn prewalk
  "emulates clojure.walk/prewalk behavior with zipper
   
   (-> (vector-zip [[1 2] [3 4]])
       (prewalk (fn [v] (if (vector? v)
                          (conj v 100)
                          (+ v 100))))
       (root-element))
   => [[101 102 200] [103 104 200] 200]"
  {:added "3.0"}
  [{:keys [context] :as zip} f]
  (let [elem (right-element zip)
        zip  (replace-right zip (f elem))]
    (cond (can-step-inside? zip)
          (loop [zip  (step-inside zip)]
            (let [zip (-> (prewalk zip f)
                          (step-right))]
              (cond (can-step-right? zip)
                    (recur zip)

                    :else
                    (step-outside zip))))
          :else zip)))

(defn postwalk
  "emulates clojure.walk/postwalk behavior with zipper
 
   (-> (vector-zip [[1 2] [3 4]])
       (postwalk (fn [v] (if (vector? v)
                           (conj v 100)
                           (+ v 100))))
       (root-element))
   => [[101 102 100] [103 104 100] 100]"
  {:added "3.0"}
  [zip f]
  (let [zip (cond (can-step-inside? zip)
                  (loop [zip (step-inside zip)]
                    (let [zip  (postwalk zip f)]
                      (cond (can-step-right? zip)
                            (recur (step-right zip))
                            
                            :else
                            (step-outside zip))))
                  
                  :else zip)]
    (if (can-step-right? zip)
      (let [elem (right-element zip)]
        (replace-right zip (f elem)))
      zip)))

(defn matchwalk
  "performs a match at each level
   
   (-> (matchwalk (vector-zip [1 [2 [3 [4]]]])
                  [(fn [zip]
                     (= 2 (first (right-element zip))))
                   (fn [zip]
                     (= 4 (first (right-element zip))))]
                  delete-left)
       (root-element))
   => [1 [2 [[4]]]]"
  {:added "3.0"}
  ([zip matchers f]
   (matchwalk zip matchers f matchwalk {}))
  ([zip [pred & more :as matchers] f matchwalk {:keys [move-right
                                                       can-move-right?]
                                                :as opts}]
   (let [zip (if (try (pred zip)
                      (catch Throwable t))
               (cond (empty? more)
                     (f zip)
                     
                     (can-step-inside? zip)
                     (step-outside (matchwalk (step-inside zip) more f matchwalk opts))
                     
                     :else
                     zip)
               zip)
         zip (if (can-step-inside? zip)
               (step-outside (matchwalk (step-inside zip) matchers f matchwalk opts))
               zip)
         zip  (if ((or can-move-right?
                       can-step-right?) zip)
                (matchwalk ((or move-right
                                step-right) zip) matchers f matchwalk opts)
                zip)]
     zip)))

(defn levelwalk
  "performs a match at the same level
 
   (-> (vector-zip [1 2 3 4])
       (step-inside)
       (levelwalk [(fn [zip]
                     (odd? (right-element zip)))]
                  delete-right)
       (root-element))
   => [2 4]"
  {:added "3.0"}
  ([zip [pred] f]
   (levelwalk zip [pred] f levelwalk {}))
  ([zip [pred] f levelwalk {:keys [move-right
                                   can-move-right?]
                            :as opts}]
   (let [zip  (if (try (pred zip)
                       (catch Throwable t))
                (f zip)
                zip)
         zip  (if ((or can-move-right?
                       can-step-right?) zip)
                (levelwalk ((or move-right
                                step-right) zip) [pred] f levelwalk opts)
                zip)]
      zip)))
