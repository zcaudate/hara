(ns hara.protocol.extern)

(defprotocol IMemory
  (-malloc [impl bytes] "Allocate the memory required by this object.")
  (-free   [impl address] "Free the memory associated with this object."))

(defprotocol IStruct
  (-sizeof [impl] "Returns the size of a value or class in bytes"))