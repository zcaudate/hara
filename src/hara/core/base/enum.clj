(ns hara.core.base.enum
  (:require [hara.core.base.inheritance :as inheritance]
            [hara.string :as string]
            [hara.function.base.invoke :refer [definvoke]]))

(defn enum?
  "check to see if class is an enum type
 
   (enum? java.lang.annotation.ElementType) => true
 
   (enum? String) => false"
  {:added "3.0"}
  [type]
  (if (-> (inheritance/ancestor-list type)
          (set)
          (get java.lang.Enum))
    true false))

(defn enum-values
  "returns all values of an enum type
 
   (->> (enum-values ElementType)
        (map str))
   => (contains [\"TYPE\" \"FIELD\" \"METHOD\" \"PARAMETER\" \"CONSTRUCTOR\"]
                :in-any-order :gaps-ok)"
  {:added "3.0"}
  [type]
  (let [method (.getMethod type "values" (make-array Class 0))
        values (.invoke method nil (object-array []))]
    (seq values)))

(defn create-enum
  "creates an enum value from a string
 
   (create-enum \"TYPE\" ElementType)
   => ElementType/TYPE"
  {:added "3.0"}
  [s type]
  (loop [[e :as values] (enum-values type)]
    (cond (empty? values)
          (throw (ex-info "Cannot create enum" {:type type
                                                :value s}))
          (= (str e) s) e

          :else
          (recur (rest values)))))

(definvoke enum-map
  "cached map of enum values
 
   (enum-map ElementType)"
  {:added "3.0"}
  [:memoize]
  ([type]
   (->> (enum-values type)
        (map (juxt (comp keyword string/spear-type string/to-string)
                   identity))
        (into {}))))

(defn to-enum
  "gets an enum value given a symbol
 
   (to-enum \"TYPE\" ElementType)
   => ElementType/TYPE
 
   (to-enum :field ElementType)
   => ElementType/FIELD"
  {:added "3.0"}
  [s type]
  (let [key ((comp keyword string/spear-type string/to-string) s)]
    (or (get (enum-map type) key)
        (throw (ex-info "Cannot find the enum value."
                        {:input s
                         :key key
                         :type type
                         :options (keys (enum-map type))})))))
