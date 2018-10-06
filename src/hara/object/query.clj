(ns hara.object.query
  (:require [hara.core.base.inheritance :as inheritance]
            [hara.function :refer [definvoke]]
            [hara.protocol.function :as protocol.function]
            [hara.data :as data]
            [hara.object.element :as element]
            [hara.object.element.class :as class]
            [hara.object.element.common :as common]
            [hara.object.query.input :as input]
            [hara.object.query.order :as order]))

(defn all-class-members
  "returns the raw reflected methods, fields and constructors
 
   (all-class-members String)"
  {:added "3.0"}
  [^Class class]
  (concat
   (seq (.getDeclaredMethods class))
   (seq (.getDeclaredConstructors class))
   (seq (.getDeclaredFields class))))

(definvoke all-class-elements
  "returns elements 
 
   (all-class-elements String)"
  {:added "3.0"}
  [:memoize]
  ([class]
   (->> (all-class-members class)
        (mapv element/to-element))))

(definvoke select-class-elements
  "returns the processed reflected methods, fields and constructors
   
   (select-class-elements String [#\"^c\" :name])"
  {:added "3.0"}
  [:memoize]
  ([class selectors]
   (let [grp (input/args-group selectors)]
    (->> (all-class-elements class)
         (order/order grp)))))

(defn query-class
  "queries the java view of the class declaration
 
   (query-class String [#\"^c\" :name])
   ;;=> [\"charAt\" \"checkBounds\" \"codePointAt\" \"codePointBefore\"
   ;;    \"codePointCount\" \"compareTo\" \"compareToIgnoreCase\"
   ;;    \"concat\" \"contains\" \"contentEquals\" \"copyValueOf\"]
 "
  {:added "3.0"}
  [obj selectors]
  (select-class-elements (common/context-class obj) selectors))

(definvoke select-supers-elements
  "returns the elements related to the type's super classes
 
   (select-supers-elements String [])"
  {:added "3.0"}
  [:memoize]
  ([class selectors]
  (let [grp    (input/args-group selectors)
        supers (data/flatten-all (inheritance/ancestor-tree class))
        elems  (mapcat #(select-class-elements % selectors) supers)]
    (if (and (seq elems)
             (common/element? (first elems)))
      (order/order grp elems)
      (sort (set elems))))))

(defn query-supers
  "returns all elements associated with the context class's super
 
   (query-supers \"122\" [])"
  {:added "3.0"}
  [obj selectors]
  (let [tcls  (common/context-class obj)]
    (select-supers-elements tcls selectors)))

(defn query-hierarchy
  "lists what methods could be applied to a particular instance
 
   (query-hierarchy String [:name #\"^to\"])
   => [\"toCharArray\" \"toLowerCase\" \"toString\" \"toUpperCase\"]"
  {:added "3.0"}
  [obj selectors]
  (let [grp (input/args-group selectors)
        tcls (common/context-class obj)
        elems (concat (select-class-elements tcls selectors)
                      (select-supers-elements tcls selectors))]
    (if (and (seq elems)
             (common/element? (first elems)))
      (order/order grp elems)
      (sort (set elems)))))

(definvoke all-instance-elements
  "returns the hierarchy of elements corresponding to a class
 
   (all-instance-elements String nil)"
  {:added "3.0"}
  [:memoize]
  ([tcls icls]
  (let [supers (reverse (inheritance/ancestor-list tcls))
        eles   (mapcat #(select-class-elements % [:instance]) supers)]
    (concat eles
            (if icls (concat eles (select-class-elements icls [:static])))))))

(definvoke select-instance-elements
  "returns the hierarchy of elements corresponding to a class
 
   (select-instance-elements String nil [#\"^c\" :name])"
  {:added "3.0"}
  [:memoize]
  ([tcls icls selectors]
   (let [grp (input/args-group selectors)]
     (->> (all-instance-elements tcls icls)
          (order/order grp)))))

(defn query-instance
  "lists what class methods could be applied to a particular instance
 
   (query-instance \"abc\" [:name #\"^to\"])
   => [\"toCharArray\" \"toLowerCase\" \"toString\" \"toUpperCase\"]
 
   (query-instance String [:name #\"^to\"])
   => (contains [\"toString\"])"
  {:added "3.0"}
  [obj selectors]
  (let [tcls (type obj)]
    (select-instance-elements tcls (if (class? obj) obj) selectors)))

(defn query-instance-hierarchy
  "lists what methods could be applied to a particular instance. includes all super class methods
 
   (query-instance-hierarchy String [:name #\"^to\"])
   => [\"toCharArray\" \"toLowerCase\" \"toString\" \"toUpperCase\"]"
  {:added "3.0"}
  [obj selectors]
  (select-instance-elements (common/context-class obj) nil selectors))

(defn apply-element
  "apply the class element to arguments
   
   (->> (apply-element \"123\" \"value\" [])
        (map char))
   => [\1 \2 \3]"
  {:added "3.0"}
  [obj method args]
  (let [elem (or (query-instance obj [method (inc (count args)) :field :#])
                 (query-instance obj [method (inc (count args)) :method :#]))]
    (if elem (apply elem obj args))))

(deftype Delegate [pointer fields]
  Object
  (toString [self]
    (format "<%s@%s %s>" (.getName ^Class (type pointer)) (.hashCode pointer) (self)))

  clojure.lang.IDeref
  (deref [self] fields)

  java.util.Map
  (equals [self other] (= (self) other))
  (size [self] (count fields))
  (keySet [self] (keys fields))
  (entrySet [self] (set (map (fn [[k f]] (clojure.lang.MapEntry. k (f pointer))) fields)))
  (containsKey [self key] (contains? fields key))
  (values [self] (map (fn [f] (f pointer)) (vals fields)))

  clojure.lang.ILookup
  (valAt [self key]
    (if-let [f (get fields key)]
      (f pointer)))
  (valAt [self key not-found]
    (if-let [f (get fields key)]
      (f pointer)
      not-found))

  clojure.lang.IFn
  (invoke [self]
    (->> fields
         (map (fn [[k f]]
                [k (f pointer)]))
         (into {})))
  (invoke [self key]
    (.valAt self key))
  (invoke [self key value]
    (if-let [f (get fields key)]
      (f pointer value))
    self))

(defmethod print-method Delegate
  [v ^java.io.Writer w]
  (.write w (str v)))
  
(defn delegate
  "Allow transparent field access and manipulation to the underlying object.
 
   (def -a- \"hello\")
   (def -*a-  (delegate -a-))
   (def -world-array- (.getBytes \"world\"))
 
   (mapv char (-*a- :value)) => [\\h \\e \\l \\l \\o]
 
   (-*a- :value -world-array-)
   (String. (-*a- :value)) => \"world\"
   -a- => \"world\""
  {:added "3.0"}
  [obj]
 (let [fields (->> (map (juxt (comp keyword :name) identity) (query-instance obj [:field]))
                   (into {}))]
   (Delegate. obj fields)))

(defonce +query-functions+
  {:class query-class
   :instance query-instance
   :supers query-supers
   :hierarchy query-hierarchy
   :ihierarchy query-instance-hierarchy})

(definvoke invoke-intern-element
  "creates the form for `element` for definvoke
 
   (invoke-intern-element :element '-foo- {:class String
                                           :selector [\"charAt\"]} nil)"
  {:added "3.0"}
  [:method {:multi protocol.function/-invoke-intern
            :val :element}]
  ([_ name {:keys [type class selector] :as config} _]
   `(let [~'elem ((get +query-functions+ (or ~type :class)) ~class (cons :merge ~selector))
          ~'arglists (list (element/element-params ~'elem))] 
      (doto (def ~name ~'elem)
        (alter-meta! merge
                     {:arglists ~'arglists}
                     ~(dissoc config :type :class :selector))))))
