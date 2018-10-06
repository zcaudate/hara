(ns hara.test
  (:require [hara.string :as string]
            [hara.module :as module]
            [hara.core.base.result :as result]
            [hara.io.project :as project]
            [hara.function :refer [definvoke]]
            [hara.function.task :as task]
            [hara.test.checker.base]
            [hara.test.checker.collection]
            [hara.test.checker.logic]
            [hara.test.common :as common]
            [hara.test.executive :as executive]
            [hara.test.form]
            [clojure.set :as set]))

(module/include (hara.test.checker.base
                 throws exactly approx satisfies anything)

                (hara.test.checker.collection
                 contains just contains-in just-in throws-info)

                (hara.test.checker.logic
                 any all is-not)

                (hara.test.form
                 fact facts =>))

(defn- display-errors
  [data]
  (let [errors (concat (executive/retrieve-line :failed data)
                       (executive/retrieve-line :thrown data))
        cnt  (count (:passed data))]
    (if (empty? errors)
      (result/result {:status :highlight
                      :data  (format "passed (%s)" cnt)})
      (result/result {:status :error
                      :data (format "passed (%s), errors: #{%s}"
                                    cnt
                                    (string/join ", " (mapv #(str (first %) ":" (second %))
                                                            errors)))}))))

(defn- retrieve-fn [kw]
  (fn [data]
    (->> (executive/retrieve-line kw data)
         (mapv #(str (first %) ":" (second %))))))

(defmethod task/task-defaults :test
  [_]
  {:construct {:input    (fn [_] *ns*)
               :lookup   (fn [_ project]
                           (project/all-files (:test-paths project)
                                              {}
                                              project))
               :env      (fn [_] (project/project))}
   :params    {:print {:item true
                       :result true
                       :summary true}
               :return :summary}
   :arglists '([] [ns] [ns params] [ns params project] [ns params lookup project])
   :main      {:count 4}
   :item      {:list     (fn [lookup _] (sort (keys lookup)))
               :pre      project/sym-name
               :output   executive/summarise
               :display  display-errors}
   :result    {:ignore  (fn [data]
                          (and (empty? (:failed data))
                               (empty? (:thrown data))))
               :keys    {:failed  (retrieve-fn :failed)
                         :thrown  (retrieve-fn :thrown)}
               :columns [{:key    :key
                          :align  :left}
                         {:key    :failed
                          :align  :left
                          :length 40
                          :color  #{:red}}
                         {:key    :thrown
                          :align  :left
                          :length 40
                          :color  #{:yellow}}]}
   :summary  {:finalise  executive/summarise-bulk}})

(definvoke run
  "runs all tests
 
   (run :list)
 
   (run 'hara.core.base.util)
   ;; {:files 1, :thrown 0, :facts 8, :checks 18, :passed 18, :failed 0}
   => map?"
  {:added "3.0"}
  [:task {:template :test
          :main {:fn executive/run}
          :params {:title "TESTING PROJECT"}}])

(defn print-options
  "output options for test results
 
   (print-options)
   => #{:disable :default :all :current :help}
 
   (print-options :default)
   => #{:print-bulk :print-failure :print-thrown}"
  {:added "3.0"}
  ([] (print-options :help))
  ([opts]
   (cond (set? opts)
         (alter-var-root #'common/*print*
                         (constantly opts))

         (= :help opts)
         #{:help :current :default :disable :all}

         (= :current opts) common/*print*

         (= :default opts)
         (alter-var-root #'common/*print* (constantly #{:print-thrown :print-failure :print-bulk}))

         (= :disable opts)
         (alter-var-root #'common/*print* (constantly #{}))

         (= :all opts)
         #{:print-thrown :print-success :print-facts :print-facts-success :print-failure :print-bulk})))

(defn- process-args
  "processes input arguments"
  {:added "3.0"}
  [args]
  (->> (map read-string args)
       (map (fn [x]
              (cond (symbol? x)
                    (keyword (name x))

                    (keyword? x) x

                    (string? x) (keyword x))))
       set))

(defn -main
  "main entry point for leiningen"
  {:added "3.0"}
  ([& args]
   (let [args (process-args args)
         {:keys [thrown failed] :as stats} (run :all)
         res (+ thrown failed)]
     (if (get args :exit)
       (System/exit res)
       res))))

(defn run-errored
  []
  (let [latest @executive/+latest+]
    (-> (set/union (set (:errored latest))
                   (set (map (comp :ns :meta) (:failed latest)))
                   (set (map (comp :ns :meta) (:thrown latest))))
        (run))))

(comment
  (run-errored)
  (./ns:reset '[hara])
  (./ns:reset '[hara.function.task]))
