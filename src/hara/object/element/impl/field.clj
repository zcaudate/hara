(ns hara.object.element.impl.field
  (:require [hara.object.element.common :as common]
            [hara.object.element.class :as class]
            [hara.object.element.impl.type :as type]
            [hara.object.element.util :as util]))

(def patch-field
  (let [mf (.getDeclaredField java.lang.reflect.Field  "modifiers")]
    (.setAccessible mf true)
    (fn [^java.lang.reflect.Field field]
      (.setInt mf
               field
               (bit-and (.getModifiers field)
                        (bit-not java.lang.reflect.Modifier/FINAL)))
      field)))

(defn arg-params
  "arguments for getters and setters of fields
 
   (-> (query/query-class String [\"hash\" :#])
       (field/arg-params :set))
   => [java.lang.String Integer/TYPE]
 
   (-> (query/query-class String [\"hash\" :#])
       (field/arg-params :get))
   => [java.lang.String]"
  {:added "3.0"}
  [ele access]
  (let [args [(:container ele)]]
    (condp = access
      :set (conj args (:type ele))
      :get args)))

(defmacro throw-arg-exception
  "helper macro for invoke to throw more readable messages"
  {:added "3.0"}
  [ele args]
  `(throw
    (Exception.
     (format  "Accessor `%s` expects %s for getter or %s for setter, but was invoked with %s."
              (str (:name ~ele))
              (arg-params ~ele :get)
              (arg-params ~ele :set)
              (mapv #(symbol (class/class-convert
                              (common/context-class %) :string))
                    ~args)))))

(defn invoke-static-field
  "invokes the function on the class static field
 
   (-> (query/query-class String [\"CASE_INSENSITIVE_ORDER\" :#])
       (field/invoke-static-field String))
   => java.lang.String$CaseInsensitiveComparator"
  {:added "3.0"}
  ([ele cls]
   (.get ^java.lang.reflect.Field
    (:delegate ele) nil))
  ([ele cls val]
   (util/set-field (:delegate ele) nil val)
   true))

(defn invoke-instance-field
  "invokes the function on the field of an instance
 
   (-> (query/query-class String [\"hash\" :#])
       (field/invoke-instance-field \"123\"))
   => 48690"
  {:added "3.0"}
  ([ele obj]
   (.get ^java.lang.reflect.Field
    (:delegate ele) (util/box-arg (:container ele) obj)))
  ([ele obj val]
   (util/set-field (:delegate ele) (util/box-arg (:container ele) obj) val)
   true))

(defmethod common/-invoke-element :field
  ([ele]
   (throw-arg-exception ele []))
  ([ele x]
   (if (:static ele)
     (invoke-static-field ele x)
     (invoke-instance-field ele x)))

  ([ele x y]
   (if (:static ele)
     (invoke-static-field ele x y)
     (invoke-instance-field ele x y)))
  ([ele x y & more]
   (throw-arg-exception ele (vec (concat [x y] more)))))

(defmethod common/-to-element java.lang.reflect.Field
  [^java.lang.reflect.Field obj]
  (let [body (type/seed :field obj)
        type (.getType obj)]
    (-> body
        (assoc :type type)
        (assoc :origins (list (:container body)))
        (assoc :params (if (:static body) [Class] [(:container body)]))
        (assoc :delegate (patch-field obj))
        (common/element))))

(defmethod common/-format-element :field [ele]
  (if (:static ele)
    (format "[%s :: <%s> | %s]"
            (:name ele)
            (.getName ^Class (:container ele))
            (class/class-convert (:type ele) :string))
    (format "[%s :: (%s) | %s]"
            (:name ele)
            (.getName ^Class (:container ele))
            (class/class-convert (:type ele) :string))))

(defmethod common/-element-params :field [ele]
  (list (mapv #(symbol (class/class-convert % :string)) (arg-params ele :get))
        (mapv #(symbol (class/class-convert % :string)) (arg-params ele :set))))
