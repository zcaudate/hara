(ns hara.protocol.object)

(defmulti -meta-read
  "accesses class meta information for reading from object"
  {:added "3.0"}
  identity)

(defmethod -meta-read :default
  [_]
  {})

(defmulti -meta-write
  "accesses class meta information for writing to the object"
  {:added "3.0"}
  identity)

(defmethod -meta-write :default
  [_]
  {})