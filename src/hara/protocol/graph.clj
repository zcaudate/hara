(ns hara.protocol.graph)

(defprotocol IGraph
  (-install-schema  [db schema])
  (-purge   [db opts])
  (-select  [db selector opts])
  (-insert  [db data opts])
  (-delete  [db selector opts])
  (-retract [db selector key opts])
  (-update  [db selector data opts]))

(defmulti -create
  "creates a graph database for use with components"
  {:added "3.0"}
  :type)
