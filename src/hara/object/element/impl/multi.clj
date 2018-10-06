(ns hara.object.element.impl.multi
  (:require [hara.object.element.common :as common]
            [hara.object.element.class :as class]
            [hara.object.element.util :as util]
            [hara.string :as string]))

(defn get-name
  "all elements in the array have to have the same name
   (multi/get-name [{:name \"a\"} {:name \"b\"}])
   => (throws)
 
   (multi/get-name [{:name \"a\"} {:name \"a\"}])
   => \"a\""
  {:added "3.0"}
  [v]
  (let [names (map :name v)]
    (assert (and (apply = names)
                 (first names))
            "All elements in vector must have the same name")
    (first names)))

(defn to-element-array
  "converts a three layer map to a flat sequence of values
 
   (multi/to-element-array {:a {:b {:c 1
                                    :d 2}}})
   => [1 2]"
  {:added "3.0"}
  [m0]
  (for [[k1 m1] (seq m0)
        [k2 m2] (seq m1)
        [k3 v]  (seq m2)]
    v))

(defn multi-element
  "combining elements together into one
 
   (->> (query/query-class clojure.lang.PersistentVector [\"create\"])
        (multi/multi-element {}))
   ;;=> #[create :: ([java.util.List]), ... ([java.lang.Iterable])]
 "
  {:added "3.0"}
  [m v]
  (common/element {:tag :multi
                   :name (get-name v)
                   :array v
                   :lookup m
                   :cache (atom {})}))

(defmethod common/-to-element clojure.lang.APersistentMap [m]
  (let [v (to-element-array m)]
    (multi-element m v)))

(defn to-element-map-path
  "creates a map path for the element
 
   (-> (query/query-class String [\"charAt\" :#])
       (multi/to-element-map-path))
   => [:method 2 [java.lang.String Integer/TYPE]]"
  {:added "3.0"}
  [ele]
  (let [tag (:tag ele)
        params (:params ele)]
    (cond (= (:tag ele) :field)
          [tag 0 []]

          :else
          [tag (count params) params])))

(defmethod common/-to-element clojure.lang.APersistentVector [v]
  (let [m (reduce
           (fn [m ele]
             (assoc-in m (to-element-map-path ele) ele))
           {} v)]
    (multi-element m v)))

(defmethod common/-format-element :multi [mele]
  (format "[%s :: %s]"
          (:name mele)
          (->> (:array mele)
               (map common/-element-params)
               (map (fn [params] (if (empty? params) [] (apply list params))))
               (sort (fn [x y] (compare (count x) (count y))))
               (hara.string.base.common/joinr ", "))))

(defmethod common/-element-params :multi [mele]
  (mapcat common/-element-params (:array mele)))

(defn elegible-candidates
  "finds elegible candidates based upon argument list
 
   (-> (query/query-class clojure.lang.PersistentVector [\"create\" :#])
       (get-in [:lookup :method 1])
       (multi/elegible-candidates [java.util.List]))
   ;;=> (#[create :: (java.util.List) -> clojure.lang.PersistentVector]
   ;;    #[create :: (java.lang.Iterable) -> clojure.lang.PersistentVector])
 "
  {:added "3.0"}
  [prelim aparams]
  (->> prelim
       (map (fn [[_ v]] v))
       (filter (fn [ele]
                 (every? (fn [[ptype atype]]
                           (util/param-arg-match ptype atype))
                         (map list (:params ele) aparams))))))

(defn find-method-candidate
  "creates a map path for the element
   (-> (query/query-class clojure.lang.PersistentVector [\"create\" :#])
       (multi/find-method-candidate [java.util.List]))
   ;; #[create :: (java.util.List) -> clojure.lang.PersistentVector]
   => hara.object.element.common.Element"
  {:added "3.0"}
  [mele aparams]
  (let [tag (if (= "new" (:name mele)) :constructor :method)
        prelim (get-in (:lookup mele) [tag (count aparams)])]
    (or (get prelim aparams)
        (get @(:cache mele) aparams)
        (if-let [ele (first (elegible-candidates prelim aparams))]
          (do (swap! (:cache mele) assoc aparams ele)
              ele)))))

(defn find-field-candidate
  "finds best field candidate for the element
 
   (if (< 9 (:major (env/java-version)))
     (-> (query/query-class String [\"value\" :#])
         (multi/find-field-candidate [(type (chars \"a\"))])))
   ;; #[value :: (java.lang.String) | byte[]]
   => (any hara.object.element.common.Element
           nil)"
  {:added "3.0"}
  [mele aparams]
  (if-let [ele (get-in (:lookup mele) [:field 0 []])]
    (and (or (= 0 (count aparams))
             (and (= 1 (count aparams))
                  (util/param-arg-match (:type ele) (first aparams))))
         ele)))

(defn find-candidate
  "finds best element within the multi, methods then fields
 
   (if (< 9 (:major (env/java-version)))
     (-> (query/query-class String [\"value\" :#])
         (multi/find-candidate [String (type (chars \"a\"))])))
   ;; #[value :: (java.lang.String) | byte[]]
   => (any hara.object.element.common.Element
           nil)"
  {:added "3.0"}
  [mele aparams]
  (or (find-method-candidate mele aparams)
      (find-field-candidate mele (rest aparams))
      (throw (Exception. (format "Cannot find a suitable candidate function, need %s, invoked with %s."
                                 (common/-format-element mele)
                                 (mapv #(symbol (class/class-convert
                                                 % :string))
                                       aparams))))))

(defmethod common/-invoke-element :multi [mele & args]
  (let [aparams (mapv type args)
        candidate (find-candidate mele aparams)]
    (apply candidate args)))
