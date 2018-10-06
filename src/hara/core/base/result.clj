(ns hara.core.base.result)

(defrecord Result [status data]
  Object
  (toString [res]
    (str "#result" (if status
                     (str "." (name status)))
         (into {} (dissoc res :status)))))

(defmethod print-method Result
  [v w]
  (.write w (str v)))

(defn result
  "creates a result used for printing
 
   (result {:status :warn :data [1 2 3 4]})
   ;; #result{:status :warn, :data [1 2 3 4]}
   => hara.core.base.result.Result"
  {:added "3.0"}
  [m]
  (map->Result m))

(defn result?
  "checks if an object is a result
 
   (-> (result {:status :warn :data [1 2 3 4]})
       result?)
   => true"
  {:added "3.0"}
  [obj]
  (instance? Result obj))

(defn ->result
  "converts data into a result
   
   (->result :hello [1 2 3])
   ;;#result.return{:data [1 2 3], :key :hello}
   => hara.core.base.result.Result"
  {:added "3.0"}
  [key data]
  (cond (result? data)
        (assoc data :key key)

        :else
        (result {:key key :status :return :data data})))

