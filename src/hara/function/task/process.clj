(ns hara.function.task.process
  (:require [clojure.set :as set]
            [hara.function :as fn]
            [hara.core.base.result :as result]
            [hara.data.base.nested :as nested]
            [hara.data.base.seq :as seq]
            [hara.function.task.bulk :as bulk]))

(defn main-function
  "creates a main function to be used for execution
 
   (main-function ns-aliases 1)
   => (contains [arg/vargs? false])
 
   (main-function ns-unmap 1)
   => (contains [arg/vargs? true])"
  {:added "3.0"}
  [func count]
  (let [fcounts (fn/arg-count func)
        fcount  (if-not (empty? fcounts)
                  (apply min fcounts)
                  4)
        args?   (> fcount count)
        main    (cond (= count 4) (fn [input params lookup env & args]
                                    (apply func input params lookup env args))
                      (= count 3) (fn [input params _ env & args]
                                    (apply func input params env args))
                      (= count 2) (fn [input params _ _ & args]
                                    (apply func input params args))
                      (= count 1) (fn [input _ _ _ & args]
                                    (apply func input args))
                      :else (throw (ex-info "`count` is a value between 1 to 4" {:count count})))]
    [main args?]))

(defn select-match
  "returns true if selector matches with input
 
   (select-match 'hara 'hara.test) => true
 
   (select-match 'hara 'spirit.common) => false"
  {:added "3.0"}
  [sel input]
  (or (.startsWith (str input) (str sel))))

(defn select-inputs
  "selects inputs based on matches
 
   (select-inputs {:item {:list (fn [_ _] ['hara.test 'spirit.common])}}
                  {}
                  {}
                  ['hara])
   => ['hara.test]"
  {:added "3.0"}
  [task lookup env input]
  (let [list-fn    (or (-> task :item :list)
                       (throw (ex-info "No `:list` function defined" {:key [:item :list]})))
        candidates (cond (= input :all)
                         (list-fn lookup env)

                         (set? input) input

                         (vector? input)
                         (let [all (list-fn lookup env)
                               sel-fn (fn [sel]
                                        (->> all
                                             (filter #(select-match sel %))
                                             set))]
                           (->> (map sel-fn input)
                                (apply set/union)
                                (sort)))

                         :else
                         (throw (ex-info "No `:list` function defined" {:key [:item :list]})))]
    candidates))

(defn wrap-execute
  "enables execution of task with transformations"
  {:added "3.0"}
  [f task]
  (fn [input params lookup env & args]
    (let [pre-fn    (or (-> task :item :pre) identity)
          post-fn   (or (-> task :item :post) identity)
          output-fn (or (-> task :item :output) identity)
          input  (pre-fn input)
          result (apply f input params lookup env args)
          result (post-fn result)]
      (if (:bulk params)
        [input (result/->result input result)]
        (output-fn result)))))

(defn wrap-input
  "enables execution of task with single or multiple inputs"
  {:added "3.0"}
  [f task]
  (fn [input params lookup env & args]
    (cond (= :list input)
          (let [list-fn  (or (-> task :item :list)
                             (throw (ex-info "No `:list` function defined" {:key [:item :list]})))]
            (list-fn lookup env))

          (or (keyword? input)
              (vector? input)
              (set? input))
          (let [inputs (select-inputs task lookup env input)]
            (apply bulk/bulk task f inputs params lookup env args))

          :else
          (apply f input params lookup env args))))

(defn task-inputs
  "constructs inputs to the task given a set of parameters
 
   (task-inputs (task/task :namespace \"ns-interns\" ns-interns)
                'hara.function.task)
   => '[hara.function.task {} {} {}]
 
   (task-inputs (task/task :namespace \"ns-interns\" ns-interns)
                {:bulk true})
   => '[hara.function.task.process-test {:bulk true} {} {}]"
  {:added "3.0"}
  ([task]
   (let [input-fn (-> task :construct :input)]
     (task-inputs task (input-fn task) task)))
  ([task input]
   (let [input-fn (-> task :construct :input)
         [input params] (cond (map? input)
                              [(input-fn task) input]

                              :else [input {}])]
     (task-inputs task input params)))
  ([task input params]
   (let [env-fn (-> task :construct :env)]
     (task-inputs task input params (env-fn task))))
  ([task input params env]
   (let [lookup-fn (-> task :construct :lookup)]
     (task-inputs task input params (lookup-fn task env) env)))
  ([task input params lookup env]
   [input params lookup env]))

(defn invoke
  "executes the task, given functions and parameters
 
   (def world nil)
   
   (invoke (task/task :namespace \"ns-interns\" ns-interns))
   => {'world #'hara.function.task.process-test/world}"
  {:added "3.0"}
  [task & args]
  (let [idx (seq/index-of #(= % :args) args)
        _   (if (and (neg? idx) (-> task :main :args?))
              (throw (ex-info "Require `:args` keyword to specify additional arguments"
                              {:input args})))
        [task-args func-args] (if (neg? idx)
                                [args []]
                                [(take idx args) (drop (inc idx) args)])
        [input params lookup env] (apply task-inputs task task-args)
        f  (-> (-> task :main :fn)
               (wrap-execute task)
               (wrap-input task))
        params (nested/merge-nested (:params task) params)]
    (apply f input params lookup env func-args)))
