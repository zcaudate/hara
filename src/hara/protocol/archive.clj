(ns hara.protocol.archive)

(defprotocol IArchive
  (-url     [archive])
  (-path    [archive entry])
  (-list    [archive])
  (-has?    [archive entry])
  (-archive [archive root inputs])
  (-extract [archive output entries])
  (-insert  [archive entry input])
  (-remove  [archive entry])
  (-write   [archive entry stream])
  (-stream  [archive entry]))

(defmulti -open
  "allows the opening of zip and jar files"
  {:added "3.0"}
  (fn [type path] type))
