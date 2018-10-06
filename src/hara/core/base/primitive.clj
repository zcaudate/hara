(ns hara.core.base.primitive
  (:require [hara.core.base.util :as util]))

(def +primitives+
  {:void    ["V" Void      nil     nil           :no-size]
   :boolean ["Z" Boolean   boolean boolean-array :no-size]
   :byte    ["B" Byte      byte    byte-array]
   :char    ["C" Character char    char-array]
   :short   ["S" Short     short   short-array]
   :int     ["I" Integer   int     int-array]
   :long    ["J" Long      long    long-array]
   :float   ["F" Float     float   float-array]
   :double  ["D" Double    double  double-array]})

(def +primitive-records+
  (->> +primitives+
       (mapv (fn [[k [raw container type-fn array-fn no-size?]]]
               (let [kstr (name k)
                     base {:type k
                           :raw raw
                           :symbol (symbol kstr)
                           :string kstr
                           :container container
                           :class (.get (.getField container "TYPE") nil)}
                     size (if-not no-size?
                            {:size  (.get (.getField container "SIZE") nil)
                             :bytes (.get (.getField container "BYTES") nil)})
                     types (if (not= k :void)
                             {:type-fn type-fn
                              :array-raw (str "[" raw)
                              :array-fn array-fn
                              :array-class (Class/forName (str "[" raw))})]
                 [k (merge base size types)])))
       (into {})))

(defn create-lookup
  "creates a path lookup given a record
 
   (create-lookup {:byte {:name \"byte\" :size 1}
                   :long {:name \"long\" :size 4}})
   => {\"byte\" [:byte :name]
       1 [:byte :size]
       \"long\" [:long :name]
       4 [:long :size]}"
  {:added "3.0"}
  ([m] (create-lookup m util/F))
  ([m ignore]
   (reduce-kv (fn [out type record]
                (reduce-kv (fn [out k v]
                                  (if (ignore k)
                                    out
                                    (assoc out v [type k])))
                                out
                                record))
              {}
              m)))

(def +primitive-lookup+
  (create-lookup +primitive-records+ #{:size :bytes}))

(defn primitive-type
  "Converts primitive values across their different representations. The choices are:
    :raw       - The string in the jdk (i.e. `Z` for Boolean, `C` for Character)
    :symbol    - The symbol that hara.object.query uses for matching (i.e. boolean, char, int)
    :string    - The string that hara.object.query uses for matching
    :class     - The primitive class representation of the primitive
    :container - The containing class representation for the primitive type
 
   (primitive-type Boolean/TYPE :symbol)
   => 'boolean
 
   (primitive-type \"Z\" :symbol)
   => 'boolean
 
   (primitive-type \"int\" :symbol)
   => 'int
 
   (primitive-type Character :string)
   => \"char\"
 
   (primitive-type \"V\" :class)
   => Void/TYPE
 
   (primitive-type 'long :container)
   => Long
 
   (primitive-type 'long :type)
   => :long"
  {:added "3.0"}
  ([v to]
   (if-let [[type rep] (get +primitive-lookup+ v)]
     (get-in +primitive-records+ [type to]))))
