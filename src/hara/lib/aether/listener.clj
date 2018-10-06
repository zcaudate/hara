(ns hara.lib.aether.listener
  (:require [hara.print :as print]
            [hara.io.file :as fs]
            [hara.module.artifact :as artifact]
            [hara.object :as object])
  (:import (org.eclipse.aether RepositoryEvent RepositoryListener)
           (org.eclipse.aether.transfer TransferEvent TransferListener TransferResource)))

(def ^:dynamic *progress* nil)

(def ^:dynamic *pairs* nil)

(defonce ^:dynamic *current* (atom nil))

(defn- print-params
  [color]
  {:padding 0
   :spacing 1
   :columns [{:key    :type
              :align  :left
              :length 15
              :color  color}
             {:key    :key
              :align  :left
              :length  49
              :color  color}
             {:key    :version
              :align  :left
              :length 9
              :color  color}
             {:key    :repo
              :align  :left
              :length 10
              :color  color}
             {:key    :info
              :align  :left
              :length 10
              :color  color}]})

(def category {:deploying :deploy
               :downloading :download
               :installing :install
               :resolving :resolve
               :deployed :deploy
               :downloaded :download
               :installed :install
               :resolved :resolve})

(defn event->rep
  "converts the event to a map representation"
  {:added "3.0"}
  [val]
  (if-let [artifact (-> val :event :artifact)]
    (artifact/artifact artifact)
    (assoc (artifact/artifact (-> val :event :metadata))
           :version "<meta>")))

(defn record
  "adds an event to the recorder"
  {:added "3.0"}
  ([type event]
   (record *progress* type event))
  ([progress type event]
   (let [out {:type type
              :event event
              :time (System/currentTimeMillis)}]
     (if progress
       (swap! progress conj out))
     (reset! *current* out))))

