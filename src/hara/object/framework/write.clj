(ns hara.object.framework.write
  (:require [clojure.walk :as walk]
            [hara.core.base.enum :as enum]
            [hara.data :as data]
            [hara.protocol.object :as protocol.object]
            [hara.object.query :as query]
            [hara.object.element.util :as element.util]
            [hara.string :as string]
            [hara.function :as fn :refer [definvoke]]))

(def ^:dynamic *transform* nil)

(defonce +write-template+
  '(fn <method> [obj val]
     (or (. obj (<method> val))
         obj)))

(definvoke meta-write
  "access read-attributes with caching
 
   (write/meta-write DogBuilder)
   => (contains {:class test.DogBuilder
                 :empty fn?,
                 :methods (contains
                           {:name
                            (contains {:type java.lang.String, :fn fn?})})})"
  {:added "3.0"}
  [:memoize]
  ([^Class cls]
   (assoc (protocol.object/-meta-write cls) :class cls)))

(declare from-data)

(defn write-fields
  "write fields of an object from reflection
   
   (-> (write/write-fields Dog)
       keys)
   => [:name :species]"
  {:added "3.0"}
  ([cls]
   (write-fields cls query/query-class))
  ([cls query-fn]
   (->> (query-fn cls [:field])
        (reduce (fn [out ele]
                  (let [k (-> ele :name string/spear-type keyword)
                        cls (.getType (get-in ele [:all :delegate]))]
                    (assoc out k {:type cls :fn ele})))
                {}))))

(defn write-all-fields
  "all write fields of an object from reflection
   (hara.object/.* {})
   (-> (write/write-all-fields {})
       keys)
   => [:-hash :-hasheq :-meta :array]"
  {:added "3.0"}
  [cls]
  (write-fields cls query/query-instance-hierarchy))

(defn create-write-method
  "create a write method from the template
 
   (-> ((-> (write/create-write-method (reflect/query-class Cat [\"setName\" :#])
                                       \"set\"
                                       write/+write-template+)
            second
            :fn) (test.Cat. \"spike\") \"fluffy\")
       (.getName))
   => \"fluffy\""
  {:added "3.0"}
  [ele prefix template]
  [(-> (:name ele) (subs (count prefix)) string/spear-type keyword)
   {:type (-> ele :params second)
    :fn (eval (walk/postwalk-replace {'<method> (symbol (:name ele))}
                                     template))}])

(defn write-setters
  "write fields of an object through setter methods
   
   (write/write-setters Dog)
   => {}
 
   (keys (write/write-setters DogBuilder))
   => [:name]"
  {:added "3.0"}
  ([cls] (write-setters cls {}))
  ([cls opts] (write-setters cls opts query/query-class))
  ([cls {:keys [prefix template]
         :or {prefix "set"
              template +write-template+}} query-fn]
   (->> [:method :instance :public (re-pattern (str "^" prefix ".+")) 2]
        (query-fn cls)
        (reduce (fn [out ele]
                  (conj out (create-write-method ele prefix template)))
                {}))))

(defn write-all-setters
  "write all setters of an object and base classes
   
   (write/write-all-setters Dog)
   => {}
 
   (keys (write/write-all-setters DogBuilder))
   => [:name]"
  {:added "3.0"}
  ([cls] (write-all-setters cls {}))
  ([cls opts]
   (write-setters cls opts query/query-hierarchy)))

(defn from-empty
  "creates the object from an empty object constructor
   
   (write/from-empty {:name \"chris\" :pet \"dog\"}
                     (fn [] (java.util.Hashtable.))
                     {:name {:type String
                             :fn (fn [obj v]
                                   (.put obj \"hello\" (keyword v))
                                   obj)}
                      :pet  {:type String
                             :fn (fn [obj v]
                                   (.put obj \"pet\" (keyword v))
                                   obj)}})
  => {\"pet\" :dog, \"hello\" :chris}"
  {:added "3.0"}
  [m empty methods]
  (let [obj (empty)]
    (reduce-kv (fn [obj k v]
                 (if-let [{:keys [type] func :fn} (get methods k)]
                   (func obj (from-data v type))
                   obj))
               obj
               m)))

(defn from-constructor
  "creates the object from a constructor
   
   (-> {:name \"spike\"}
       (write/from-constructor {:fn (fn [name] (Cat. name))
                                :params [:name]}
                               {}))
   ;;=> #test.Cat{:name \"spike\", :species \"cat\"}
 "
  {:added "3.0"}
  [m {:keys [params] :as construct} methods]
  (let [obj (apply (:fn construct) (map m params))]
    (reduce-kv (fn [obj k v]
                 (if-let [{:keys [type] func :fn} (get methods k)]
                   (func obj (from-data v type))
                   obj))
               obj
               (apply dissoc m params))))

(defn from-map
  "creates the object from a map
   
   (-> {:name \"chris\" :age 30 :pets [{:name \"slurp\" :species \"dog\"}
                                     {:name \"happy\" :species \"cat\"}]}
       (write/from-map Person)
       (read/to-data))
   => (contains-in
       {:name \"chris\",
        :age 30,
        :pets [{:name \"slurp\"}
               {:name \"happy\"}]})"
  {:added "3.0"}
  [m ^Class cls]
  (let [m (if-let [rels (get *transform* type)]
            (data/transform-in m rels)
            m)
        {:keys [construct empty methods from-map] :as mobj} (meta-write cls)]
    (cond from-map
          (from-map m)

          empty
          (from-empty m empty methods)

          construct
          (from-constructor m construct methods)
          
          :else
          (throw (ex-info "Cannot convert from map" {:data m :type cls})))))

(defn from-data
  "creates the object from data
   
   (-> (write/from-data [\"hello\"] (Class/forName \"[Ljava.lang.String;\"))
       seq)
   => [\"hello\"]"
  {:added "3.0"}
  [arg ^Class cls]
  (let [^Class targ (type arg)]
    (cond
      ;; If there is a direct match
      (or (element.util/param-arg-match cls targ)
          (element.util/param-float-match cls targ))
      arg
      
      ;; Special case for String/CharArray
      (and (string? arg) (= cls (Class/forName "[C")))
      (.toCharArray arg)

      ;; Special case for Enums
      (enum/enum? cls)
      (if (string? arg)
        (enum/to-enum arg cls)
        (throw (ex-info "Only strings supported for Enums" {:input arg
                                                            :type cls})))

      ;; If there is a vector
      (and (vector? arg)
           (.isArray cls))
      (let [cls (.getComponentType cls)]
        (->> arg
             (map #(from-data % cls))
             (into-array cls)))

      :else
      (let [{:keys [from-clojure from-string from-vector] :as mobj} (meta-write cls)]
        (cond

          from-clojure (from-clojure arg)

          ;; If input is a string and there is a from-string method
          (and (string? arg) from-string)
          (from-string arg)

          ;; If input is a string and there is a from-string method
          (and (vector? arg) from-vector)
          (from-vector arg)

          ;; If the input is a map
          (map? arg)
          (from-map arg cls)

          :else
          (throw (Exception. (format "Problem converting %s to %s" arg cls))))))))
