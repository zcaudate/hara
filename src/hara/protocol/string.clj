(ns hara.protocol.string)

(defprotocol IString
  (-to-string [x]))

(defmulti -from-string
  "common method for extending string-like objects"
  {:added "3.0"}
  (fn [string type opts] type))

(defmulti -path-separator
  "common method for finding the path separator for a given data type"
  {:added "3.0"}
  identity)
