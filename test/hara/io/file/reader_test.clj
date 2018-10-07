(ns hara.io.file.reader-test
  (:use hara.test)
  (:require [hara.io.file.reader :refer :all]))

^{:refer hara.io.file.reader/-reader :added "3.0"}
(fact "creates a reader for a given input"

  (-> (-reader :pushback "project.clj" {})
      (read)
      first)
  => 'defproject)

^{:refer hara.io.file.reader/reader-types :added "3.0"}
(fact "returns the types of readers"

  (reader-types)
  => (contains [:input-stream :buffered :file
                :string :pushback :char-array
                :piped :line-number]))


^{:refer hara.io.file.reader/reader :added "3.0"}
(fact "creates a reader for a given input"

  (-> (reader :pushback "project.clj")
      (read)
      first)
  => 'defproject)
