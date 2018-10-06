(ns hara.core.base.event.handler
  (:require [hara.core.base.check :as check]
            [hara.core.base.util :as primitive]
            [hara.data.base.seq :as seq]))

(defn new-id
  "creates a random id with a keyword base
   (new-id)
   ;;=> :06679506-1f87-4be8-8cfb-c48f8579bc00
 "
  {:added "3.0"}
  []
  (keyword (str (primitive/uuid))))

(defn expand-data
  "expands shorthand data into a map
 
   (expand-data :hello)
   => {:hello true}
 
   (expand-data [:hello {:world \"foo\"}])
   => {:world \"foo\", :hello true}"
  {:added "3.0"}
  [data]
  (cond (check/hash-map? data) data
        (keyword? data) {data true}
        (vector? data)  (apply merge (map expand-data data))
        :else (throw (ex-info "Input should be a keyword, hash-map or vector." {:input data}))))

(defn check-data
  "checks to see if the data corresponds to a template
 
   (check-data {:hello true} :hello)
   => true
 
   (check-data {:hello true} {:hello true?})
   => true
 
   (check-data {:hello true} '_)
   => true
 
   (check-data {:hello true} #{:hello})
   => true"
  {:added "3.0"}
  [data chk]
  (cond (check/hash-map? chk)
        (every? (fn [[k vchk]]
                  (let [vcnt (get data k)]
                    (cond (keyword? vchk) (= vchk vcnt)
                          (fn? vchk) (vchk vcnt)
                          :else (= vchk vcnt))))
                chk)

        (vector? chk)
        (every? #(check-data data %) chk)

        (or (fn? chk) (keyword? chk))
        (chk data)

        (set? chk)
        (some #(check-data data %) chk)

        (= '_ chk) true

        :else
        (throw (ex-info "Not a valid checker" {:checker chk}))))

(defrecord Manager [id store options])

(defn manager
  "creates a new manager
   (manager)
   ;; => #hara.core.base.event.handler.Manager{:id :b56eb2c9-8d21-4680-b3e1-0023ae685d2b,
   ;;                               :store [], :options {}}
 "
  {:added "3.0"}
  ([] (Manager. (new-id) [] {}))
  ([id store options] (Manager. id store options)))

(defn remove-handler
  "adds a handler to the manager
   (-> (add-handler (manager) :hello {:id :hello
                                      :handler identity})
       (remove-handler :hello)
       (match-handlers {:hello \"world\"}))
   => ()"
  {:added "3.0"}
  [manager id]
  (if-let [position (first (seq/positions #(-> % :id (= id)) (:store manager)))]
    (update-in manager [:store] seq/remove-index position)
    manager))

(defn add-handler
  "adds a handler to the manager
   (-> (add-handler (manager) :hello {:id :hello
                                      :handler identity})
       (match-handlers {:hello \"world\"})
       (count))
   => 1"
  {:added "3.0"}
  ([manager handler]
   (let [handler (if (:id handler)
                   handler
                   (assoc handler :id (new-id)))]
     (-> manager
         (remove-handler (:id handler))
         (update-in [:store] conj handler))))
  ([manager checker handler]
   (let [handler (cond (fn? handler)
                       {:checker checker
                        :fn handler}

                       (map? handler)
                       (assoc handler :checker checker))]
     (add-handler manager handler))))

(defn list-handlers
  "list handlers that are present for a given manager
 
   (list-handlers (manager))
   => []"
  {:added "3.0"}
  ([manager]
   (:store manager))
  ([manager checker]
   (->> (list-handlers manager)
        (filter #(check-data (:checker %) checker)))))

(defn match-handlers
  "match handlers for a given manager
 
   (-> (add-handler (manager) :hello {:id :hello
                                      :handler identity})
       (match-handlers {:hello \"world\"}))
   => (contains-in [{:id :hello
                     :handler fn?
                     :checker :hello}])"
  {:added "3.0"}
  [manager data]
  (filter #(check-data data (:checker %))
          (:store manager)))
