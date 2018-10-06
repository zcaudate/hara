(ns hara.test.executive
  (:require [hara.core.base.util :as primitive]
            [hara.data.base.map :as map]
            [hara.io.file :as fs]
            [hara.io.project :as project]
            [hara.test.checker.base :as checker]
            [hara.test.common :as common]
            [hara.test.form.listener :as listener]
            [hara.test.form.print :as print]))

(defonce +latest+ (atom nil))

(defn accumulate
  "helper function for accumulating results over disparate facts and files"
  {:added "3.0"}
  ([func id]
   (let [sink (atom [])
         source common/*accumulator*]
     (add-watch source id (fn [_ _ _ n]
                            (if (= (:id n) id)
                              (swap! sink conj n))))
     (binding [common/*id* id]
       (func id sink))
     (remove-watch source id)
     @sink)))

(defn interim
  "summary function for accumulated results"
  {:added "3.0"}
  [facts]
  (let [results (mapcat :results facts)
        checks  (filter #(-> % :from (= :verify))    results)
        forms   (filter #(-> % :from (= :evaluate))  results)
        thrown  (filter #(-> % :type (= :exception)) forms)
        passed  (filter checker/succeeded? checks)
        failed  (filter (comp not checker/succeeded?) checks)
        facts   (filter (comp not empty? :results) facts)
        files   (->> checks
                     (map (comp :path :meta))
                     (frequencies)
                     (keys))]
    {:files  files
     :thrown thrown
     :facts  facts
     :checks checks
     :passed passed
     :failed failed}))

(defn retrieve-line
  "returns the line of the test"
  {:added "3.0"}
  [key results]
  (->> (mapv (fn [result]
               (let [refer (-> result :meta :refer)
                     line (-> result :meta :line)]
                 [line (if refer (-> refer name symbol))]))
             (get results key))))

(defn summarise
  "creates a summary of given results"
  {:added "3.0"}
  [items]
  (let [summary (map/map-vals count items)]
    (when (:print-bulk common/*print*)
      (doseq [failed (:failed items)]
        (-> failed
            (listener/summarise-verify)
            (print/print-failure)))
      (doseq [thrown (:thrown items)]
        (-> thrown
            (listener/summarise-evaluate)
            (print/print-thrown)))
      (if (seq summary)
        (print/print-summary summary)))
    (swap! +latest+ assoc
           :failed (:failed items)
           :thrown (:thrown items))
    summary))

(defn summarise-bulk
  "creates a summary of all bulk results"
  {:added "3.0"}
  [_ items _]
  (let [all-items (reduce (fn [out [id item]]
                            (reduce (fn [out [k data]]
                                      (update-in out [k] concat data))
                                    out
                                    (:data item)))
                          {}
                          (remove (fn [[ns item]]
                                    (when (= :error (:status item))
                                      (swap! +latest+ update-in [:errored] conj ns)
                                      true))
                                  items))]
    (summarise all-items)))

(defn run
  "runs all tests in a given namespace"
  {:added "3.0"}
  ([ns {:keys [id] :as params} lookup project]
   (binding [*warn-on-reflection* false
             common/*root*     (:root project)
             common/*errors*   (atom {})
             common/*settings* (merge common/*settings* params)]
     (let [id (or id (primitive/uuid))
           _       (reset! +latest+ {:errored []})
           test-ns (project/test-ns ns)
           facts   (accumulate (fn [id sink]
                                 (when-let [path (or (lookup test-ns)
                                                     (lookup ns))]
                                   (binding [common/*path* (str (fs/relativize (:root project) path))]
                                     (load-file path))))
                               id)
           results (interim facts)]
       results))))
