(ns hara.io.file.reader
  (:require [hara.io.file.charset :as charset]
            [hara.io.file.path :as path])
  (:import (java.io BufferedReader CharArrayReader File FileReader InputStream InputStreamReader LineNumberReader PipedReader PipedWriter PushbackReader Reader StringReader)
           (java.nio.file Files)))

(defmulti -reader
  "creates a reader for a given input
 
   (-> (reader :pushback \"project.clj\" {})
       (read)
       second)
   => 'hara/base"
  {:added "3.0"}
  (fn [type path opts] type))

(defmethod -reader :buffered
  [_ io opts]
  (Files/newBufferedReader (path/path io)
                           (charset/charset (or (:charset opts)
                                                (charset/charset-default)))))

(defmethod -reader :char-array
  [_ io opts]
  (let [arr    io
        offset (or (:offset opts) 0)
        length (or (:length opts) (count arr))]
    (CharArrayReader. arr offset length)))

(defmethod -reader :file
  [_ io opts]
  (let [path (path/path io)]
    (FileReader. (str path))))

(defmethod -reader :input-stream
  [_ io opts]
  (let [stream  io
        charset (or (:charset opts) (charset/charset-default))]
    (InputStreamReader. ^InputStream stream  ^String charset)))

(defmethod -reader :line-number
  [_ io opts]
  (LineNumberReader. (-reader :buffered io opts)))

(defmethod -reader :piped
  [_ io opts]
  (PipedReader. ^PipedWriter io))

(defmethod -reader :pushback
  [_ io opts]
  (PushbackReader. (-reader :buffered io opts)))

(defmethod -reader :string
  [_ io opts]
  (StringReader. ^String io))

(defn reader-types
  "returns the types of readers
 
   (reader-types)
   => (contains [:input-stream :buffered :file
                 :string :pushback :char-array
                 :piped :line-number])"
  {:added "3.0"}
  []
  (keys (.getMethodTable ^clojure.lang.MultiFn -reader)))

(defn reader
  "creates a reader for a given input

   (-> (reader :pushback \"project.clj\")
       (read)
       second)
   => 'hara/base"
  {:added "3.0"}
  ([input]
   (reader :buffered input {}))
  ([type input]
   (reader type input {}))
  ([type input opts]
   (-reader type input opts)))
