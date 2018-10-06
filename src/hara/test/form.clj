(ns hara.test.form
  (:require [hara.core.base.event :as event]
            [hara.test.common :as common]
            [hara.test.form.match :as match]
            [hara.test.form.process :as process]))

(def => '=>)

(def arrows '{=> :test-equal})

(defn split
  "creates a sequence of pairs from a loose sequence
   (split '[(def a 1)
            (+ a 3)
            => 5])
   => (contains-in [{:type :form,
                     :form '(def a 1)}
                    {:type :test-equal,
                     :input '(+ a 3),
                     :output 5}])"
  {:added "3.0"}
  ([body] (split body []))
  ([[x y z & more :as arr] out]
   (cond (empty? arr)
         out

         (get arrows y)
         (recur more
                (conj out {:type (get arrows y)
                           :meta (merge common/*meta*
                                        (or (meta x) (meta y) (meta z)))
                           :input x
                           :output z}))

         :else
         (recur (rest arr)
                (conj out {:type :form
                           :meta (merge common/*meta* (meta x))
                           :form x})))))

(defn collect
  "makes sure that all returned verified results are true
   (->> (split '[(+ 1 1) => 2
                 (+ 1 2) => 3])
        (mapv process/process)
        (collect {}))
   => true"
  {:added "3.0"}
  [meta results]
  (event/signal {:id common/*id* :test :fact :meta meta :results results})
  (and (->> results
            (filter #(-> % :from (= :verify)))
            (mapv :data)
            (every? true?))
       (->> results
            (filter #(and (-> % :from (= :evaluate))
                          (-> % :type (= :exception))))
            (empty?))))

(defn skip
  "returns the form with no ops evaluated"
  {:added "3.0"}
  [meta]
  (event/signal {:id common/*id* :test :fact :meta meta :results [] :skipped true})
  :skipped)

(defmacro fact
  "top level macro for test definitions"
  {:added "3.0"}
  ([& [desc? & body]]
   (let [[desc body] (if (string? desc?)
                       [desc? body]
                       [nil (cons desc? body)])
         fmeta  (assoc (meta &form) :path common/*path* :desc desc :ns (.getName *ns*))
         body   (binding [common/*meta* fmeta] (split body))]
     `(binding [common/*meta* ~(list `quote fmeta)]
        (if (or (match/match-options common/*meta* common/*settings*)
                (not common/*id*))
          (->> (mapv process/process ~(list `quote body))
               (collect common/*meta*))
          (skip common/*meta*))))))

(defmacro facts
  "top level macro for test definitions"
  {:added "3.0"}
  [& more]
  `(fact ~@more))
