(ns hara.protocol.keystore)

(defprotocol IKeystore
  (-put-in    [store path v])
  (-peek-in   [store path])
  (-keys-in   [store path])
  (-drop-in   [store path])
  (-set-in    [store path v])
  (-select-in [store path v])
  (-batch-in  [store path add-map remove-vec]))

(defmulti -create
  "creates a keystore for use with components"
  {:added "3.0"}
  :type)
