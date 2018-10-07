(ns hara.event.condition.manage)

(defn manage-apply
  "helper function to manage-condition"
  {:added "3.0"}
  [f args label]
  (try
    (apply f args)
    (catch clojure.lang.ArityException e
      (throw (ex-info "MANAGE-APPLY: Wrong number of arguments to option key: " {:label label})))))

(defn manage-condition
  "allows conditionals to be run with :choose and :catch options"
  {:added "3.0"}
  [manager ex]
  (let [data (ex-data ex)]
    (cond (not= (:id manager) (:target data))
          (throw ex)

          (= :choose   (:event/condition data))
          (let [label  (:label data)
                f      (get (:options manager) label)
                args   (:args data)]
            (manage-apply f args label))

          (= :catch (:event/condition data))
          (:value data)

          :else (throw ex))))
