(ns hara.print.base.progress
  (:require [hara.data.base.nested :as nested]
            [hara.print.base.common :as common]))

(def +progress-defaults+
  {:throttle          20
   :bar {:width       50
         :complete    \=
         :incomplete  \space
         :marker      \=}})

(defn replace-center
  "replaces the center of the background with text
 
   (replace-center \"=================\" \" hello \")
   => \"===== hello =====\""
  {:added "3.0"}
  [background text]
  (let [text-start (quot (- (count background) (count text)) 2)
        text-end   (+ text-start (count text))
        bg0  (subs background 0 text-start)
        bg1  (subs background text-end (count background))]
    (str bg0 text bg1)))

(defn progress-bar-string
  "converts a progress percentage to a string
 
   (progress-bar-string 50 100 (:bar +progress-defaults+))
   => \"===================== 50/100                      \""
  {:added "3.0"}
  [current total {:keys [width complete incomplete marker]}]
  (let [sb (new StringBuilder)
        threshold (-> (/ current total) (* width) int)
        text (format " %d/%d " current total)
        _   (doseq [i (range width)]
              (cond (< i threshold) (.append sb complete)
                    (= i threshold) (.append sb marker)
                    :else     (.append sb incomplete)))
        bar  (str sb)]
    (replace-center bar text)))

(defn progress-spinner-string
  "converts a progress to a spinner string
 
   (progress-spinner-string 9 20)
   => \"-\""
  {:added "3.0"}
  [current total]
  (if (= current total)
    ""
    (get  ["/" "-" "\\" "|"]
          (mod current 4))))

(defn progress-eta
  "calculates the estimated time left for the task
 
   (progress-eta 100 90 90)
   => 10"
  {:added "3.0"}
  [total progress elapsed]
  (-> total (/ progress) (- 1) (* elapsed) long))

(defrecord Progress [options state]
  Object
  (toString [_]
    (str "#progress" @state)))

(defmethod print-method Progress
  [v w]
  (.write w (str v)))

(defn progress
  "creates a structure representing progress
 
   (-> (progress) :state deref)
   => (contains {:total 100, :current 0, :label \"\"})"
  {:added "3.0"}
  ([]
   (progress nil))
  ([initial]
   (progress initial nil))
  ([initial options]
   (let [now (/ (System/nanoTime) 1000000000)
         default {:start-time  now
                  :update-time now
                  :total 100
                  :current 0
                  :label ""}]
     (Progress. (merge +progress-defaults+ options)
                (atom (merge default initial))))))

(defn progress-string
  "creates a string representation of the current progress
 
   (progress-string (-> @(:state (progress))
                        (update :update-time + 10)
                        (update :current + 9))
                    +progress-defaults+)
   => \"[=====                 9/100                       ] 101s -\""
  {:added "3.0"}
  ([progress]
   (progress-string @(:state progress)
                    (:options progress)))
  ([{:keys [total current label] :as state} {:keys [template bar] :as options}]
   (format "%s[%s] %ss %s"
           (if-not (empty? label) (str label " ") "")
           (progress-bar-string current total bar)
           (progress-eta total current (- (:update-time state)
                                          (:start-time state)))
           (progress-spinner-string current total))))

(defn progress-update
  "updates the progress meter
   
   (progress-update (progress) 10)"
  {:added "3.0"}
  ([progress]
   (progress-update progress 1))
  ([progress number]
   (swap! (:state progress)
          (fn [{:keys [total current] :as state}]
            (let [now (/ (System/nanoTime) 1000000000)
                  current (+ current number)
                  current (if (< current total)
                            current
                            total)]
              (-> state
                  (assoc :update-time now
                         :current current)))))))

(defn progress-test
  "demo for how progress should work
 
   (progress-test)"
  {:added "3.0"}
  ([]
   (let [prog (progress {:total 100})]
     (println)
     (dotimes [i 100]
       (Thread/sleep 50)
       (progress-update prog)
       (println common/+up+ common/+clearline+ (progress-string prog))))))
