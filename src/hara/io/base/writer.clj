(ns hara.io.base.writer
  (:require [hara.io.file.charset :as charset]
            [hara.io.file.option :as option]
            [hara.io.file.path :as path])
  (:import (java.io BufferedWriter CharArrayWriter FileWriter FilterWriter OutputStream OutputStreamWriter PipedWriter PrintWriter StringWriter Writer)
           (java.nio.file Files OpenOption)))

(defmulti -writer
  "creates a writer for a given input
 
   (doto (writer :buffered {:path \"hello.txt\"})
     (.write \"Hello\" 0 4)
     (.write \"World\" 0 4)
     (.close))
 
   (slurp \"hello.txt\") => \"HellWorl\"
 
   "
  {:added "3.0"}
  (fn [type opts] type))

(defmethod -writer :buffered
  [_ {:keys [path charset] :as opts}]
  (Files/newBufferedWriter (path/path path)
                           (charset/charset (or charset
                                                (charset/charset-default)))
                           (->> [:create]
                                (or (:options opts))
                                (mapv option/+open-options+)
                                (into-array OpenOption))))

(defmethod -writer :char-array
  [_ {:keys [size] :as opts}]
  (CharArrayWriter. (or size 0)))

(defmethod -writer :file
  [_ {:keys [path append] :as opts :or {append false}}]
  (let [path (.toFile (path/path path))]
    (FileWriter. path append)))

(defmethod -writer :output-stream
  [_ {:keys [stream charset] :as opts}]
  (OutputStreamWriter. ^OutputStream stream (charset/charset (or charset
                                                                 (charset/charset-default)))))

(defmethod -writer :piped
  [_ {:keys [reader] :as opts}]
  (PipedWriter. reader))

(defmethod -writer :print
  [_ {:keys [out]}]
  (PrintWriter. out))

(defmethod -writer :string
  [_ {:keys [size] :as opts}]
  (StringWriter. size))

(defn writer-types
  "returns the types of writers
 
   (writer-types)
   => (contains [:buffered :char-array :file
                 :output-stream :piped :print :string]
                :in-any-order)"
  {:added "3.0"}
  []
  (keys (.getMethodTable ^clojure.lang.MultiFn -writer)))

(defn writer
  "creates a writer given options

   (doto (writer :buffered {:path \"hello.txt\"})
     (.write \"Hello\" 0 4)
     (.write \"World\" 0 4)
     (.close))

   (slurp \"hello.txt\") => \"HellWorl\"

   "
  {:added "3.0"}
  ([opts]
   (writer :buffered opts))
  ([type opts]
   (-writer type opts)))