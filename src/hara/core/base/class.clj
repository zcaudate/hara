(ns hara.core.base.class)

(defn array?
  "checks if a class is an array class

   (array? (type (int-array 0)))
   => true"
  {:added "3.0"}
  [cls]
  (.isArray cls))

(defn primitive-array?
  "checks if class is a primitive array

   (primitive-array? (type (int-array 0)))
   => true

   (primitive-array? (type (into-array [1 2 3])))
   => false"
  {:added "3.0"}
  [cls]
  (and (.isArray cls)
       (.isPrimitive (.getComponentType cls))))

(defn array-component
  "returns the array element within the array

   (array-component (type (int-array 0)))
   => Integer/TYPE

   (array-component (type (into-array [1 2 3])))
   => java.lang.Long"
  {:added "3.0"}
  [cls]
  (if (array? cls)
    (.getComponentType cls)
    (throw (ex-info "Not an array" {:class cls}))))

(defn interface?
  "returns `true` if `class` is an interface

   (interface? java.util.Map) => true

   (interface? Class) => false"
  {:added "3.0"}
  [^java.lang.Class class]
  (.isInterface class))

(defn abstract?
  "returns `true` if `class` is an abstract class

   (abstract? java.util.Map) => true

   (abstract? Class) => false"
  {:added "3.0"}
  [^java.lang.Class class]
  (java.lang.reflect.Modifier/isAbstract (.getModifiers class)))
