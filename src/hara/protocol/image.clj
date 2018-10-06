(ns hara.protocol.image)

(defmulti -image-meta
  "additional information about the image"
  {:added "3.0"}
  (fn [cls] cls))

(defprotocol ISize
  (-height        [dim])
  (-width         [dim]))

(defprotocol IRepresentation
  (-channels   [img])
  (-size       [img])
  (-model      [img])
  (-data       [img])
  (-subimage   [img x y w h]))

(defmulti -image
  "creates an image based on inputs"
  {:added "3.0"}
  (fn [size model data type] type))

(defmulti -blank
  "creates an empty image"
  {:added "3.0"}
  (fn [size model type] type))

(defmulti -read
  "reads an image from file"
  {:added "3.0"}
  (fn [source model type] type))

(defprotocol ITransfer
  (-to-byte-gray [image])
  (-to-int-argb  [image])
  (-write   [image opts sink]))

(defmulti -display
  "displays an image"
  {:added "3.0"}
  (fn [img opts type] type))

(defmulti -display-class
  "types that are able to be displayed"
  {:added "3.0"}
  (fn [type] type))

(defprotocol ITransform
  (-op  [transform img args opts]))
