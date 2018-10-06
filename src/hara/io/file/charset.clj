(ns hara.io.file.charset
  (:import (java.nio.charset Charset)))

(defn charset-default
  "returns the default charset
 
   (charset-default)
   => \"UTF-8\""
  {:added "3.0"}
  []
  (str (Charset/defaultCharset)))

(defn charset-list
  "returns the list of available charset
 
   (charset-list)
   => (\"Big5\" \"Big5-HKSCS\" ... \"x-windows-iso2022jp\")"
  {:added "3.0"}
  []
  (keys (Charset/availableCharsets)))

(defn charset
  "constructs a charset object from a string
   (charset \"UTF-8\")
   => java.nio.charset.Charset"
  {:added "3.0"}
  [s]
  (Charset/forName s))
