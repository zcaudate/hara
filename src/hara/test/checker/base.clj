(ns hara.test.checker.base
  (:require [hara.core.base.check :as check]
            [hara.core.base.util :as primitive]
            [hara.test.common :as common])
  (:import (hara.test.common Checker Result)
           (java.util.regex Pattern)))

(defn verify
  "verifies a value with it's associated check
 
   (verify (satisfies 2) 1)
   => (contains-in {:type :success
                    :data false
                    :checker {:tag :satisfies
                              :doc string?
                              :expect 2}
                    :actual 1
                    :from :verify})
 
   (verify (->checker #(/ % 0)) 1)
   => (contains {:type :exception
                 :data java.lang.ArithmeticException
                 :from :verify})"
  {:added "3.0"}
  [ck result]
  (let [out (try
              {:type :success :data (ck result)}
              (catch Throwable t
                {:type :exception :data t}))]
    (common/result (assoc out :checker ck :actual result :from :verify))))

(defn succeeded?
  "determines if the results of a check have succeeded
 
   (-> (satisfies Long)
       (verify 1)
       succeeded?)
   => true
 
   (-> (satisfies even?)
       (verify 1)
       succeeded?)
   => false"
  {:added "3.0"}
  [{:keys [type data]}]
  (and (= :success type)
       (= true data)))

(defn throws
  "checker that determines if an exception has been thrown
 
   ((throws Exception \"Hello There\")
    (common/map->Result
     {:type :exception
      :data (Exception. \"Hello There\")}))
   => true"
  {:added "3.0"}
  ([]  (throws Throwable))
  ([e] (throws e nil))
  ([e msg]
   (common/checker
    {:tag :throws
     :doc "Checks if an exception has been thrown"
     :fn (fn [{:keys [^Throwable data type]}]
           (and (= :exception type)
                (instance? e data)
                (if msg
                  (= msg (.getMessage data))
                  true)))
     :expect {:exception e :message msg}})))

(defn exactly
  "checker that allows exact verifications
 
   ((exactly 1) 1) => true
 
   ((exactly Long) 1) => false
 
   ((exactly number?) 1) => false"
  {:added "3.0"}
  ([v]
   (exactly v identity))
  ([v function]
   (common/checker
    {:tag :exactly
     :doc "Checks if the result exactly satisfies the condition"
     :fn (fn [res] (= (function (common/->data res)) v))
     :expect v})))

(defn approx
  "checker that allows approximate verifications
 
   ((approx 1) 1.000001) => true
 
   ((approx 1) 1.1) => false
 
   ((approx 1 0.0000001) 1.001) => false"
  {:added "3.0"}
  ([v]
   (approx v 0.001))
  ([v threshold]
   (common/checker
    {:tag :approx
     :doc "Checks if the result is approximately the given value"
     :fn (fn [res] (< (- v threshold) (common/->data res) (+ v threshold)))
     :expect v})))

(defn satisfies
  "checker that allows loose verifications
 
   ((satisfies 1) 1) => true
 
   ((satisfies Long) 1) => true
 
   ((satisfies number?) 1) => true
 
   ((satisfies #{1 2 3}) 1) => true
 
   ((satisfies [1 2 3]) 1) => false
 
   ((satisfies number?) \"e\") => false
 
   ((satisfies #\"hello\") #\"hello\") => true"
  {:added "3.0"}
  ([v]
   (satisfies v identity))
  ([v function]
   (common/checker
    {:tag :satisfies
     :doc "Checks if the result can satisfy the condition:"
     :fn (fn [res]
           (let [data (function (common/->data res))]
             (cond (= data v) true
                   
                   (class? v) (instance? v data)

                   (and (check/comparable? v data)
                        (zero? (compare v data)))
                   true
                   
                   (map? v) (= (into {} data) v)

                   (vector? v) (= data v)

                   (ifn? v) (boolean (v data))
                   
                   (check/regexp? v)
                   (cond (check/regexp? data)
                         (= (.pattern ^Pattern v)
                            (.pattern ^Pattern data))

                         (string? data)
                         (boolean (re-find v data))

                         :else false)
                   
                   :else false)))
     :expect v})))

(defn anything
  "a checker that returns true for any value
 
   (anything nil) => true
 
   (anything [:hello :world]) => true"
  {:added "3.0"}
  [x]
  ((satisfies primitive/T) x))

(defn ->checker
  "creates a 'satisfies' checker if not already a checker
 
   ((->checker 1) 1) => true
 
   ((->checker (exactly 1)) 1) => true"
  {:added "3.0"}
  [x]
  (if (instance? Checker x)
    x
    (satisfies x)))
