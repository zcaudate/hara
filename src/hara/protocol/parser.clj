(ns hara.protocol.parser)

(defprotocol IParser
  (-parse [parser opts input]))