(defn aggregate
  "summarises all events that have been processed"
  {:added "3.0"}
  [events]
  (->> events
       (reduce (fn [{:keys [finished initiated timeline] :as out} {:keys [type event time]}]
                 (let [{:keys [artifact]} event
                       init-types #{:deploying :downloading :installing :resolving}
                       end-types  #{:deployed :downloaded :installed :resolved}]
                   (cond (init-types type)
                         (assoc-in out [:initiated artifact (category type)] time)

                         (end-types type)
                         (let [cat (category type)
                               start (get-in initiated [artifact cat] time)
                               initiated (update-in initiated [artifact] dissoc cat)
                               entry {:type cat :start start :total (- time start) :artifact artifact}
                               finished (update-in finished [artifact] (fnil #(conj % entry) []))]
                           {:initiated initiated
                            :finished finished}))))
               {:finished  {}
                :initiated {}})
       :finished))

(object/map-like
 TransferEvent
 {:tag "transfer.event"
  :read  :class
  :exclude [:session]}

 TransferResource
 {:tag "transfer.resource"
  :read  :class
  :exclude [:trace]})

(defonce +no-op+ (fn [_]))

(deftype TransferListenerProxy [fns]
  TransferListener
  (transferCorrupted [_ e] ((or (:corrupted fns) +no-op+) (object/to-map e)))
  (transferFailed [_ e] ((or (:failed fns) +no-op+) (object/to-map e)))
  (transferInitiated [_ e] ((or (:initiated fns) +no-op+) (object/to-map e)))
  (transferProgressed [_ e] ((or (:progressed fns) +no-op+) (object/to-map e)))
  (transferStarted [_ e] ((or (:started fns) +no-op+) (object/to-map e)))
  (transferSucceeded [_ e] ((or (:succeded fns) +no-op+) (object/to-map e))))

(def default-transfer-listener
  (TransferListenerProxy.
   {:initiated  (fn [{:keys [resource request-type] :as  m}]
                  (println (str request-type ": " (:resource-name resource) " has INITIATED")))
    :progressed (fn [{:keys [resource request-type] :as  m}])
    :succeded   (fn [{:keys [resource request-type] :as  m}]
                  (println (str request-type ": " (:resource-name resource) " has SUCCEEDED")))
    :failed     (fn [{:keys [resource request-type] :as  m}]
                  (println (str request-type ": " (:resource-name resource) " has FAILED")))}))

(def blank-transfer-listener
  (TransferListenerProxy. {}))

(object/map-like
 RepositoryEvent
 {:tag "repo.event"
  :read  :class
  :exclude [:session :trace]})

(deftype RepositoryListenerProxy [fns]
  RepositoryListener
  (artifactDeployed     [_ e]  ((or (-> fns  :artifact :deployed) +no-op+) (object/to-map e)))
  (artifactDeploying    [_ e]  ((or (-> fns  :artifact :deploying) +no-op+) (object/to-map e)))
  (artifactDescriptorInvalid  [_ e]  ((or (-> fns  :artifact :invalid) +no-op+) (object/to-map e)))
  (artifactDescriptorMissing  [_ e]  ((or (-> fns  :artifact :missing) +no-op+) (object/to-map e)))
  (artifactDownloaded   [_ e]  ((or (-> fns  :artifact :downloaded) +no-op+) (object/to-map e)))
  (artifactDownloading  [_ e]  ((or (-> fns  :artifact :downloading) +no-op+) (object/to-map e)))
  (artifactInstalled    [_ e]  ((or (-> fns  :artifact :installed) +no-op+) (object/to-map e)))
  (artifactInstalling   [_ e]  ((or (-> fns  :artifact :installing) +no-op+) (object/to-map e)))
  (artifactResolved     [_ e]  ((or (-> fns  :artifact :resolved) +no-op+) (object/to-map e)))
  (artifactResolving    [_ e]  ((or (-> fns  :artifact :resolving) +no-op+) (object/to-map e)))

  (metadataDeployed     [_ e]  ((or (-> fns  :metadata :deployed) +no-op+) (object/to-map e)))
  (metadataDeploying    [_ e]  ((or (-> fns  :metadata :deploying) +no-op+) (object/to-map e)))
  (metadataDownloaded   [_ e]  ((or (-> fns  :metadata :downloaded) +no-op+) (object/to-map e)))
  (metadataDownloading  [_ e]  ((or (-> fns  :metadata :downloading) +no-op+) (object/to-map e)))
  (metadataInstalled    [_ e]  ((or (-> fns  :metadata :installed) +no-op+) (object/to-map e)))
  (metadataInstalling   [_ e]  ((or (-> fns  :metadata :installing) +no-op+) (object/to-map e)))
  (metadataResolved     [_ e]  ((or (-> fns  :metadata :resolved) +no-op+) (object/to-map e)))
  (metadataResolving    [_ e]  ((or (-> fns  :metadata :resolving) +no-op+) (object/to-map e))))

(def +default-repository-listener+
  (RepositoryListenerProxy. {:artifact {:deploying   (fn [{:keys [artifact] :as  m}]
                                                       (record :deploying m))
                                        :deployed    (fn [{:keys [artifact] :as  m}]
                                                       (record :deployed m))
                                        :downloading (fn [{:keys [artifact] :as m}]
                                                       (record :downloading m))
                                        :downloaded  (fn [{:keys [artifact] :as m}]
                                                       (record :downloaded m))
                                        :installing  (fn [{:keys [artifact] :as m}]
                                                       (record :installing m))
                                        :installed   (fn [{:keys [artifact] :as m}]
                                                       (record :installed m))
                                        :resolving   (fn [{:keys [artifact] :as m}]
                                                       (record :resolving m))
                                        :resolved    (fn [{:keys [artifact] :as m}]
                                                       (record :resolved m))}
                             :metadata {:deploying   (fn [{:keys [artifact] :as  m}]
                                                       (record :deploying m))
                                        :deployed    (fn [{:keys [artifact] :as  m}]
                                                       (record :deployed m))
                                        :downloading (fn [{:keys [artifact] :as m}]
                                                       (record :downloading m))
                                        :downloaded  (fn [{:keys [artifact] :as m}]
                                                       (record :downloaded m))
                                        :installing  (fn [{:keys [artifact] :as m}]
                                                       (record :installing m))
                                        :installed   (fn [{:keys [artifact] :as m}]
                                                       (record :installed m))
                                        :resolving   (fn [{:keys [artifact] :as m}]
                                                       (record :resolving m))
                                        :resolved    (fn [{:keys [artifact] :as m}]
                                                       (record :resolved m))}}))

(def blank-repository-listener
  (RepositoryListenerProxy. {}))

(defn process-event
  "processes a recorded event"
  {:added "3.0"}
  [v]
  (if *pairs*
    (let [rep (event->rep v)
          label  (category (:type v))
          id     (-> v :event :artifact)
          p      (if-let [p (get-in @*pairs* [label id])]
                   p
                   (do (swap! *pairs* assoc-in [label id] v)
                       nil))]
      (print/print-row [(str "  " (name (:type v)))
                        (str (:group rep) "/" (:artifact rep)
                             (if (:extension rep) (str ":" (:extension rep))))
                        (:version rep)
                        (if p
                          [(symbol (or (-> v :event :repository :id)
                                       "local"))]
                          "")
                        (if p
                          (format "(%.2fs, %d bytes)"
                                  (/ (- (:time v) (:time p)) 1000.0)
                                  (-> v :event :file fs/file (.length)))

                          "")]
                       (print-params (if p #{:bold} #{:red}))))))

(add-watch *current* :print-listener
           (fn [_ _ _ v] (process-event v)))
