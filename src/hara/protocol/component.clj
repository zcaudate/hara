(ns hara.protocol.component)

(defprotocol IComponent
  (-start [component])
  (-stop  [component])
  (-started? [component])
  (-stopped? [component])
  (-properties [component]))