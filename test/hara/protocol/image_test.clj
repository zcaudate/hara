(ns hara.protocol.image-test
  (:use hara.test)
  (:require [hara.protocol.image :refer :all]))

^{:refer hara.protocol.image/-image-meta :added "3.0"}
(comment "additional information about the image")

^{:refer hara.protocol.image/-image :added "3.0"}
(comment "creates an image based on inputs")

^{:refer hara.protocol.image/-blank :added "3.0"}
(comment "creates an empty image")

^{:refer hara.protocol.image/-read :added "3.0"}
(comment "reads an image from file")

^{:refer hara.protocol.image/-display :added "3.0"}
(comment "displays an image")

^{:refer hara.protocol.image/-display-class :added "3.0"}
(comment "types that are able to be displayed")

(comment
  (./import))
