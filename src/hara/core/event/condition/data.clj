(ns hara.core.event.condition.data
  (:require [hara.core.event.handler :as handler]))

(defn issue
  "creates a new issue
 
   (issue {:a 1} \"hello\" {} nil)
   => (contains {:id keyword?
                 :data {:a 1},
                 :msg \"hello\",
                 :options {},
                 :optmap {},
                 :default nil})"
  {:added "3.0"}
  ([data msg options default]
   (issue (handler/new-id) data msg options default))
  ([id data msg options default]
   (let [data    (handler/expand-data data)
         options (or options {})
         optmap  (zipmap (keys options) (repeat id))]
     {:id id
      :data data
      :msg msg
      :options options
      :optmap optmap
      :default default})))

(defn catch-condition
  "creates a catch condition
 
   (catch-condition :<TARGET> \"some value\")"
  {:added "3.0"}
  [target value]
  (ex-info "catch" {:event/condition :catch :target target :value value}))

(defn choose-condition
  "creates a choose-condition
 
   (choose-condition :<TARGET> :choice-A [1])"
  {:added "3.0"}
  [target label args]
  (ex-info "choose" {:event/condition :choose :target target :label label :args args}))

(defn exception
  "creates an exception
 
   (exception (issue {:a 1} \"hello\" {} nil))"
  {:added "3.0"}
  ([issue]
   (let [contents (:data issue)
         msg    (str (:msg issue) " - " contents)
         error  ^Throwable (ex-info msg contents)]
     (doto error
       (.setStackTrace (->> (seq (.getStackTrace error))
                            (filter (fn [^StackTraceElement name]
                                      (-> (.getClassName name)
                                          (.startsWith "hara.core.event")
                                          (not))))
                            (into-array StackTraceElement))))))
  ([issue data]
   (exception (update-in issue [:data] merge data))))
