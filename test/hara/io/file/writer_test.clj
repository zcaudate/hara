(ns hara.io.file.writer-test
  (:use hara.test)
  (:require [hara.io.file.writer :refer :all]
            [hara.io.file :as fs]))

^{:refer hara.io.file.writer/-writer :added "3.0"}
(fact "creates a writer for a given input"

  (doto (-writer :buffered {:path "hello.txt"})
    (.write "Hello" 0 4)
    (.write "World" 0 4)
    (.close))

  (slurp "hello.txt") => "HellWorl"

  ^:hidden
  (fs/delete "hello.txt"))

^{:refer hara.io.file.writer/writer-types :added "3.0"}
(fact "returns the types of writers"

  (writer-types)
  => (contains [:buffered :char-array :file
                :output-stream :piped :print :string]
               :in-any-order))

^{:refer hara.io.file.writer/writer :added "3.0"}
(fact "creates a writer given options"

  (doto (writer :buffered {:path "hello.txt"})
    (.write "Hello" 0 4)
    (.write "World" 0 4)
    (.close))

  (slurp "hello.txt") => "HellWorl"

  ^:hidden
  (fs/delete "hello.txt"))

