(ns hara.io.base.binary
  (:require [hara.function :refer [definvoke] :as fn]
            [hara.protocol.binary :as protocol.binary])
  (:import (hara.io ByteBufferInputStream
                    ByteBufferOutputStream)
           (java.io InputStream
                    OutputStream
                    FileInputStream
                    FileOutputStream
                    PipedInputStream
                    PipedOutputStream
                    ByteArrayInputStream
                    ByteArrayOutputStream
                    InputStreamReader
                    OutputStreamWriter)
           (java.util BitSet)
           (java.net URL URI)
           (java.io File)
           (java.nio ByteBuffer
                     ByteOrder)
           (java.nio.file Path)
           (java.nio.channels ByteChannel
                              FileChannel
                              Channels))
  (:refer-clojure :exclude [bytes]))

(def +native+ (ByteOrder/nativeOrder))

;; Supported Formats
;;
;; 1.  bitstr
;; 2.  bitseq
;; 3.  bitset
;; 4.  number
;; 5.  byte[]
;; 6.  io.inputstream
;; 7.  nio.bytebuffer
;; 8.  io.file
;; 9.  nio.path
;; 10. nio.channel
;; 11. net.url
;; 12. net.uri

;; bitseq to number is little endian (start bit is largest)
;; byte[] to bitseq is little endian (start bit is largest)
;; buffer and stream to byte[] will be according to the instance settings (usually little endian)

