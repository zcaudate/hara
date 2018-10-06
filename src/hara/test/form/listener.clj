(ns hara.test.form.listener
  (:require [hara.core.base.event :as event]
            [hara.test.common :as common]
            [hara.test.form.print :as print]))

(defn summarise-verify
  "extract the comparison into a valid format "
  {:added "3.0"}
  [result]
  {:type    (if (and (= :success (-> result :type))
                     (= true (-> result :data)))
              :success
              :failed)
   :path    (-> result :meta :path)
   :name    (-> result :meta :refer)
   :ns      (-> result :meta :ns)
   :line    (-> result :meta :line)
   :desc    (-> result :meta :desc)
   :form    (-> result :actual :form)
   :check   (-> result :checker :form)
   :actual  (-> result :actual :data)})

(defn summarise-evaluate
  "extract the form into a valid format"
  {:added "3.0"}
  [result]
  {:type    (-> result :type)
   :path    (-> result :meta :path)
   :name    (-> result :meta :refer)
   :ns      (-> result :meta :ns)
   :line    (-> result :meta :line)
   :desc    (-> result :meta :desc)
   :form    (-> result :form)
   :actual  (-> result :data)})

(event/deflistener form-printer {:test :form}
  [result]
  (if (and (-> result :type (= :exception))
           (common/*print* :print-thrown))
    (do (.beep (java.awt.Toolkit/getDefaultToolkit))
        (print/print-thrown (summarise-evaluate result)))))

(event/deflistener check-printer {:test :check}
  [result]
  (if (or (and (-> result :type (= :exception))
               (common/*print* :print-failure))
          (and (-> result :data (= false))
               (common/*print* :print-failure)))
    (do (.beep (java.awt.Toolkit/getDefaultToolkit))
        (print/print-failure (summarise-verify result))))
  (if (and (-> result :data (= true))
           (common/*print* :print-success))
    (print/print-success (summarise-verify result))))

(event/deflistener form-error-accumulator {:test :form}
  [result]
  (when common/*errors*
    (if (-> result :type (= :exception))
      (swap! common/*errors* update-in [:exception] conj result))))

(event/deflistener check-error-accumulator {:test :check}
  [result]
  (when common/*errors*
    (if (or (-> result :type (= :exception))
            (-> result :data (= false)))

      (swap! common/*errors* update-in [:failed] conj result))))

(event/deflistener fact-printer {:test :fact}
  [meta results skipped]
  (if (and (common/*print* :print-facts)
           (not skipped))
    (print/print-fact meta results)))

(event/deflistener fact-accumulator {:test :fact}
  [id meta results]
  (reset! common/*accumulator* {:id id :meta meta :results results}))

(event/deflistener bulk-printer {:test :bulk}
  [results]
  (if (common/*print* :print-bulk)
    (print/print-summary results))
  (when common/*errors*
    (println "-------------------------")
    (when-let [failed (:failed @common/*errors*)]
      (doseq [result failed]
        (print/print-failure (summarise-verify result))))
    (when-let [exceptions (:exception @common/*errors*)]
      (doseq [result exceptions]
        (print/print-thrown (summarise-evaluate result))))
    (println "")))
