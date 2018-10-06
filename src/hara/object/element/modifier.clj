(ns hara.object.element.modifier)

(def flags
  {:plain          0
   :public         1      ;; java.lang.reflect.Modifier/PUBLIC
   :private        2      ;; java.lang.reflect.Modifier/PRIVATE
   :protected      4      ;; java.lang.reflect.Modifier/PROTECTED
   :static         8      ;; java.lang.reflect.Modifier/STATIC
   :final          16     ;; java.lang.reflect.Modifier/FINAL
   :synchronized   32     ;; java.lang.reflect.Modifier/SYNCHRONIZE

   :native         256    ;; java.lang.reflect.Modifier/NATIVE
   :interface      512    ;; java.lang.reflect.Modifier/INTERFACE
   :abstract       1024   ;; java.lang.reflect.Modifier/ABSTRACT
   :strict         2048   ;; java.lang.reflect.Modifier/STRICT

   :synthetic      4096   ;; java.lang.Class/SYNTHETIC
   :annotation     8192   ;; java.lang.Class/ANNOTATION
   :enum           16384  ;; java.lang.Class/ENUM
})

(def field-flags
  {:volatile       64    ;; java.lang.reflect.Modifier/VOLATILE
   :transient      128    ;; java.lang.reflect.Modifier/TRANSIENT
})

(def method-flags
  {:bridge         64    ;; java.lang.reflect.Modifier/BRIDGE
   :varargs        128    ;; java.lang.reflect.Modifier/VARARGS
})

(defn- get-modifiers
  [int [[k v] & more] output]
  (if (nil? k) output
      (recur int more
             (if (zero? (bit-and v int))
               output
               (conj output k)))))

(defn int-to-modifiers
  "converts the modifier integer into human readable represenation
 
   (int-to-modifiers 2r001100)
   => #{:protected :static}
 
   (int-to-modifiers 128 :field)
   => #{:transient}
 
   (int-to-modifiers 128 :method)
   => #{:varargs}"
  {:added "3.0"}
  ([int] (int-to-modifiers int nil))
  ([int method]
   (let [output (get-modifiers int (seq flags) #{})]
     (condp = method
       :method (get-modifiers int (seq method-flags) output)
       :field  (get-modifiers int (seq field-flags) output)
       output))))

(defn modifiers-to-int
  "converts the human readable represenation of modifiers into an int
 
   (modifiers-to-int #{:protected :static})
   => 12
 
   (modifiers-to-int #{:transient :field})
   => 128"
  {:added "3.0"}
  [modifiers]
  (reduce (fn [i x]
            (bit-or i (or (get flags x)
                          (get field-flags x)
                          (get method-flags x)
                          0)))
          0 modifiers))
