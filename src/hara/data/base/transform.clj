(ns hara.data.base.transform)

(defn template?
  "checks if an object is a template
 
   (template? \"{{template}}\")
   => true
 
   (template? :not-one)
   => false"
  {:added "3.0"}
  [s]
  (and (string? s)
       (.startsWith s "{{")
       (.endsWith s "}}")))

(defn find-templates
  "finds the template with associated path
 
   (find-templates {:hash  \"{{hash}}\"
                    :salt  \"{{salt}}\"
                    :email \"{{email}}\"
                    :user {:firstname \"{{firstname}}\"
                           :lastname  \"{{lastname}}\"}})
   => {\"{{hash}}\" [:hash]
       \"{{salt}}\" [:salt]
       \"{{email}}\" [:email]
      \"{{firstname}}\" [:user :firstname]
       \"{{lastname}}\" [:user :lastname]}"
  {:added "3.0"}
  ([m]
   (find-templates m [] {}))
  ([m path saved]
   (reduce-kv (fn [out k v]
                (cond (template? v)
                      (assoc out v (conj path k))

                      (map? v)
                      (find-templates v (conj path k) out)

                      :else
                      out))
              saved
              m)))

(defn transform-fn
  "creates a transformation function
   ((transform-fn {:keystore {:hash  \"{{hash}}\"
                              :salt  \"{{salt}}\"
                              :email \"{{email}}\"}
 
                   :db       {:login {:type :email
                                      :user {:hash \"{{hash}}\"
                                             :salt \"{{salt}}\"}
                                      :value \"{{email}}\"}}}
                 [:keystore :db])
    {:hash \"1234\"
     :salt \"ABCD\"
     :email \"a@a.com\"})
   => {:login {:type :email,
               :user {:hash \"1234\",
                      :salt \"ABCD\"},
               :value \"a@a.com\"}}"
  {:added "3.0"}
  [schema [from to]]
  (let [from-template (find-templates (get schema from))
        to-template   (find-templates (get schema to))]
    (fn [data]
      (reduce (fn [out k]
                (assoc-in out
                          (get to-template k)
                          (->> (get from-template k)
                               (get-in data))))
              (get schema to)
              (keys to-template)))))

(def transform-fn* (memoize transform-fn))

(defn transform
  "creates a transformation function
   (transform {:keystore {:hash  \"{{hash}}\"
                          :salt  \"{{salt}}\"
                          :email \"{{email}}\"}
 
               :db       {:login {:type :email
                                  :user {:hash \"{{hash}}\"
                                         :salt \"{{salt}}\"}
                                  :value \"{{email}}\"}}}
             [:keystore :db]
              {:hash \"1234\"
               :salt \"ABCD\"
               :email \"a@a.com\"})
   => {:login {:type :email,
               :user {:hash \"1234\",
                      :salt \"ABCD\"},
               :value \"a@a.com\"}}"
  {:added "3.0"}
  [schema [from to] data]
  (let [f (transform-fn* schema [from to])]
    (f data)))
