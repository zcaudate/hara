(ns hara.object.framework.map-like
  (:require [hara.object.framework.access :as access]
            [hara.object.framework.print :as print]
            [hara.object.framework.read :as read]
            [hara.object.framework.write :as write]
            [hara.function :as fn]
            [hara.protocol.object :as protocol.object]))

(defn key-selection
  "selects map based on keys
 
   (key-selection {:a 1 :b 2} [:a] nil)
   => {:a 1}
 
   (key-selection {:a 1 :b 2} nil [:a])
   => {:b 2}"
  {:added "3.0"}
  [m include exclude]
  (cond-> m
    include (select-keys include)
    exclude (#(apply dissoc % exclude))))

(defn read-proxy-functions
  "creates a proxy access through a field in the object
 
   (read-proxy-functions {:school [:name :raw]})
   => '{:name (clojure.core/fn [obj]
                (clojure.core/let [proxy (hara.object.framework.access/get obj :school)]
                  (hara.object.framework.access/get proxy :name))),
        :raw (clojure.core/fn [obj]
               (clojure.core/let [proxy (hara.object.framework.access/get obj :school)]
                 (hara.object.framework.access/get proxy :raw)))}"
  {:added "3.0"}
  [proxy]
  (reduce-kv (fn [out accessor ks]
               (reduce (fn [out k]
                         (assoc out k `(fn [~'obj]
                                         (let [~'proxy (access/get ~'obj ~accessor)]
                                           (access/get ~'proxy ~k)))))
                       out
                       ks))
             {}
             proxy))

(defn write-proxy-functions
  "creates a proxy access through a field in the object
 
   (write-proxy-functions {:school [:name :raw]})
   => '{:name (clojure.core/fn [obj v]
                (clojure.core/let [proxy (hara.object.framework.access/get obj :school)]
                  (hara.object.framework.access/set proxy :name v))),
        :raw (clojure.core/fn [obj v]
               (clojure.core/let [proxy (hara.object.framework.access/get obj :school)]
                 (hara.object.framework.access/set proxy :raw v)))}"
  {:added "3.0"}
  [proxy]
  (reduce-kv (fn [out accessor ks]
               (reduce (fn [out k]
                         (assoc out k `(fn [~'obj ~'v]
                                         (let [~'proxy (access/get ~'obj ~accessor)]
                                           (access/set ~'proxy ~k ~'v)))))
                       out
                       ks))
             {}
             proxy))

(defmacro extend-map-like
  "creates an entry for map-like classes
 
   (extend-map-like test.DogBuilder
                    {:tag \"build.dog\"
                     :write {:empty (fn [] (test.DogBuilder.))}
                     :read :fields})
 
   (extend-map-like test.Dog {:tag \"dog\"
                              :write  {:methods :fields
                                       :from-map (fn [m] (-> m
                                                             (write/from-map test.DogBuilder)
                                                             (.build)))}
                              :exclude [:species]})
   
   (with-out-str
     (prn (write/from-data {:name \"hello\"} test.Dog)))
   => \"#dog{:name \\\"hello\\\"}\\n\"
 
   (extend-map-like test.Cat {:tag \"cat\"
                              :write  {:from-map (fn [m] (test.Cat. (:name m)))}
                              :exclude [:species]})
   
   (extend-map-like test.Pet {:tag \"pet\"
                              :write {:from-map (fn [m] (case (:species m)
                                                          \"dog\" (write/from-map m test.Dog)
                                                          \"cat\" (write/from-map m test.Cat)))}})
   
   (with-out-str
     (prn (write/from-data {:name \"hello\" :species \"cat\"} test.Pet)))
   => \"#cat{:name \\\"hello\\\"}\\n\""
  {:added "3.0"}
  [^Class cls {:keys [read write exclude include proxy] :as opts}]
  `[(defmethod protocol.object/-meta-read ~cls
      [~'_]
      ~(let [methods (:methods read)
             read (cond (and (map? read) (not (keyword? methods)))
                        (update-in read [:methods]
                                   #(list 'merge %
                                          `(-> (merge (read/read-all-getters ~cls read/+read-get-opts+)
                                                      (read/read-all-getters ~cls read/+read-is-opts+))
                                               (key-selection (or ~include []) ~exclude))))

                        (or (= read :fields)
                            (= methods :fields))
                        `{:methods (key-selection (read/read-fields ~cls) ~include ~exclude)}

                        (or (= read :all-fields)
                            (= methods :all-fields))
                        `{:methods (key-selection (read/read-all-fields ~cls) ~include ~exclude)}
                        
                        (or (= read :all)
                            (= methods :all))
                        `{:methods (-> (merge (read/read-all-getters ~cls read/+read-get-opts+)
                                              (read/read-all-getters ~cls read/+read-is-opts+))
                                       (key-selection ~include ~exclude))}

                        (or (nil? read)
                            (= read :class)
                            (= methods :class))
                        `{:methods (-> (merge (read/read-getters ~cls read/+read-get-opts+)
                                              (read/read-getters ~cls read/+read-is-opts+))
                                       (key-selection ~include ~exclude))})
             read (update-in read [:methods] #(list 'merge % (read-proxy-functions proxy)))]
         (print/assoc-print-vars read opts)))

    ~(when (and write (map? write))
       (assert (or (:from-map write)
                   (:empty write)
                   (:construct write))
               "The :write entry requires a sub-entry for either :from-map, :construct or :empty ")
       (let [methods (:methods write)]
         `(defmethod protocol.object/-meta-write ~cls
            [~'_]
            ~(cond-> write
               (= methods :fields)
               (assoc :methods `(write/write-fields ~cls))

               (= methods :all-fields)
               (assoc :methods `(write/write-all-fields ~cls))

               (= methods :all)
               (assoc :methods `(write/write-all-setters ~cls))

               (or (= methods :class)
                   (nil? methods))
               (assoc :methods `(write/write-setters ~cls))

               :then
               (update-in [:methods] #(list 'merge % (write-proxy-functions proxy)))))))

    (do (fn/memoize-remove read/meta-read ~cls)
        (fn/memoize-remove write/meta-write ~cls)
        (print/extend-print ~cls))])

