(ns hara.protocol.watch)

(defprotocol IWatch
  (-add-watch    [obj k f opts])
  (-remove-watch [obj k opts])
  (-list-watch   [obj opts]))
