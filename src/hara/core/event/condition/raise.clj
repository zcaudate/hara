(ns hara.core.event.condition.raise
  (:require [hara.data.base.map :as map]
            [hara.core.event.handler :as handler]
            [hara.core.event.condition.data :as data]
            [hara.core.event.condition.manage :as manage]))

(defn default-unhandled-fn
  "raises an unhandled exception"
  {:added "3.0"}
  [issue]
  (let [ex (data/exception issue)]
    (throw ex)))

(declare raise-loop)

(defn raise-catch
  "raises a catch exception"
  {:added "3.0"}
  [manager value]
  (throw (data/catch-condition (:id manager) value)))

(defn raise-choose
  "raises a choose exception"
  {:added "3.0"}
  [issue label args optmap]
  (let [target (get optmap label)]
    (cond (nil? target)
          (throw (ex-info "Label has not been implemented." {:label label}))

          (= target (:id issue))
          (manage/manage-apply (-> issue :options label) args label)

          :else
          (throw (data/choose-condition target label args)))))

(defn- raise-unhandled [issue optmap]
  (if-let [[label & args] (:default issue)]
    (raise-choose issue label args optmap)
    (default-unhandled-fn issue)))

(defn raise-fail
  "raises a fail exception"
  {:added "3.0"}
  [issue data]
  (throw (data/exception issue (handler/expand-data data))))

(defn- raise-escalate [issue res managers optmap]
  (let [ndata     (handler/expand-data (:data res))
        noptions  (:options res)
        noptmap   (zipmap (keys noptions) (repeat (:id issue)))
        ndefault  (:default res)
        nissue (-> issue
                   (update-in [:data] merge ndata)
                   (update-in [:options] merge noptions)
                   (map/assoc-if :default ndefault))]
    (raise-loop nissue (next managers) (merge noptmap optmap))))

(defn raise-loop
  "makes sure that the issue has been handled by all managers"
  {:added "3.0"}
  [issue [manager & more :as managers] optmap]
  (if manager
    (if-let [handler (first (handler/match-handlers manager (:data issue)))]
      (let [data (:data issue)
            result  ((:fn handler) data)]
        (condp = (:type result)
          :continue   (:value result)
          :choose     (raise-choose issue (:label result) (:args result) optmap)
          :default    (raise-unhandled issue optmap)
          :fail       (raise-fail issue (:data result))
          :escalate   (raise-escalate issue result managers optmap)
          (raise-catch manager result)))
      (recur issue more optmap))
    (raise-unhandled issue optmap)))
