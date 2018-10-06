(ns hara.string.base.impl
  (:require [hara.protocol.string :as protocol.string]
            [hara.string.base.common :as common]
            [hara.function.base.invoke :refer [definvoke]]))

(defn from-string
  "converts a string to an object
 
   (from-string \"a\" clojure.lang.Symbol)
   => 'a
 
   (from-string \"hara.string\" clojure.lang.Namespace)
   => (find-ns 'hara.string)"
  {:added "3.0"}
  ([string type]
    (from-string string type nil))
  ([string type opts]
   (protocol.string/-from-string string type opts)))

(defn to-string
  "converts an object to a string
 
   (to-string :hello/world)
   => \"hello/world\"
 
   (to-string *ns*)
   => \"hara.string.base.impl-test\""
  {:added "3.0"}
  [string]
  (protocol.string/-to-string string))

(definvoke path-separator
  "returns the default path separator for an object
 
   (path-separator clojure.lang.Namespace)
   => \".\"
 
   (path-separator clojure.lang.Keyword)
   => \"/\""
  {:added "3.0"}
  [:memoize {:arglists '([type])
             :function protocol.string/-path-separator}])

(defn wrap-op
  "wraps a string function such that it can take any string-like input
 
   ((wrap-op str false) :hello 'hello)
   => :hellohello
 
   ((wrap-op str true) :hello 'hello)
   => \"hellohello\""
  {:added "3.0"}
  [f return]
  (fn [input & args]
    (let [[isample iarr?] (if (sequential? input)
                            [(first input) true]
                            [input false])
          class   (type isample)]
      (binding [common/*sep* (path-separator class)]
        (let [interim (cond (= class String)
                            (apply f input args)
                            
                            iarr?
                            (apply f (map to-string input) args)
                            
                            :else
                            (apply f (to-string input) args))
              oarr?   (sequential? interim)
              output  (cond return
                            interim
                            
                            (= class String)
                            interim
                            
                            oarr?
                            (mapv #(from-string % class) interim)
                            
                            :else
                            (from-string interim class))]
          output)))))

(defn wrap-compare
  "wraps a function so that it can compare any two string-like inputs
 
   ((wrap-compare =) :hello 'hello)
   => true"
  {:added "3.0"}
  [f]
  (fn [x y & args]
    (binding [common/*sep* (path-separator (type x))]
      (apply f (to-string x) (to-string y) args))))

;;  ------------------
;;   java.lang.Object
;;  ------------------

(extend-type nil
  protocol.string/IString
  (-to-string [x] ""))

(extend-type Object
  protocol.string/IString
  (-to-string [x] (.toString x)))

(defmethod protocol.string/-from-string nil
  [_ _ _]
  nil)

(defmethod protocol.string/-path-separator :default
  [_]
  "/")

;;  ------------------
;;   java.lang.String
;;  ------------------

(extend-type String
  protocol.string/IString
  (-to-string [x] x))

(defmethod protocol.string/-from-string String
  [string _ _]
  string)

;;  ------------------
;;   byte[]
;;  ------------------

(extend-type (Class/forName "[B")
  protocol.string/IString
  (-to-string [^"[B" x] (String. x)))

(defmethod protocol.string/-from-string (Class/forName "[B")
  [string _ _]
  (.getBytes string))

;;  ------------------
;;   char[]
;;  ------------------

(extend-type (Class/forName "[C")
  protocol.string/IString
  (-to-string [^"[C" x] (String. x)))

(defmethod protocol.string/-from-string (Class/forName "[C")
  [string _ _]
  (.toCharArray string))

;;  ------------------
;;   java.lang.Class
;;  ------------------

(extend-type Class
  protocol.string/IString
  (-to-string [^Class x] (.getName x)))

(defmethod protocol.string/-from-string Class
  [string _ _]
  (Class/forName string))

(defmethod protocol.string/-path-separator Class
  [_]
  ".")

;;  ----------------------
;;   clojure.lang.Keyword
;;  ----------------------

(extend-type clojure.lang.Keyword
  protocol.string/IString
  (-to-string [x]
    (if (nil? x) "" (.replaceFirst (str x) ":" ""))))

(defmethod protocol.string/-from-string clojure.lang.Keyword
  [string _ _]
  (keyword string))

;;  ---------------------
;;   clojure.lang.Symbol
;;  ---------------------

(defmethod protocol.string/-from-string clojure.lang.Symbol
  [string _ _]
  (symbol string))

;;  ------------------------
;;   clojure.lang.Namespace
;;  ------------------------

(defmethod protocol.string/-from-string clojure.lang.Namespace
  [string _ _]
  (find-ns (symbol string)))

(defmethod protocol.string/-path-separator clojure.lang.Namespace
  [_]
  ".")
