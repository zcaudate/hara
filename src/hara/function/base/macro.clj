(ns hara.function.base.macro
  (:refer-clojure :exclude [deftype defrecord]))

(defn create-args
  "caches the result of a function
 
   (create-args '[[x] (inc x) nil nil])
   => '(\"\" {} [x] (inc x))"
  {:added "3.0"}
  ([[doc? attr? & more :as arglist]]
   (let [[doc attr? more] (if (string? doc?)
                            [doc? attr? more]
                            ["" doc? (cons attr? more)])
         [attr more] (if (map? attr?)
                       [attr? more]
                       [{} (cons attr? more)])]
     (->> more
          (cons attr)
          (cons doc)
          (keep identity)))))

(defn create-def-form
  "removes a cached result
   
   (create-def-form 'hello \"doc\" {:added \"1.3\"} '[x] '(inc x))
   '(do (def hello (inc x))
        (clojure.core/doto (var hello)
          (clojure.core/alter-meta! clojure.core/merge
                                    {:added \"1.3\"}
                                    {:arglists (quote ([x])), :doc \"doc\"})))"
  {:added "3.0"}
  ([name attrs body]
   `(do (def ~name ~body)
          (doto (var ~name)
            (alter-meta! merge ~attrs))))
  ([name doc attrs arglist body]
   (let [arglists (cond (nil? arglist)
                        nil
                        
                        (vector? arglist)
                        `(quote ~(list arglist))

                        :else
                        `(quote ~arglist))]
     (create-def-form name
                      (merge attrs
                             {:doc doc}
                             (if arglists {:arglists arglists}))
                      body))))

(defmacro defcompose
  "used instead of `def` for functional composition
 
   (defcompose -add-10-
     [x & more]
     (partial + 10))
 
   (-add-10- 10) => 20"
  {:added "3.0"}
  [name doc? attrs? & [arglist body]]
  (->> (create-args [doc? attrs? arglist body])
       (apply create-def-form name)))

(defn lookup
  "creates a lookup function based on a map lookup
 
   (def -opts-
     {:in  (fn [s] (-> s (.toLowerCase) keyword))
      :out name
      :not-found :no-reference})
   
   (def -lookup-
     (lookup {:kunming :china
              :melbourne :australia}
             -opts-))
   
   (-lookup- \"MeLBoURne\") => \"australia\""
  {:added "3.0"}
  ([m] m)
  ([m {:keys [in out not-found] :as transfer}]
   (cond (not (or in out not-found))
         m
         
         :else
         (let [in  (or in identity)
               out (or out identity)]
           (fn [input]
             (out (get m (in input) not-found)))))))

(defmacro deflookup
  "defines a map based lookup
 
   (deflookup -country-
     [city]
     {:kunming :china
      :melbourne :australia})
 
   (-country- :kunming) => :china"
  {:added "3.0"}
  [name doc? attrs? & [arglist lookup transfer?]]
  (let [[doc attrs arglist & lookup-body]
        (create-args [doc? attrs? arglist lookup transfer?])
        
        body `(lookup ~@lookup-body)]
    (create-def-form name doc attrs arglist body)))
