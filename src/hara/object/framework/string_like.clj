(ns hara.object.framework.string-like
  (:require [hara.object.framework.print :as print]
            [hara.protocol.object :as protocol.object]
            [hara.protocol.string :as protocol.string]
            [hara.object.framework.read :as read]
            [hara.object.framework.write :as write]
            [hara.function :as fn]))

(defmacro extend-string-like
  "creates an entry for string-like classes
 
   (extend-string-like
    java.io.File
    {:tag \"path\"
     :read (fn [f] (.getPath f))
     :write (fn [^String path] (java.io.File. path))})
 
   (object/from-data \"/home\" java.io.File)
 
   (with-out-str
     (prn (java.io.File. \"/home\")))
   => \"#path \\\"/home\\\"\\n\""
  {:added "3.0"}
  [cls {:keys [read write meta] :as opts}]
  `(vector
    (defmethod protocol.object/-meta-read ~cls
      [~'_]
      ~(-> {:to-string `protocol.string/-to-string}
           (print/assoc-print-vars opts)))

    (defmethod protocol.object/-meta-write ~cls
      [~'_]
      {:from-string (fn [s#] (protocol.string/-from-string s# ~cls nil))})

    (extend-protocol protocol.string/IString
      ~cls
      (-to-string [obj#]
        (~(or read `str) obj#)))

    ~(if write
       `(defmethod protocol.string/-from-string ~cls
          [data# ~'_ ~'_]
          (~write data#))
       `(defmethod protocol.string/-from-string ~cls
          [data# type# ~'_]
          (throw (Exception. (str "Cannot create " type# " from string.")))))

    (do (fn/memoize-remove read/meta-read ~cls)
        (fn/memoize-remove write/meta-write ~cls)
        (print/extend-print ~cls))))
