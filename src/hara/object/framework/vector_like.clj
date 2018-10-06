(ns hara.object.framework.vector-like
  (:require [hara.object.framework.print :as print]
            [hara.protocol.object :as protocol.object]
            [hara.object.framework.read :as read]
            [hara.object.framework.write :as write]
            [hara.function :as fn]))

(defmacro extend-vector-like
  "sets the fields of an object with keyword
 
   (extend-vector-like test.Cat {:read (fn [x] (seq (.getName x)))
                                 :write (fn [arr] (test.Cat. (apply str arr)))})
 
   (test.Cat. \"spike\")
   ;=> #test.Cat(\\s \\p \\i \\k \\e)
 "
  {:added "3.0"}
  [cls {:keys [read write] :as opts}]
  (cond-> []
    read  (conj `(defmethod protocol.object/-meta-read ~cls
                   [~'_]
                   ~(-> {:to-vector read}
                        (print/assoc-print-vars opts))))
    write (conj `(defmethod protocol.object/-meta-write ~cls
                   [~'_]
                   {:from-vector ~write}))

    true  (conj `(do (fn/memoize-remove read/meta-read ~cls)
                     (fn/memoize-remove write/meta-write ~cls)
                     (print/extend-print ~cls)))))
