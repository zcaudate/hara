(ns hara.protocol.constructor)

(defprotocol IConstructor
  (-construct [cls] [cls opts]))
