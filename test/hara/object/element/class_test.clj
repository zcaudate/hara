(ns hara.object.element.class-test
  (:use hara.test)
  (:require [hara.object.element.class :refer :all]))

^{:refer hara.object.element.class/type->raw :added "3.0"}
(fact "converts to the raw representation"

  (type->raw Class) => "java.lang.Class"
  (type->raw 'byte) => "B")

^{:refer hara.object.element.class/raw-array->string :added "3.0"}
(fact "converts the raw representation to a more readable form"

  (raw-array->string "[[B") => "byte[][]"
  (raw-array->string "[Ljava.lang.Class;") => "java.lang.Class[]")

^{:refer hara.object.element.class/raw->string :added "3.0"}
(fact "converts the raw array representation to a human readable form"

  (raw->string "[[V") => "void[][]"
  (raw->string "[Ljava.lang.String;") => "java.lang.String[]")

^{:refer hara.object.element.class/string-array->raw :added "3.0"}
(fact "converts the human readable form to a raw string"

  (string-array->raw "java.lang.String[]") "[Ljava.lang.String;")

^{:refer hara.object.element.class/string->raw :added "3.0"}
(fact "converts any string to it's raw representation"

  (string->raw "java.lang.String[]") => "[Ljava.lang.String;"

  (string->raw "int[][][]") => "[[[I")

^{:refer hara.object.element.class/-class-convert :added "3.0"}
(fact "converts a string to its representation. Implementation function"

  (-class-convert Class  :string) => "java.lang.Class"

  (-class-convert "byte" :class) => Byte/TYPE

  (-class-convert "byte" :container) => Byte)

^{:refer hara.object.element.class/class-convert :added "3.0"}
(fact "Converts a class to its representation."

  (class-convert "byte") => Byte/TYPE

  (class-convert 'byte :string) => "byte"

  (class-convert (Class/forName "[[B") :string) => "byte[][]")