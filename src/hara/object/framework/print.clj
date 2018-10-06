(ns hara.object.framework.print
  (:require [hara.object.framework.read :as read]
            [hara.protocol.object :as protocol.object]))

(defn assoc-print-vars
  "helper to assoc print vars in options
 
   (print/assoc-print-vars {} {:tag \"hello\"})
   => {:tag \"hello\"}"
  {:added "3.0"}
  [read {:keys [tag prefix print display]}]
  (cond-> read
    prefix   (assoc :prefix prefix)
    tag      (assoc :tag tag)
    print    (assoc :print print)
    display  (assoc :display display)))

(defn format-value
  "formats the object into a readable string
 
   (print/format-value (test.Cat. \"fluffy\")
                       {:tag \"cat\"})
   => \"#cat{:name \\\"fluffy\\\"}\""
  {:added "3.0"}
  [v {:keys [tag print display prefix]}]
  (str (or prefix "#")
       (or tag (.getName ^Class (type v)))
       (let [out (if print
                   (print v)
                   (cond-> (read/to-data v)
                     display display))]
         (if (string? out)
           (str " \"" out "\"")
           out))))

(defmacro extend-print
  "extend `print-method` function for a particular class
 
   (macroexpand-1 '(print/extend-print test.Cat))"
  {:added "3.0"}
  [cls]
  `(defmethod print-method ~cls
     [~'v ^java.io.Writer w#]
     (let [read# (read/meta-read ~cls)]
       (.write w# (format-value ~'v read#)))))
