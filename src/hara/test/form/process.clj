(ns hara.test.form.process
  (:require [hara.core.event :as event]
            [hara.test.checker.base :as checker]
            [hara.test.common :as common]))

(defmulti process
  "processes a form or a check
   (defn view-signal [op]
     (let [output (atom nil)]
       (event/with-temp-listener [:test
                                  (fn [{:keys [result]}]
                                    (reset! output (into {} result)))]
         (process op)
         @output)))
 
   (view-signal (common/op {:type :form
                            :form '(+ 1 2 3)
                            :meta {:line 10 :col 3}}))
   => {:type :success,
       :data 6,
       :form '(+ 1 2 3),
       :from :evaluate,
       :meta {:line 10, :col 3}}
 
   ((contains {:type :success,
               :data true,
               :checker common/checker?
               :actual 6,
               :from :verify,
               :meta nil})
    (view-signal (common/op {:type :test-equal
                            :input  '(+ 1 2 3)
                             :output 'even?})))
   => true"
  {:added "3.0"}
  :type)

(defmethod process :form
  [{:keys [form meta] :as op}]
  (let [result (assoc (common/evaluate form) :meta meta)]
    (event/signal {:test :form :result result})
    result))

(defmethod process :test-equal
  [{:keys [input output meta] :as op}]
  (let [actual   (common/evaluate input)
        expected (common/evaluate output)
        checker  (assoc (checker/->checker (common/->data expected))
                        :form (:form expected))
        result   (assoc (checker/verify checker actual) :meta meta)]
    (event/signal {:test :check :result result})
    result))
