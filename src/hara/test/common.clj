(ns hara.test.common
  (:require [clojure.main :as main]
            [hara.string :as string]))

(defonce ^:dynamic *settings* {:test-paths ["test"]})

(defonce ^:dynamic *meta* nil)

(defonce ^:dynamic *desc* nil)

(defonce ^:dynamic *path* nil)

(defonce ^:dynamic *id* nil)

(defonce ^:dynamic *accumulator* (atom nil))

(defonce ^:dynamic *errors* nil)

(defonce ^:dynamic *print* #{:print-thrown :print-failure :print-bulk})

(defonce ^:dynamic *test-suffix* "-test")

(defonce ^:dynamic *root* nil)

(defrecord Op []
  Object
  (toString [op]
    (str "#op." (name (:type op)) (dissoc (into {} op) :type))))

(defmethod print-method Op
  [v ^java.io.Writer w]
  (.write w (str v)))

(defn op
  "creates an 'op' for evaluation
 
   (op {:type :form :form '(+ 1 1)})
   => hara.test.common.Op"
  {:added "3.0"}
  [m]
  (map->Op m))

(defn op?
  "checks to see if a datastructure is an 'Op'
 
   (op? (op {:type :form :form '(+ 1 1)}))
   => true"
  {:added "3.0"}
  [x]
  (instance? Op x))

(defrecord Result []
  Object
  (toString [res]
    (str "#result." (name (:type res)) (dissoc (into {} res) :type))))

(defmethod print-method Result
  [v ^java.io.Writer w]
  (.write w (str v)))

(defn result
  "creates a 'hara.test.common.Result' object
 
   (result {:type :success :data true})
   => hara.test.common.Result"
  {:added "3.0"}
  [m]
  (map->Result m))

(defn result?
  "checks to see if a datastructure is a 'hara.test.common.Result'
 
   (result? (result {:type :success :data true}))
   => true"
  {:added "3.0"}
  [x]
  (instance? Result x))

(defn ->data
  "coerces a checker result into data
 
   (->data 1) => 1
 
   (->data (result {:data 1}))
   => 1"
  {:added "3.0"}
  [res]
  (if (result? res) (:data res) res))

(defn function-string
  "returns the string representation of a function
 
   (function-string every?) => \"every?\"
 
   (function-string reset!) => \"reset!\""
  {:added "3.0"}
  [func]
  (-> (type func)
      str
      (string/split #"\$")
      last
      main/demunge))

(defrecord Checker [fn]
  Object
  (toString [{:keys [expect tag]}]
    (str "#" (name tag) (cond (coll? expect)
                              expect

                              (fn? expect)
                              (str "<" (function-string expect) ">")

                              :else
                              (str "<" expect ">"))))

  clojure.lang.IFn
  (invoke [ck data] (let [func (:fn ck)] (func data))))

(defmethod print-method Checker
  [v ^java.io.Writer w]
  (.write w (str v)))

(defn checker
  "creates a 'hara.test.common.Checker' object
 
   (checker {:tag :anything :fn (fn [x] true)})
   => hara.test.common.Checker"
  {:added "3.0"}
  [m]
  (map->Checker m))

(defn checker?
  "checks to see if a datastructure is a 'hara.test.common.Checker'
 
   (checker? (checker {:tag :anything :fn (fn [x] true)}))
   => true"
  {:added "3.0"}
  [x]
  (instance? Checker x))

(defn evaluate
  "converts a form to a result
 
   (->> (evaluate '(+ 1 2 3))
        (into {}))
   => {:type :success, :data 6, :form '(+ 1 2 3), :from :evaluate}"
  {:added "3.0"}
  [form]
  (let [out (try
              {:type :success :data (eval form)}
              (catch Throwable t
                {:type :exception :data t}))]
    (result (assoc out :form form :from :evaluate))))
