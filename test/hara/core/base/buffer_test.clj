(ns hara.core.base.buffer-test
  (:use hara.test)
  (:require [hara.core.base.buffer :refer :all]))

^{:refer hara.core.base.buffer/byte-order :added "3.0"}
(fact "converts keyword to ByteOrder"
  
  (byte-order :little)
  => java.nio.ByteOrder/LITTLE_ENDIAN)

^{:refer hara.core.base.buffer/create-buffer-records :added "3.0"}
(fact "creates records for buffer types"

  (macroexpand-1 '(create-buffer-records ([Byte identity])))^:hidden
  => '{:byte {:buffer java.nio.ByteBuffer
              :convert identity
              :allocate (clojure.core/fn [n] (java.nio.ByteBuffer/allocate n))
              :wrap (clojure.core/fn [arr] (java.nio.ByteBuffer/wrap arr))}})

^{:refer hara.core.base.buffer/create-buffer-functions :added "3.0"}
(fact "creates functions for buffer types"
  
  (macroexpand-1 '(create-buffer-functions (:byte)))^:hidden
  => '[(clojure.core/defn byte-buffer
         ([len-or-elems] (byte-buffer len-or-elems {}))
         ([len-or-elems {:keys [type direct endian convert] :as opts}]
          (hara.core.base.buffer/buffer len-or-elems (clojure.core/assoc opts :type :byte))))])

^{:refer hara.core.base.buffer/buffer-convert :added "3.0"}
(fact "converts an nio ByteBuffer to another type"

  (buffer-convert (java.nio.ByteBuffer/allocate 8)
                  :double)
  => java.nio.DoubleBuffer)

^{:refer hara.core.base.buffer/buffer-type :added "3.0"}
(fact "returns the corresponding type associated with the class"

  (buffer-type java.nio.FloatBuffer :type)
  => :float

  (buffer-type java.nio.FloatBuffer :array-fn)
  => float-array)

^{:refer hara.core.base.buffer/buffer-primitive :added "3.0"}
(fact "returns the corresponding type associated with the instance"

  (buffer-primitive (double-buffer 0) :class)
  => Double/TYPE)

^{:refer hara.core.base.buffer/buffer-write :added "3.0"}
(fact "writes primitive array to a buffer"
  
  (def -buf- (buffer 16))
  (buffer-write -buf- (int-array [1 2 3 4]))
  
  [(.get -buf- 3) (.get -buf- 7) (.get -buf- 11) (.get -buf- 15)]
  => [1 2 3 4])

^{:refer hara.core.base.buffer/buffer-create :added "3.0"}
(fact "creates a byte buffer"

  (buffer-create :double (double-array [1 2 3 4]) 4 true :little true)
  => java.nio.DoubleBuffer

  (buffer-create :double (double-array [1 2 3 4]) 4 false :big false)
  => java.nio.ByteBuffer)

^{:refer hara.core.base.buffer/buffer :added "3.0"}
(fact "either creates or wraps a byte buffer of a given type"

  (buffer 10)
  => java.nio.ByteBuffer

  (buffer 10 {:type :float
              :convert false})
  => java.nio.ByteBuffer
  
  (buffer 10 {:type :float
              :direct true})
  => java.nio.FloatBuffer^:hidden

  (def farr (float-array [1 2 3 4]))
  (= (.array (buffer farr {:type :float :wrap true}))
     farr)
  => true
  
  (not= (.array (buffer farr {:type :float}))
        farr)
  => true)

^{:refer hara.core.base.buffer/buffer-read :added "3.0"}
(fact "reads primitive array from buffer"

  (def -buf- (buffer 4))
  (def -out- (int-array 1))
  
  (do (.put -buf- 3 (byte 1))
      (buffer-read -buf- -out-)
      (first -out-))
  => 1)
