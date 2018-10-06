(ns hara.object.handle
  (:import (java.lang.invoke MethodHandles
                             MethodHandles$Lookup
                             MethodType
                             MethodHandle)))


(defonce +public-lookup+ (MethodHandles/publicLookup))

(comment

  (import (java.lang.invoke MethodHandles
                            MethodHandles$Lookup
                            MethodType
                            MethodHandle))
  
  (defonce +lookup+ (MethodHandles/lookup))
  
  
  
  
  
  (.invokeWithArguments concat-handle (into-array Object ["hello" "there"]))

  (def concat-reflect (.? String "concat" :#)) 
 
  (with-out-str
    (time (dotimes [i 1000000]
            (.concat "hello" "there"))))
  => "\"Elapsed time: 8.542214 msecs\"\n"

  
  (with-out-str
    (def concat-fn (fn [a b] (.concat a b)))
    (time (dotimes [i 1000000]
            (concat-fn "hello" "there"))))
  => "\"Elapsed time: 3600.357352 msecs\"\n"

  (with-out-str
    (def concat-anno (fn [^String a b] (.concat a b)))
    (time (dotimes [i 1000000]
            (concat-anno "hello" "there"))))
  => "\"Elapsed time: 16.461237 msecs\"\n"

  (with-out-str
    (def concat-reflect (.? String "concat" :#))
    (time (dotimes [i 1000000]
            (concat-reflect "hello" "there"))))
  => "\"Elapsed time: 1804.522226 msecs\"\n"

  (with-out-str
    (def ^MethodHandle concat-handle
      (.findVirtual +lookup+
                    String
                    "concat"
                    (MethodType/methodType String String)))
    (time (dotimes [i 1000000]
            (.invokeWithArguments concat-handle (into-array Object ["hello" "there"])))))
  => "\"Elapsed time: 1974.824815 msecs\"\n" 

  (with-out-str
    (def ^MethodHandle concat-spread
      (.asSpreader concat-handle
                   (Class/forName "[Ljava.lang.String;") ;; String[].class
                   2))
    (time (dotimes [i 1000000]
            (.invoke other-handle (into-array String ["hello" "there"])))))
  => "\"Elapsed time: 399.779913 msecs\"\n"

  )
