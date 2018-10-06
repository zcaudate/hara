(ns hara.core.base.buffer
  (:require [hara.core.base.class :as class]
            [hara.core.base.primitive :as primitive]
            [hara.function :refer [definvoke]])
  (:import (java.nio ByteBuffer
                     CharBuffer
                     DoubleBuffer
                     FloatBuffer
                     IntBuffer
                     LongBuffer
                     ShortBuffer
                     ByteOrder)))

(def +endian+
  {:lookup  {:big    ByteOrder/BIG_ENDIAN
             :little ByteOrder/LITTLE_ENDIAN}
   :mapping {ByteOrder/BIG_ENDIAN    :big   
             ByteOrder/LITTLE_ENDIAN :little}})

(def +endian-native+
  (-> +endian+ :mapping (get (ByteOrder/nativeOrder))))

(defn byte-order
  "converts keyword to ByteOrder
   
   (byte-order :little)
   => java.nio.ByteOrder/LITTLE_ENDIAN"
  {:added "3.0"} [obj]
  (cond (instance? ByteOrder obj)
        obj

        :else
        (-> +endian+ :lookup obj)))

(defmacro create-buffer-records
  "creates records for buffer types
 
   (macroexpand-1 '(create-buffer-records ([Byte identity])))"
  {:added "3.0"} 
  [types]
  (->> types
       (map (fn [k]
              (let [[k fn-convert] (if (vector? k)
                                     k
                                     [k `(fn [~'buff]
                                           (~(symbol (str ".as" k "Buffer")) ~'buff))])
                    bkey  (keyword (.toLowerCase (str k)))
                    bname (symbol (str "java.nio." k "Buffer"))
                    ballocate (symbol (str bname "/allocate"))
                    bwrap (symbol (str bname "/wrap"))]
                [bkey {:buffer  bname
                       :convert fn-convert
                       :allocate `(fn [~'n] (~ballocate ~'n))
                       :wrap `(fn [~'arr] (~bwrap ~'arr))}])))
       (into {})))

(defmacro create-buffer-functions
  "creates functions for buffer types
   
   (macroexpand-1 '(create-buffer-functions (:byte)))"
  {:added "3.0"} 
  [types]
  (mapv (fn [k]
          (let [t (name k)
                fname (symbol (str t "-buffer"))]
            `(defn ~fname
               (~'[len-or-elems] (~fname ~'len-or-elems {}))
               (~'[len-or-elems {:keys [type direct endian convert] :as opts}]
                (buffer ~'len-or-elems
                        (assoc ~'opts :type ~k))))))
        types))

(declare buffer)

(def +buffer-records+
  (create-buffer-records ([Byte identity] Char Double Float Int Long Short)))

(def +buffer-functions+
  (create-buffer-functions (:byte :char :double :float :int :long :short)))

(def +buffer-lookup+
  (primitive/create-lookup +buffer-records+ #{:convert :allocate}))

(defn buffer-convert
  "converts an nio ByteBuffer to another type
 
   (buffer-convert (java.nio.ByteBuffer/allocate 8)
                   :double)
   => java.nio.DoubleBuffer"
  {:added "3.0"}
  ([buff type]
   (if-let [convert-fn (get-in +buffer-records+ [type :convert])]
     (convert-fn buff)
     (throw (ex-info "Cannot convert buffer" {:buffer buff
                                              :type type})))))

(definvoke buffer-type
  "returns the corresponding type associated with the class
 
   (buffer-type java.nio.FloatBuffer :type)
   => :float
 
   (buffer-type java.nio.FloatBuffer :array-fn)
   => float-array"
  {:added "3.0"}
  [:memoize]
  ([cls]
   (buffer-type cls :type))
  ([cls tag]
   (-> cls
       (.getMethod "get" (into-array Class []))
       (.getReturnType)
       (primitive/primitive-type tag))))

(defn buffer-primitive
  "returns the corresponding type associated with the instance
 
   (buffer-primitive (double-buffer 0) :class)
   => Double/TYPE"
  {:added "3.0"}
  ([buff]
   (buffer-primitive buff :type))
  ([buff tag]
   (buffer-type (type buff) tag)))

(defn buffer-write
  "writes primitive array to a buffer
   
   (def -buf- (buffer 16))
   (buffer-write -buf- (int-array [1 2 3 4]))
   
   [(.get -buf- 3) (.get -buf- 7) (.get -buf- 11) (.get -buf- 15)]
   => [1 2 3 4]"
  {:added "3.0"}
  ([buff arr]
   (let [comp  (class/array-component (type arr))
         ctype (primitive/primitive-type comp :type)]
     (cond (instance? (get-in +buffer-records+ [ctype :buffer])
                      buff)
           (doto buff
             (.put arr)
             (.rewind))
           
           (instance? ByteBuffer buff)
           (recur (buffer-convert buff ctype) arr)
           
           :else
           (throw (ex-info "Incompatible buffer and array" {:buffer {:type (type buff)}
                                                            :array  {:type ctype}}))))))

(defn buffer-create
  "creates a byte buffer
 
   (buffer-create :double (double-array [1 2 3 4]) 4 true :little true)
   => java.nio.DoubleBuffer
 
   (buffer-create :double (double-array [1 2 3 4]) 4 false :big false)
   => java.nio.ByteBuffer"
  {:added "3.0"}
  ([type elems length direct endian convert]
   (let [buff (cond (= type :byte)
                    (if direct
                      (ByteBuffer/allocateDirect length)
                      (ByteBuffer/allocate length))

                    :else
                    (let [size (* length (primitive/primitive-type type :bytes))
                          order (byte-order endian)
                          buff (cond direct
                                     (-> (doto (ByteBuffer/allocateDirect size)
                                           (.order order))
                                         (buffer-convert (if convert type :byte)))
                                     
                                     convert
                                     ((get-in +buffer-records+ [type :allocate]) length)

                                     :else
                                     (doto (ByteBuffer/allocate size)
                                       (.order order)))]
                      buff))
         _     (cond (nil? elems) nil
                     
                     (class/primitive-array? (class elems))
                     (buffer-write buff elems)
                     
                     :else
                     (let [array-fn (primitive/primitive-type type :array-fn)]
                       (buffer-write buff (array-fn elems))))]
     buff)))

(defn buffer
  "either creates or wraps a byte buffer of a given type
 
   (buffer 10)
   => java.nio.ByteBuffer
 
   (buffer 10 {:type :float
               :convert false})
   => java.nio.ByteBuffer
   
   (buffer 10 {:type :float
               :direct true})
   => java.nio.FloatBuffer"
  {:added "3.0"}
  ([len-or-elems]
   (buffer len-or-elems {}))
  ([len-or-elems {:keys [type direct endian convert wrap]
                  :or {type    :byte
                       direct  false
                       endian  +endian-native+
                       convert true
                       wrap    false}}]
   (let [[len elems] (if (number? len-or-elems)
                       [len-or-elems nil]
                       [(count len-or-elems) len-or-elems])]
     (if wrap
       ((get-in +buffer-records+ [type :wrap]) elems)
       (buffer-create type elems len direct endian convert)))))

(defn buffer-read
  "reads primitive array from buffer
 
   (def -buf- (buffer 4))
   (def -out- (int-array 1))
   
   (do (.put -buf- 3 (byte 1))
       (buffer-read -buf- -out-)
       (first -out-))
   => 1"
  {:added "3.0"}
  ([buff]
   (cond (.hasArray buff)
         (.array buff)
         
         :else
         (let [len (.capacity buff)
               arr ((buffer-primitive buff :array-fn) len)]
           (buffer-read buff arr))))
  ([buff arr]
   (let [comp  (class/array-component (type arr))
         ctype (primitive/primitive-type comp :type)]
     (cond (instance? (get-in +buffer-records+ [ctype :buffer])
                      buff)
           (do (doto buff
                 (.rewind)
                 (.get arr))
               arr)
           
           (instance? ByteBuffer buff)
           (recur (buffer-convert buff ctype) arr)
           
           :else
           (throw (ex-info "Incompatible buffer and array" {:buffer {:type (type buff)}
                                                            :array  {:type ctype}}))))))
