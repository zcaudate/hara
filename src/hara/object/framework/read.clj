(ns hara.object.framework.read
  (:require [clojure.walk :as walk]
            [hara.protocol.object :as protocol.object]
            [hara.object.query :as query]
            [hara.string :as string]
            [hara.function :as fn :refer [definvoke]]))

(definvoke meta-read
  "access read-attributes with caching
 
   (read/meta-read Pet)
   => (contains-in {:class test.Pet
                    :methods {:name fn?
                              :species fn?}})"
  {:added "3.0"}
  [:memoize]
  ([^Class cls]
   (assoc (protocol.object/-meta-read cls) :class cls)))

(defn read-fields
  "fields of an object from reflection
   (-> (read/read-fields Dog)
       keys)
   => [:name :species]"
  {:added "3.0"}
  ([cls]
   (read-fields cls query/query-class))
  ([cls query-fn]
   (->> (query-fn cls [:field])
        (map (juxt (comp keyword string/spear-type :name)
                   identity))
        (into {}))))

(defn read-all-fields
  "all fields of an object from reflection
 
   (-> (read/read-all-fields {})
       keys)
   => [:-hash :-hasheq :-meta :array]"
  {:added "3.0"}
  [cls]
  (read-fields cls query/query-instance-hierarchy))

(defonce +read-template+
  '(fn <method> [obj] (. obj (<method>))))

(defonce +read-is-opts+
  {:prefix "is" :template +read-template+ :extra "?"})

(defonce +read-get-opts+
  {:prefix "get" :template +read-template+})

(defn create-read-method
  "creates a method based on a template
   (read/create-read-method (reflect/query-class Dog [\"getName\" :#])
                            \"get\"
                            read/+read-get-opts+
                            nil)
   => (contains-in [:name {:prefix \"get\", :template fn?}])"
  {:added "3.0"}
  [ele prefix template extra]
  [(-> (:name ele)
       (subs (count prefix))
       string/spear-type
       (str (or extra ""))
       keyword)
   (eval (walk/postwalk-replace {'<method> (symbol (:name ele))}
                                template))])

(defn read-getters
  "returns fields of an object through getter methods
   (-> (read/read-getters Dog)
       keys)
   => [:name :species]"
  {:added "3.0"}
  ([cls] (read-getters cls +read-get-opts+))
  ([cls opts] (read-getters cls +read-get-opts+ query/query-class))
  ([cls {:keys [prefix template extra] :as opts} query-fn]
   (->> [:method :instance :public (re-pattern (str "^" prefix ".+")) 1]
        (query-fn cls)
        (reduce (fn [out ele]
                  (conj out (create-read-method ele prefix template extra)))
                {}))))

(defn read-all-getters
  "returns fields of an object and base classes
   (-> (read/read-all-getters Dog)
       keys)
   => [:class :name :species]"
  {:added "3.0"}
  ([cls] (read-all-getters cls +read-get-opts+))
  ([cls {:keys [prefix template extra] :as opts}]
   (read-getters cls opts query/query-hierarchy)))

(defn to-data
  "creates the object from a string or map
 
   (read/to-data \"hello\")
   => \"hello\"
 
   (read/to-data (write/from-map {:name \"hello\" :species \"dog\"} Pet))
   => (contains {:name \"hello\"})"
  {:added "3.0"}
  [obj]
  (let [cls (type obj)
        {:keys [to-clojure to-string to-map to-vector methods]} (meta-read cls)]
    (cond (nil? obj) nil

          (instance? java.util.Map obj)
          obj
          
          to-clojure (to-clojure obj)

          to-string (to-string obj)

          to-map (to-map obj)

          to-vector (to-vector obj)

          methods (reduce-kv (fn [out k func]
                               (if-some [v (func obj)]
                                 (assoc out k (to-data v))
                                 out))
                             {}
                             methods)

          (.isArray ^Class cls)
          (->> (seq obj)
               (mapv to-data))

          (instance? java.lang.Iterable obj)
          (mapv to-data obj)

          (instance? java.util.Iterator obj)
          (->> obj iterator-seq (mapv to-data))

          (instance? java.util.Enumeration obj)
          (->> obj enumeration-seq (mapv to-data))

          (instance? java.util.AbstractCollection obj)
          (to-data (.iterator ^java.util.AbstractCollection obj))

          (instance? java.lang.Enum obj)
          (str obj)
          
          :else obj)))

(defn to-map
  "creates a map from an object
 
   (read/to-map (Cat. \"spike\"))
   => {:name \"spike\"}"
  {:added "3.0"}
  [obj]
  (let [cls (type obj)
        {:keys [to-map methods]} (meta-read cls)]
    (cond (nil? obj) nil

          (instance? java.util.Map obj)
          obj

          to-map (to-map obj)

          methods (reduce-kv (fn [out k func]
                               (if-some [v (func obj)]
                                 (assoc out k (to-data v))
                                 out))
                             {}
                             methods)

          :else
          (throw (Exception. (str "Cannot process object: " obj))))))
