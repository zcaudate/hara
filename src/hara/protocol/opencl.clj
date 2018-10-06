(ns hara.protocol.opencl)

(defmulti -opencl-create-input
  {:added "0.1"}
  (fn [param arg context] (:type param)))

(defmulti -opencl-write-input
  {:added "0.1"}
  (fn [param arg buffer queue] (:type param)))

(defmulti -opencl-read-output
  {:added "0.1"}
  (fn [param arg buffer queue] (:type param)))
