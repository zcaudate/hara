(ns hara.protocol.transport)

(defprotocol IConnection
  (-request [conn package])
  (-send    [conn package]))