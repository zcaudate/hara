(ns hara.protocol.match)

(defprotocol ITemplate
  (-match [template obj]))