(defn bitstr-to-bitseq
  "creates a binary sequence from a bitstr
   
   (bitstr-to-bitseq \"100011\")
   => [1 0 0 0 1 1]"
  {:added "0.1"}
  [^String s]
  (vec (map-indexed (fn [i ch]
                      (case ch \0 0 \1 1
                            (throw (ex-info "Not a binary character." {:index i
                                                                       :char ch}))))
                    s)))

(defn bitseq-to-bitstr
  "creates a bitstr from a binary sequence
 
   (bitseq-to-bitstr [1 0 0 0 1 1])
   => \"100011\""
  {:added "0.1"}
  [bseq]
  (apply str bseq))

(defn bitseq-to-number
  "creates a number from a binary sequence
 
   (bitseq-to-number [1 0 0 0 1 1])
   => 49
   
   (bitseq-to-number (repeat 10 1))
   => 1023
   "
  {:added "0.1"}
  ([arr]
   (bitseq-to-number arr 1))
  ([arr factor]
   (cond (empty? arr) 0
         
         (> 64 (count arr))
         (*' (->> arr
                  (map-indexed (fn [i x]
                                 (bit-shift-left x i)))
                  (apply +))
             factor)

         :else
         (+' (bitseq-to-number (take 63 arr) factor)
             (bitseq-to-number (drop 63 arr) (*' factor
                                                 (+' Long/MAX_VALUE 1)))))))

(defn bitseq-to-bitset
  "creates a bitset from a binary sequence
 
   (bitseq-to-bitset [1 0 0 0 1 1])
   ;; => #bs[1 0 0 0 1 1]
   "
  {:added "0.1"}
  [bseq]
  (let [bs (BitSet. (count bseq))]
    (doall (map-indexed (fn [i b]
                          (.set bs i (not (zero? b))))
                        bseq))
    bs))

(defn bitset-to-bitseq
  "creates a binary sequence from a bitset
   (-> (bitseq-to-bitset [1 0 0 0 1 1])
       (bitset-to-bitseq))
   => [1 0 0 0 1 1]"
  {:added "0.1"}
  ([^BitSet bs]
   (bitset-to-bitseq bs (.length bs)))
  ([^BitSet bs length]
   (vec (for [i (range length)]
          (if (.get bs i) 1 0)))))

(defn bitset-to-bytes
  "creates a byte array from a bitset
   
   (-> (bitseq-to-bitset [1 0 0 0 1 1])
       (bitset-to-bytes)
       seq)
   => [49]"
  {:added "0.1"}
  [^BitSet bs]
  (.toByteArray bs))

(defn bytes-to-bitset
  "creates a bitset from bytes
 
   (-> (byte-array [49])
       (bytes-to-bitset)
       (bitset-to-bitseq 8))
   => [1 0 0 0 1 1 0 0]"
  {:added "0.1"}
  [arr]
  (BitSet/valueOf arr))

(defn long-to-bitseq
  "creates a binary sequence from a long
   
   (long-to-bitseq 1023)
   => [1 1 1 1 1 1 1 1 1 1]
 
   (long-to-bitseq 49 8)
   => [1 0 0 0 1 1 0 0]"
  {:added "0.1"}
  ([x]
   (let [m (loop [m 0
                  x x]
             (if (zero? x)
               m
               (recur (inc m) (bit-shift-right x 1))))]
     (long-to-bitseq x m)))
  ([x m]
   (let [arr (vec (repeat m 0))]
     (reduce (fn [out i]
               (assoc out i (bit-and 1 (bit-shift-right x i))))
             arr
             (range m)))))

(defn bigint-to-bitset
  "creates a bitset from a bigint
   
   (-> (bigint-to-bitset 9223372036854775808N)
       (bitset-to-bitseq))
   => (conj (vec (repeat 63 0))
            1)"
  {:added "0.1"}
  [x]
  (-> (.toBigInteger x)
      (.toByteArray)
      (reverse)
      (byte-array)
      (bytes-to-bitset)))

(defn- inputstream-to-bytes [istream]
  (let [ostream (ByteArrayOutputStream.)]
    (.transferTo ostream)
    (.toByteArray ostream)))

(defn- bytebuffer-to-bytes [buff]
  (.array buff))

;; 1. bitstr 

(extend-type java.lang.String
  protocol.binary/IBinary
  (-to-bitstr [x] x)
  (-to-bitseq [x] (bitstr-to-bitseq x))
  (-to-bitset [x] (-> x bitstr-to-bitseq bitseq-to-bitset))
  (-to-bytes  [x] (-> x bitstr-to-bitseq bitseq-to-bitset bitset-to-bytes))
  (-to-number [x] (-> x bitstr-to-bitseq bitseq-to-number)))

;; 2. bitseq

(extend-type clojure.lang.PersistentVector
  protocol.binary/IBinary
  (-to-bitstr [x] (bitseq-to-bitstr x))
  (-to-bitseq [x] x)
  (-to-bitset [x] (bitseq-to-bitset x))
  (-to-bytes  [x] (-> x bitseq-to-bitset bitset-to-bytes))
  (-to-number [x] (bitseq-to-number x)))

;; 3. bitset

(extend-type java.util.BitSet
  protocol.binary/IBinary
  (-to-bitstr [x] (-> x bitset-to-bitseq bitseq-to-bitstr))
  (-to-bitseq [x] (bitset-to-bitseq x))
  (-to-bitset [x] x)
  (-to-bytes  [x] (bitset-to-bytes x))
  (-to-number [x] (-> x bitset-to-bitseq bitseq-to-number)))

;; 4. number

(extend-type java.lang.Long
  protocol.binary/IBinary
  (-to-bitstr [x] (-> x long-to-bitseq bitseq-to-bitstr))
  (-to-bitseq [x] (long-to-bitseq x))
  (-to-bitset [x] (-> x long-to-bitseq bitseq-to-bitset))
  (-to-bytes  [x] (-> x long-to-bitseq bitseq-to-bitset bitset-to-bytes))
  (-to-number [x] x))

(extend-type clojure.lang.BigInt
  protocol.binary/IBinary
  (-to-bitstr [x] (-> x bigint-to-bitset bitset-to-bitseq bitseq-to-bitstr))
  (-to-bitseq [x] (-> x bigint-to-bitset bitset-to-bitseq))
  (-to-bitset [x] (bigint-to-bitset x))
  (-to-bytes  [x] (-> x bigint-to-bitset bitset-to-bytes))
  (-to-number [x] x))

;; 5. byte[]

(extend-type (Class/forName "[B")
  protocol.binary/IBinary
  (-to-bitstr [x] (-> x bytes-to-bitset bitset-to-bitseq bitseq-to-bitstr))
  (-to-bitseq [x] (-> x bytes-to-bitset bitset-to-bitseq))
  (-to-bitset [x] (bytes-to-bitset x))
  (-to-bytes  [x] x)
  (-to-number [x] (-> x bytes-to-bitset bitset-to-bitseq bitseq-to-number))
  
  protocol.binary/IByteSource
  (-to-inputstream [bs]
    (ByteArrayInputStream. bs))
  
  protocol.binary/IByteSink
  (-to-outputstream [bs]
    (ByteArrayOutputStream. bs)))

;; 6. io.inputstream

(extend-type java.io.InputStream
  protocol.binary/IBinary
  (-to-bitstr [x] (-> x inputstream-to-bytes bytes-to-bitset bitset-to-bitseq bitseq-to-bitstr))
  (-to-bitseq [x] (-> x inputstream-to-bytes bytes-to-bitset bitset-to-bitseq))
  (-to-bitset [x] (-> inputstream-to-bytes bytes-to-bitset))
  (-to-bytes  [x] (inputstream-to-bytes x))
  (-to-number [x] (-> x inputstream-to-bytes bytes-to-bitset bitset-to-bitseq bitseq-to-number))

  protocol.binary/IByteSource
  (-to-inputstream [x] x)
  
  protocol.binary/IByteChannel
  (-to-channel [x]
    (Channels/newChannel x)))

;; 7. nio.bytebuffer

(extend-type java.nio.ByteBuffer
  protocol.binary/IBinary
  (-to-bitstr [x] (-> x bytebuffer-to-bytes bytes-to-bitset bitset-to-bitseq bitseq-to-bitstr))
  (-to-bitseq [x] (-> x bytebuffer-to-bytes bytes-to-bitset bitset-to-bitseq))
  (-to-bitset [x] (-> bytebuffer-to-bytes bytes-to-bitset))
  (-to-bytes  [x] (bytebuffer-to-bytes x))
  (-to-number [x] (-> x bytebuffer-to-bytes bytes-to-bitset bitset-to-bitseq bitseq-to-number))

  protocol.binary/IByteSource
  (-to-inputstream [buff]
    (ByteBufferInputStream. buff))
  
  protocol.binary/IByteSink
  (-to-outputstream [buff]
    (ByteBufferOutputStream. buff)))

;; 8. io.file

(extend-type java.io.File
  protocol.binary/IByteSource
  (-to-inputstream [file]
    (FileInputStream. file))
  
  protocol.binary/IByteSink
  (-to-outputstream [file]
    (FileOutputStream. file))

  protocol.binary/IByteChannel
  (-to-channel [file]
    (.getChannel (FileInputStream. file))))

;; 9.  nio.path

(extend-type java.nio.file.Path
  protocol.binary/IByteSource
  (-to-inputstream [path]
    (FileInputStream. (.toFile path)))
  
  protocol.binary/IByteSink
  (-to-outputstream [path]
    (FileOutputStream. (.toFile path)))

  protocol.binary/IByteChannel
  (-to-channel [path]
    (FileChannel/open path (into-array java.nio.file.OpenOption []))))

;; 10. nio.channel

(extend-type java.nio.channels.Channel
  protocol.binary/IByteSource
  (-to-inputstream [ch]
    (Channels/newInputStream ch))
  
  protocol.binary/IByteSink
  (-to-outputstream [ch]
    (Channels/newOutputStream ch))

  protocol.binary/IByteChannel
  (-to-channel [ch] ch))

;; 11. net.url

(extend-type java.net.URL
  protocol.binary/IByteSource
  (-to-inputstream [url]
    (.openStream url))

  protocol.binary/IByteChannel
  (-to-channel [url]
    (Channels/newChannel (.openStream url))))

;; 12. net.uri

(extend-type java.net.URI
  protocol.binary/IByteSource
  (-to-inputstream [uri]
    (.openStream (.toURL uri)))

  protocol.binary/IByteChannel
  (-to-channel [uri]
    (Channels/newChannel (.openStream (.toURL uri)))))

(defn- prepare-inputstream [obj]
  (cond (instance? java.io.InputStream obj)
        obj

        (satisfies? protocol.binary/IBinary obj)
        (ByteArrayInputStream. (protocol.binary/-to-bytes obj))
        
        :else
        obj))

(definvoke inputstream
  "creates an inputstream from various representations
 
   (inputstream 9223372036854775808N)
   => java.io.ByteArrayInputStream"
  {:added "3.0"}
  [:compose {:val (comp protocol.binary/-to-inputstream prepare-inputstream)
             :arglists '([obj])}])

(defn outputstream
  "creates an inputstream from various representations
 
   (outputstream (fs/file \"project.clj\"))
   => java.io.FileOutputStream"
  {:added "3.0"}
  ([obj]
   (protocol.binary/-to-outputstream obj)))

(defn channel
  "creates a channel from various representations
 
   (channel (fs/file \"project.clj\"))
   => java.nio.channels.Channel"
  {:added "3.0"}
  ([obj]
   (protocol.binary/-to-channel obj)))

(defn- prepare-binary [obj]
  (cond (bytes? obj) obj

        (satisfies? protocol.binary/IByteSource obj)
        (protocol.binary/-to-bytes (inputstream obj))
        
        :else obj))

(definvoke bitstr
  "converts to a bitstr binary representation
 
   (binary/bitstr (byte-array [49]))
   => \"100011\""
  {:added "0.1"}
  [:compose {:val (comp protocol.binary/-to-bitstr prepare-binary)
             :arglists '([obj])}])

(definvoke bitseq
  "converts to a bitseq binary representation
 
   (binary/bitseq (byte-array [49]))
   => [1 0 0 0 1 1]"
  {:added "0.1"}
  [:compose {:val (comp protocol.binary/-to-bitseq prepare-binary)
             :arglists '([obj])}])

(definvoke bitset
  "converts to a bitset binary representation
 
   (-> (binary/bitset \"100011\")
       (bitset-to-bitseq))
   => [1 0 0 0 1 1]"
  {:added "0.1"}
  [:compose {:val (comp protocol.binary/-to-bitset prepare-binary)
             :arglists '([obj])}])

(definvoke bytes
  "converts to a byte array binary representation
 
   (-> (binary/bytes \"100011\")
       (seq))
   => [49]"
  {:added "0.1"}
  [:compose {:val (comp protocol.binary/-to-bytes prepare-binary)
             :arglists '([obj])}])

(definvoke number
  "converts to a number binary representation
 
   (binary/number \"100011\")
   => 49
   "
  {:added "0.1"}
  [:compose {:val (comp protocol.binary/-to-number prepare-binary)
             :arglists '([obj])}])

(comment
  (./code:arrange)
  (./code:import)
  (./code:incomplete)
  )
