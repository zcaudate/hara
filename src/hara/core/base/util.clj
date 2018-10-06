(ns hara.core.base.util)

(defn T
  "Returns `true` for any combination of input `args`
 
   (T) => true
   (T :hello) => true
   (T 1 2 3) => true"
  {:added "3.0"}
  [& args] true)

(defn F
  "Returns `false` for any combination of input `args`
 
   (F) => false
   (F :hello) => false
   (F 1 2 3) => false"
  {:added "3.0"}
  [& args] false)

(defn NIL
  "Returns `nil` for any combination of input `args`
 
   (NIL) => nil
   (NIL :hello) => nil
   (NIL 1 2 3) => nil"
  {:added "3.0"}
  [& args] nil)

(defn queue
  "Returns a `clojure.lang.PersistentQueue` object.
 
   (def a (queue 1 2 3 4))
   (pop a) => [2 3 4]"
  {:added "3.0"}
  ([] (clojure.lang.PersistentQueue/EMPTY))
  ([x] (conj (queue) x))
  ([x & xs] (apply conj (queue) x xs)))

(defn uuid
  "Returns a `java.util.UUID` object
 
   (uuid) => #(instance? java.util.UUID %)
 
   (uuid \"00000000-0000-0000-0000-000000000000\")
   => #uuid \"00000000-0000-0000-0000-000000000000\""
  {:added "3.0"}
  ([] (java.util.UUID/randomUUID))
  ([id]
   (cond (string? id)
         (java.util.UUID/fromString id)

         (bytes? id)
         (java.util.UUID/nameUUIDFromBytes id)

         :else
         (throw (ex-info (str id " can only be a string or byte array")))))
  ([^Long msb ^Long lsb]
   (java.util.UUID. msb lsb)))

(defn instant
  "Returns a `java.util.Date` object
 
   (instant) => #(instance? java.util.Date %)
 
   (instant 0) => #inst \"1970-01-01T00:00:00.000-00:00\""
  {:added "3.0"}
  ([] (java.util.Date.))
  ([^Long val] (java.util.Date. val)))

(defn uri
  "Returns a `java.net.URI` object
 
   (uri \"http://www.google.com\")
   => #(instance? java.net.URI %)"
  {:added "3.0"}
  [path] (java.net.URI/create path))

(defn hash-label
  "Returns a keyword repesentation of the hash-code. For use in
    generating internally unique keys
 
   (hash-label 1) => \"__1__\"
   (hash-label \"a\" \"b\" \"c\") => \"__97_98_99__\"
   (hash-label \"abc\") => \"__96354__\""
  {:added "3.0"}
  ([^Object obj] (str "__" (.hashCode obj) "__"))
  ([^Object obj & more]
   (let [result (->> (cons obj more)
                     (map (fn [^Object x] (.hashCode x)))
                     (clojure.string/join "_"))]
     (str "__" result "__"))))
