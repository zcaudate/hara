(ns hara.object.element.impl.method
  (:require [hara.object.element.common :as common]
            [hara.object.element.impl.type :as type]
            [hara.object.element.impl.hierarchy :as hierarchy]
            [hara.object.element.util :as util]))

(defn invoke-static-method
  "invoke function for a static method
 
   (-> (query/query-class clojure.java.api.Clojure [\"read\" :#])
       (method/invoke-static-method [\"{:a 1}\"]))
   => {:a 1}"
  {:added "3.0"}
  ([ele]
   (try (.invoke ^java.lang.reflect.Method (:delegate ele) nil (object-array []))
        (catch IllegalArgumentException e
          (util/throw-arg-exception ele []))))
  ([ele args]
   (.invoke ^java.lang.reflect.Method (:delegate ele) nil (object-array (util/box-args ele args)))))

(defn invoke-instance-method
  "invoke function for an instance method
 
   (-> (query/query-class String [\"charAt\" :#])
       (method/invoke-instance-method [\"0123\" 1]))
   => '1'"
  {:added "3.0"}
  [ele args]
  (let [bargs (util/box-args ele args)]
    (.invoke ^java.lang.reflect.Method (:delegate ele) (first bargs) (object-array (rest bargs)))))

(defmethod common/-invoke-element :method
  ([ele]
   (if (:static ele)
     (invoke-static-method ele)
     (util/throw-arg-exception ele [])))
  ([ele & args]
   (if (:static ele)
     (invoke-static-method ele args)
     (invoke-instance-method ele args))))

(defn to-static-method
  "creates the parameters for a static method
 
   (-> (query/query-class clojure.java.api.Clojure [\"read\" :#])
       :delegate
       (method/to-static-method {}))
   => {:params [String]
       :origins [clojure.java.api.Clojure]}"
  {:added "3.0"}
  [^java.lang.reflect.Method obj body]
  (-> body
      (assoc :params (vec (seq (.getParameterTypes obj))))
      (assoc :origins (list (.getDeclaringClass obj)))))

(defn to-instance-method
  "creates the parameters for an instance method
 
   (-> (query/query-class String [\"charAt\" :#])
       :delegate
       (method/to-instance-method {:container String}))
   => {:container String
       :params [String Integer/TYPE]
       :origins [CharSequence String]}"
  {:added "3.0"}
  [^java.lang.reflect.Method obj body]
  (-> body
      (assoc :params (vec (cons (:container body) (seq (.getParameterTypes obj)))))
      (assoc :origins (hierarchy/origins obj))))

(defn to-pre-element
  "creates the parameters for methods
 
   (-> (query/query-class String [\"charAt\" :#])
       :delegate
       (method/to-pre-element))
   => (contains {:name \"charAt\"
                 :tag :method
                 :container String
                 :modifiers #{:instance :method :public}
                 :static false
                :delegate java.lang.reflect.Method
                 :params [String Integer/TYPE]
                 :origins [CharSequence String]})"
  {:added "3.0"}
  [obj]
  (let [body (type/seed :method obj)
        body (if (:static body)
               (to-static-method obj body)
               (to-instance-method obj body))]
    body))

(defmethod common/-to-element java.lang.reflect.Method
  [^java.lang.reflect.Method obj]
  (let [body (-> (to-pre-element obj)
                 (assoc :type (.getReturnType obj)))]
    (common/element body)))

(defmethod common/-format-element :method [ele]
  (util/format-element-method ele))

(defmethod common/-element-params :method [ele]
  (list (util/element-params-method ele)))
