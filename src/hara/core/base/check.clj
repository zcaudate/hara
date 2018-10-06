(ns hara.core.base.check
  (:refer-clojure :exclude [boolean? double? bigdec? uuid? uri? bytes?]))

;; ## Type Predicates
;;
;; Adds additional type predicates that are not in clojure.core

(defn boolean?
  "Returns `true` if `x` is of type `java.lang.Boolean`.
 
   (boolean? true)   => true
   (boolean? false)  => true"
  {:added "3.0"}
  [x] (instance? java.lang.Boolean x))

(defn hash-map?
  "Returns `true` if `x` implements `clojure.lang.APersistentMap`.
 
   (hash-map? {})    => true
   (hash-map? [])    => false"
  {:added "3.0"}
  [x] (instance? clojure.lang.APersistentMap x))

(defn lazy-seq?
  "Returns `true` if `x` implements `clojure.lang.LazySeq`.
 
   (lazy-seq? (map inc [1 2 3]))  => true
   (lazy-seq? ())    => false"
  {:added "3.0"}
  [x] (instance? clojure.lang.LazySeq x))

(defn byte?
  "Returns `true` if `x` is of type `java.lang.Byte`
 
   (byte? (byte 1)) => true"
  {:added "3.0"}
  [x] (instance? java.lang.Byte x))

(defn short?
  "Returns `true` if `x` is of type `java.lang.Short`
 
   (short? (short 1)) => true"
  {:added "3.0"}
  [x] (instance? java.lang.Short x))

(defn long?
  "Returns `true` if `x` is of type `java.lang.Long`.
 
   (long? 1)          => true
   (long? 1N)         => false"
  {:added "3.0"}
  [x] (instance? java.lang.Long x))

(defn bigint?
  "Returns `true` if `x` is of type `clojure.lang.BigInt`.
 
   (bigint? 1N)       => true
   (bigint? 1)        =>  false"
  {:added "3.0"}
  [x] (instance? clojure.lang.BigInt x))

(defn double?
  "Returns `true` if `x` is of type `java.lang.Double`.
 
   (double? 1)            => false
   (double? (double 1))   => true"
  {:added "3.0"}
  [x] (instance? java.lang.Double x))

(defn bigdec?
  "Returns `true` if `x` is of type `java.math.BigDecimal`.
 
   (bigdec? 1M)       => true
   (bigdec? 1.0)      => false"
  {:added "3.0"}
  [x] (instance? java.math.BigDecimal x))

(defn instant?
  "Returns `true` if `x` is of type `java.util.Date`.
 
   (instant? (java.util.Date.)) => true"
  {:added "3.0"}
  [x] (instance? java.util.Date x))

(defn uuid?
  "Returns `true` if `x` is of type `java.util.UUID`.
 
   (uuid? (java.util.UUID/randomUUID)) => true"
  {:added "3.0"}
  [x] (instance? java.util.UUID x))

(defn uri?
  "Returns `true` if `x` is of type `java.net.URI`.
 
   (uri? (java.net.URI. \"http://www.google.com\")) => true"
  {:added "3.0"}
  [x] (instance? java.net.URI x))

(defn url?
  "Returns `true` if `x` is of type `java.net.URL`.
 
   (url? (java.net.URL. \"file:/Users/chris/Development\")) => true"
  {:added "3.0"}
  [x] (instance? java.net.URL x))

(defn regexp?
  "Returns `true` if `x` implements `clojure.lang.IPersistentMap`.
 
   (regexp? #\"\\d+\") => true"
  {:added "3.0"}
  [x] (instance? java.util.regex.Pattern x))

(defn bytes?
  "Returns `true` if `x` is a primitive `byte` array.
 
   (bytes? (byte-array 8)) => true"
  {:added "3.0"}
  [^Object x]
  (= (Class/forName "[B")
     (.getClass x)))

(defn atom?
  "Returns `true` if `x` is of type `clojure.lang.Atom`.
 
   (atom? (atom nil)) => true"
  {:added "3.0"}
  [obj]
  (instance? clojure.lang.Atom obj))

(defn ref?
  "Returns `true` if `x` is of type `clojure.lang.Ref`.
 
   (ref? (ref nil)) => true"
  {:added "3.0"}
  [obj]
  (instance? clojure.lang.Ref obj))

(defn agent?
  "Returns `true` if `x` is of type `clojure.lang.Agent`.
 
   (agent? (agent nil)) => true"
  {:added "3.0"}
  [obj]
  (instance? clojure.lang.Agent obj))

(defn iref?
  "Returns `true` if `x` is of type `clojure.lang.IRef`.
 
   (iref? (atom 0))  => true
   (iref? (ref 0))   => true
   (iref? (agent 0)) => true
   (iref? (promise)) => false
   (iref? (future))  => false"
  {:added "3.0"}
  [obj]
  (instance? clojure.lang.IRef obj))

(defn ideref?
  "Returns `true` if `x` is of type `java.lang.IDeref`.
 
   (ideref? (atom 0))  => true
   (ideref? (promise)) => true
   (ideref? (future))  => true"
  {:added "3.0"}
  [obj]
  (instance? clojure.lang.IDeref obj))

(defn promise?
  "Returns `true` is `x` is a promise
 
   (promise? (promise)) => true
   (promise? (future))  => false"
  {:added "3.0"}
  [^Object obj]
  (let [^String s (.getName ^Class (type obj))]
    (.startsWith s "clojure.core$promise$")))

(defn thread?
  "Returns `true` is `x` is a thread
 
   (thread? (Thread/currentThread)) => true"
  {:added "3.0"}
  [obj]
  (instance? java.lang.Thread obj))

(defn iobj?
  "checks if a component is instance of clojure.lang.IObj
 
   (iobj? 1) => false
 
   (iobj? {}) => true"
  {:added "3.0"}
  [x]
  (instance? clojure.lang.IObj x))

(defn type-checker
  "Returns the checking function associated with `k`
 
   (type-checker :string) => #'clojure.core/string?
 
   (require '[hara.core.base.check :refer [bytes?]])
   (type-checker :bytes)  => #'hara.core.base.check/bytes?"
  {:added "3.0"}
  [k]
  (resolve (symbol (str (name k) "?"))))

(defn comparable?
  "Returns `true` if `x` and `y` both implements `java.lang.Comparable`.
   
   (comparable? 1 1) => true"
  {:added "3.0"}
  [x y]
  (and (instance? Comparable x)
       (instance? Comparable y)
       (= (type x) (type y))))

(defn edn?
  "Is the root of x an edn type?"
  [x]
  (or (nil? x)
      (boolean? x)
      (string? x)
      (char? x)
      (symbol? x)
      (keyword? x)
      (number? x)
      (seq? x)
      (vector? x)
      (record? x)
      (map? x)
      (set? x)
      (tagged-literal? x)
      (var? x)
      (regexp? x)))
