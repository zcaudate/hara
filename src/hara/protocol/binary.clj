(ns hara.protocol.binary)

(defprotocol IBinary
  (-to-bitstr [x])
  (-to-bitseq [x])
  (-to-bitset [x])
  (-to-bytes  [x])
  (-to-number [x]))

(defprotocol IByteSource
  (-to-inputstream [obj]))

(defprotocol IByteSink
  (-to-outputstream [obj]))

(defprotocol IByteChannel
  (-to-channel [obj]))
