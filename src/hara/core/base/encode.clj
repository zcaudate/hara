(ns hara.core.base.encode
  (:import (java.util Base64)))

(def +hex-array+ [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9 \a \b \c \d \e \f])

(defn hex-chars
  "turns a byte into two chars
 
   (hex-chars 255)
   => [\\f \\f]
 
   (hex-chars 42)
   => [\2 \\a]"
  {:added "3.0"}
  [b]
  (let [v (bit-and b 0xFF)]
    [(+hex-array+ (bit-shift-right v 4))
     (+hex-array+ (bit-and v 0x0F))]))

(defn to-hex-chars
  "turns a byte array into a hex char array"
  {:added "3.0"}
  [bytes]
  (char-array (mapcat hex-chars bytes)))

(defn to-hex
  "turns a byte array into hex string
 
   (to-hex (.getBytes \"hello\"))
   => \"68656c6c6f\""
  {:added "3.0"}
  [bytes]
  (String. (to-hex-chars bytes)))

(defn from-hex-chars
  "turns two hex characters into a byte value
 
   (byte (from-hex-chars \2 \\a))
   => 42"
  {:added "3.0"}
  [c1 c2]
  (unchecked-byte
   (+ (bit-shift-left (Character/digit c1 16) 4)
      (Character/digit c2 16))))

(defn from-hex
  "turns a hex string into a sequence of bytes
 
   (String. (from-hex \"68656c6c6f\"))
   => \"hello\""
  {:added "3.0"}
  [s]
  (byte-array (map #(apply from-hex-chars %) (partition 2 s))))

(defn to-base64-bytes
  "turns a byte array into a base64 encoding
 
   (-> (.getBytes \"hello\")
       (to-base64-bytes)
       (String.))
   => \"aGVsbG8=\""
  {:added "3.0"}
  [bytes]
  (.encode (Base64/getEncoder)
           bytes))

(defn to-base64
  "turns a byte array into a base64 encoded string
 
   (-> (.getBytes \"hello\")
       (to-base64))
   => \"aGVsbG8=\""
  {:added "3.0"}
  [bytes]
  (.encodeToString (Base64/getEncoder)
                   bytes))

(defn from-base64
  "turns a base64 encoded string into a byte array
 
   (-> (from-base64 \"aGVsbG8=\")
       (String.))
   => \"hello\""
  {:added "3.0"}
  [input]
  (.decode (Base64/getDecoder)
           input))
