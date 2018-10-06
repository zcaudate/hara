(ns hara.protocol.print)

(defmulti -serialize-node
  "an extendable method for defining serializing tags"
  {:added "3.0"}
  (fn [[tag & more]] tag))

(defmulti -document
  "representation of a printable document object"
  {:added "3.0"}
  (fn [options element text]
    (when (:print-color options)
      (:color-markup options))))

(defmulti -text
  "representation of a printable text object"
  {:added "3.0"}
  (fn [options element text]
    (when (:print-color options)
      (:color-markup options))))

(defprotocol IEdn
  (-edn [x]))

(defprotocol IOverride)

(defprotocol IVisitor

  (-visit-unknown [this x])
  (-visit-nil [this])
  (-visit-boolean [this x])
  (-visit-string [this x])
  (-visit-character [this x])
  (-visit-symbol [this x])
  (-visit-keyword [this x])
  (-visit-number [this x])
  (-visit-seq [this x])
  (-visit-vector [this x])
  (-visit-map [this x])
  (-visit-set [this x])
  (-visit-tagged [this x])

  ;; Not strictly Edn...
  (-visit-meta [this meta x])
  (-visit-var [this x])
  (-visit-pattern [this x])
  (-visit-record [this x]))
