(ns hara.protocol.time)

(defmulti -time-meta
  "accesses the meta properties of a class"
  {:added "3.0"}
  (fn [cls] cls))

(defprotocol IInstant
  (-to-long       [t])
  (-has-timezone? [t])
  (-get-timezone  [t])
  (-with-timezone [t tz]))

(defmulti -from-long
  "creates a time representation from a long"
  {:added "3.0"}
  (fn [long opts] (:type opts)))

(defmulti -now
  "creates a representation of the current time"
  {:added "3.0"}
  (fn [opts] (:type opts)))

(defprotocol IRepresentation
  (-millisecond  [t opts])
  (-second       [t opts])
  (-minute       [t opts])
  (-hour         [t opts])
  (-day          [t opts])
  (-day-of-week  [t opts])
  (-month        [t opts])
  (-year         [t opts]))

(defprotocol IDuration
  (-to-length  [d opts]))

(defmulti -from-length
  "creates a representation of a duration"
  {:added "3.0"}
  (fn [long opts] (:type opts)))

(defmulti -formatter
  "create a representation of a formatter"
  {:added "3.0"}
  (fn [pattern opts] (:type opts)))

(defmulti -format
  "a general function to format what time looks like"
  {:added "3.0"}
  (fn [formatter t opts]
    [(class formatter) (class t)]))

(defmulti -parser
  "creates a parser for parsing strings"
  {:added "3.0"}
  (fn [pattern opts] (:type opts)))

(defmulti -parse
  "generic parse function for a string representation"
  {:added "3.0"}
  (fn [parser s opts]
    [(class parser) (:type opts)]))
