(ns hara.core.component
  (:require [clojure.set :as set]
            [hara.core.base.check :as check]
            [hara.core.base.error :as error]
            [hara.core.base.sort :as sort]
            [hara.data.base.map :as map]
            [hara.data.base.nested :as nested]
            [hara.data.base.record :as record]
            [hara.protocol.component :as protocol.component]))

(extend-protocol protocol.component/IComponent
  Object
  (-start [this] this)
  (-stop  [this] this)
  (-properties [this] {}))
    
(defn primitive?
  "checks if a component is a primitive type
 
   (primitive? 1) => true
 
   (primitive? {}) => false"
  {:added "3.0"}
  [x]
  (or (string? x)
      (number? x)
      (check/boolean? x)
      (check/regexp? x)
      (check/uuid? x)
      (check/uri? x)
      (check/url? x)))

(defn component?
  "checks if an instance extends IComponent
 
   (component? (Database.))
   => true"
  {:added "3.0"}
  [x]
  (error/suppress (extends? protocol.component/IComponent (type x))))

(defn started?
  "checks if a component has been started
 
   (started? 1)
   => true
 
   (started? {})
   => false
 
   (started? (start {}))
   => true
 
   (started? (Database.))
   => false
 
   (started? (start (Database.)))
   => true
 
   (started? (stop (start (Database.))))
   => false"
  {:added "3.0"}
  [component]
  (try (protocol.component/-started? component)
       (catch IllegalArgumentException e
         (if (check/iobj? component)
           (-> component meta :started true?)
           (primitive? component)))
       (catch AbstractMethodError e
         (if (check/iobj? component)
           (-> component meta :started true?)
           (primitive? component)))))

(defn stopped?
  "checks if a component has been stopped
 
   (stopped? 1)
   => false
 
   (stopped? {})
   => true
 
   (stopped? (start {}))
   => false
 
   (stopped? (Database.))
   => true
 
   (stopped? (start (Database.)))
   => false
 
   (stopped? (stop (start (Database.))))
   => true"
  {:added "3.0"}
  [component]
  (try (protocol.component/-stopped? component)
       (catch IllegalArgumentException e
         (-> component started? not))
       (catch AbstractMethodError e
         (-> component started? not))))

(defn perform-hooks
  "perform hooks before main function
 
   (perform-hooks (Database.)
                  {:init (fn [x] 1)}
                  [:init])
   => 1"
  {:added "3.0"}
  [component functions hook-ks]
  (reduce (fn [out k]
            (let [func (or (get functions k)
                           identity)]
              (func out)))
          component
          hook-ks))

(defn start
  "starts a component/array/system
 
   (start (Database.))
   => {:status \"started\"}"
  {:added "3.0"}
  ([component]
   (start component {}))
  ([component {:keys [setup hooks] :as opts}]
   (let [{:keys [pre-start post-start]} hooks
         functions (or (get component :functions)
                       (get opts :functions))
         setup     (or setup identity)
         component   (-> component
                         (perform-hooks functions pre-start)
                         (protocol.component/-start)
                         (setup)
                         (perform-hooks functions post-start))]
     (if (check/iobj? component)
       (vary-meta component assoc :started true)
       component))))

(defn stop
  "stops a component/array/system
 
   (stop (start (Database.))) => {}"
  {:added "3.0"}
  ([component]
   (stop component {}))
  ([component {:keys [teardown hooks] :as opts}]
   (let [{:keys [pre-stop post-stop]} hooks
         functions (or (get component :functions)
                       (get opts :functions))
         teardown  (or teardown identity)
         component (-> component
                       (perform-hooks functions pre-stop)
                       (teardown)
                       (protocol.component/-stop)
                       (perform-hooks functions post-stop))]
     (if (check/iobj? component)
       (vary-meta component dissoc :started)
       component))))

(defn properties
  "returns properties of the system
 
   (properties (Database.)) => {}
 
   (properties (Filesystem.)) => {:hello \"world\"}"
  {:added "3.0"}
  [component]
  (try (protocol.component/-properties component)
       (catch IllegalArgumentException e
         {})
       (catch AbstractMethodError e
         {})))

(declare system? array? start-array stop-array)

(deftype ComponentArray [arr]
  Object
  (toString [this]
    (let [{:keys [tag display]} (meta this)]
      (str "#"
           (or tag "arr")
           (if display
             (display this)
             (mapv (fn [v]
                     (cond (or (system? v)
                               (array? v)
                               (not (component? v)))
                           v

                           :else
                           (reduce (fn [m [k v]]
                                     (cond (extends? protocol.component/IComponent (type v)) ;; for displaying internal keys
                                           (update-in m ['*] (fnil #(conj % k) []))

                                           :else
                                           (assoc m k v)))
                                   (record/empty v)
                                   v)))
                   arr)))))

  protocol.component/IComponent
  (-start [this] (start-array this))
  (-stop  [this] (stop-array  this))

  clojure.lang.Seqable
  (seq [this] (seq arr))

  clojure.lang.IObj
  (withMeta [this m]
    (ComponentArray. (with-meta arr m)))

  clojure.lang.IMeta
  (meta [this] (meta arr))

  clojure.lang.Counted
  (count [this] (count arr))

  clojure.lang.Indexed
  (nth [this i]
    (nth arr i nil))

  (nth [ova i not-found]
    (nth arr i not-found)))

(defmethod print-method ComponentArray
  [v ^java.io.Writer w]
  (.write w (str v)))

(defn start-array
  "starts an array of components"
  {:added "3.0"}
  [carr]
  (with-meta
    (ComponentArray. (mapv start (seq carr)))
    (meta carr)))

(defn stop-array
  "stops an array of components"
  {:added "3.0"}
  [carr]
  (with-meta
    (ComponentArray. (mapv stop (seq carr)))
    (meta carr)))

(defn constructor
  "returns the constructor from topology"
  {:added "3.0"}
  [x]
  (if (map? x)
    (:constructor x)
    x))

(defn array
  "creates an array of components
 
   (def recs (start (array {:constructor map->Database} [{:id 1} {:id 2}])))
   (count (seq recs)) => 2
   (first recs) => (just {:id 1 :status \"started\"})"
  {:added "3.0"}
  [{:keys [constructor]} config]
  (if (vector? config)
    (let [defaults (meta config)]
      (ComponentArray. (mapv (fn [entry]
                               (if (map? entry)
                                 (constructor (nested/merge-nested defaults entry))
                                 entry))
                             config)))
    (throw (Exception. (str "Config " config " has to be a vector.")))))

(defn array?
  "checks if object is a component array
 
   (array? (array map->Database []))
   => true"
  {:added "3.0"}
  [x]
  (instance? ComponentArray x))

(declare start-system stop-system)

(defn system-string
  "returns the system for display
 
   (system-string (system {:a [identity]
                           :b [identity]}
                          {:a 1 :b 2}
                          {:tag \"happy\"}))
   => \"#happy {:a 1, :b 2}\""
  {:added "3.0"}
  ([sys]
   (let [{:keys [tag display]} (meta sys)]
     (str "#" (or tag "sys") " "
          (if display
            (display sys)
            (reduce (fn [m [k v]]
                      (cond (or (system? v)
                                (array? v)
                                (not (component? v)))
                            (assoc m k v)

                            :else
                            (assoc m k (reduce (fn [m [k v]]
                                                 (cond (extends? protocol.component/IComponent (type v))
                                                       (update-in m ['*] (fnil #(conj % k) []))

                                                       :else
                                                       (assoc m k v)))
                                               (record/empty v)
                                               v))))
                    {} sys))))))

(defrecord ComponentSystem []
  Object
  (toString [sys]
    (system-string sys))

  protocol.component/IComponent
  (-start [sys]
    (start-system sys))
  (-stop [sys]
    (stop-system sys)))

(defmethod print-method ComponentSystem
  [v ^java.io.Writer w]
  (.write w (str v)))

(defn system?
  "checks if object is a component system
 
   (system? (system {} {}))
   => true"
  {:added "3.0"}
  [x]
  (instance? ComponentSystem x))

(defn long-form-imports
  "converts short form imports to long form
 
   (long-form-imports [:db [:file {:as :fs}]])
   => {:db   {:type :single, :as :db},
       :file {:type :single, :as :fs}}
 
   (long-form-imports [[:ids {:type :element :as :id}]])
   => {:ids {:type :element, :as :id}}"
  {:added "3.0"}
  [args]
  (->> args
       (map (fn [x]
              (cond (keyword? x)
                    [x {:type :single :as x}]
                    (vector? x)
                    [(first x) (merge {:type :single} (second x))])))
       (into {})))

(defn long-form-entry
  "converts short form entry into long form
 
   (long-form-entry [{:constructor :identity
                      :initialiser :identity}])
   => {:type :build
       :compile :single
       :constructor :identity
       :initialiser :identity
       :import {}, :dependencies ()}
 
   (long-form-entry [[identity]])
   => (contains {:compile :array,
                 :type :build,
                 :constructor fn?
                 :import {},
                 :dependencies ()})
 
   (long-form-entry [[identity] [:model {:as :raw}] [:ids {:type :element :as :id}]])
   => (contains {:compile :array,
                 :type :build
                 :constructor fn?
                 :import {:model {:type :single, :as :raw},
                          :ids {:type :element, :as :id}},
                 :dependencies [:model :ids]})"
  {:added "3.0"}
  [[desc & args]]
  (let [dependencies  (keep (fn [x] (if (vector? x)
                                      (if-not (:nocheck (second x))
                                        (first x))
                                      x))
                            args)
        [desc form] (if (vector? desc)
                      [(first desc) {:compile :array}]
                      [desc {:compile :single}])
        desc (cond (or (fn? desc)
                       (instance? clojure.lang.MultiFn desc))
                   {:type :build :constructor desc}

                   (:type desc) desc

                   (:expose desc)
                   (-> desc
                       (dissoc :expose)
                       (assoc :type :expose :in (first dependencies) :function (:expose desc)))

                   :else
                   (assoc desc :type :build))]
    (cond-> (merge form desc)
      (= :build (:type desc))
      (assoc :import (long-form-imports args))

      :finally
      (assoc :dependencies dependencies))))

(defn long-form
  "converts entire topology to long form
 
   (long-form {:db [identity]
               :count [{:expose :count} :db]})
   => (contains-in {:db {:compile :single,
                         :type :build,
                         :constructor fn?,
                         :import {},
                         :dependencies ()},
                    :count {:compile :single,
                           :type :expose,
                            :in :db,
                            :function :count,
                            :dependencies [:db]}})"
  {:added "3.0"}
  [topology]
  (map/map-vals long-form-entry topology))

(defn get-dependencies
  "get dependencies for long form
   (-> (long-form {:model   [identity]
                   :ids     [[identity]]
                   :traps   [[identity] [:model {:as :raw}] [:ids {:type :element :as :id}]]
                   :entry   [identity :model :ids]
                   :nums    [[{:expose :id}] :traps]
                   :model-tag  [{:expose :tag
                                 :setup identity}  :model]})
       get-dependencies)
  => {:model #{},
       :ids #{},
       :traps #{:ids :model},
       :entry #{:ids :model},
       :nums #{:traps},
       :model-tag #{:model}}"
  {:added "3.0"}
  [full-topology]
  (map/map-vals (comp set :dependencies) full-topology))

(defn get-exposed
  "get exposed keys for long form
   (-> (long-form {:model   [identity]
                   :ids     [[identity]]
                   :traps   [[identity] [:model {:as :raw}] [:ids {:type :element :as :id}]]
                   :entry   [identity :model :ids]
                   :nums    [[{:expose :id}] :traps]
                   :model-tag  [{:expose :tag
                                 :setup identity}  :model]})
       get-exposed)
  => [:nums :model-tag]"
  {:added "3.0"}
  [full-topology]
  (reduce-kv (fn [arr k v]
               (if (= :expose (:type v))
                 (conj arr k)
                 arr))
             [] full-topology))

(defn all-dependencies
  "gets all dependencies for long form
 
   (all-dependencies
    {1 #{4 2}
     2 #{3}
     3 #{5}
     4 #{}
     5 #{6}
     6 #{}})
   => {1 #{2 3 4 5 6}
       2 #{3 5 6}
       3 #{5 6}
       4 #{}
       5 #{6}
       6 #{}}
 
   (-> (long-form {:model   [identity]
                   :ids     [[identity]]
                   :traps   [[identity] [:model {:as :raw}] [:ids {:type :element :as :id}]]
                   :entry   [identity :model :ids]
                   :nums    [[{:expose :id}] :traps]
                  :model-tag  [{:expose :tag
                                 :setup identity}  :model]})
       get-dependencies
       all-dependencies)
   => {:model #{},
       :ids #{},
       :traps #{:ids :model},
       :entry #{:ids :model},
       :nums #{:ids :traps :model},
       :model-tag #{:model}}"
  {:added "3.0"}
  [m]
  (let [order (sort/topological-sort m)]
    (reduce (fn [out key]
              (let [inputs (set (get m key))
                    result (set (concat inputs (mapcat out inputs)))]
                (assoc out
                       key
                       result)))
            {}
            order)))

(defn valid-subcomponents
  "returns only the components that will work (for partial systems)
 
   (valid-subcomponents
    (long-form {:model  [identity]
                :tag    [{:expose :tag} :model]
                :kramer [identity :tag]})
    [:model])
   => [:model :tag]"
  {:added "3.0"}
  [full-topology keys]
  (let [expose-keys (get-exposed full-topology)
        valid-keys (set (concat expose-keys keys))
        sub-keys (->> full-topology
                      get-dependencies
                      all-dependencies
                      (map/map-entries (fn [[k v]] [k (conj v k)])))]
    (reduce-kv (fn [arr k v]
                 (if (set/superset? valid-keys v)
                   (conj arr k)
                   arr))
               []
               sub-keys)))

(defn system
  "creates a system of components
 
   ;; The topology specifies how the system is linked
   (def topo {:db        [map->Database]
              :files     [[map->Filesystem]]
              :catalogs  [[map->Catalog] [:files {:type :element :as :fs}] :db]})
 
   ;; The configuration customises the system
   (def cfg  {:db     {:type :basic
                       :host \"localhost\"
                       :port 8080}
              :files [{:path \"/app/local/1\"}
                      {:path \"/app/local/2\"}]
              :catalogs [{:id 1}
                         {:id 2}]})
 
   ;; `system` will build it and calling `start` initiates it
   (def sys (-> (system topo cfg) start))
 
   ;; Check that the `:db` entry has started
   (:db sys)
   => (just {:status \"started\",
             :type :basic,
             :port 8080,
             :host \"localhost\"})
 
   ;; Check the first `:files` entry has started
   (-> sys :files first)
   => (just {:status \"started\",
             :path \"/app/local/1\"})
 
   ;; Check that the second `:store` entry has started
   (->> sys :catalogs second)
   => (contains-in {:id 2
                    :status \"started\"
                    :db {:status \"started\",
                         :type :basic,
                         :port 8080,
                         :host \"localhost\"}
                    :fs {:path \"/app/local/2\", :status \"started\"}})"
  {:added "3.0"}
  ([topology config]
   (system topology config {:partial false}))
  ([topology config {:keys [partial? tag display] :as opts}]
   (let [full   (long-form topology)
         valid  (valid-subcomponents full (keys config))
         expose (get-exposed full)
         diff   (set/difference (set (keys full)) valid)
         _      (or (empty? diff)
                    partial?
                    (throw (Exception. (str "Missing Config Keys: " diff " " full " " valid))))
         build  (apply dissoc full diff)
         dependencies (apply dissoc (get-dependencies full) diff)
         order (sort/topological-sort dependencies)
         initial  (apply dissoc build (concat diff (get-exposed full)))]
     (-> (reduce-kv (fn [sys k {:keys [constructor compile] :as build}]
                      (let [cfg (get config k)]
                        (assoc sys k (cond (= compile :array)
                                           (array build cfg)

                                           :else
                                           (constructor cfg)))))
                    (ComponentSystem.)
                    initial)
         (with-meta (merge {:partial (not (empty? diff))
                            :build   build
                            :order   order
                            :dependencies dependencies}
                           opts))))))

(defn system-import
  "imports a component into the system"
  {:added "3.0"}
  [component system import]
  (reduce-kv (fn [out k v]
               (let [{:keys [type as]} (get import k)
                     as (or as k)
                     subsystem (get system k)]
                 (cond (array? out)
                       (cond->> (seq out)
                         (= type :element)
                         (map #(assoc %2 as %1) subsystem)

                         (= type :single)
                         (map #(assoc % as subsystem))

                         :finally
                         (ComponentArray.))

                       :else
                       (assoc out as subsystem))))
             component
             import))

(defn system-expose
  "exposes a component into the system"
  {:added "3.0"}
  [_ system {:keys [in function] :as opts}]
  (let [subsystem (get system in)]
    (cond (array? subsystem)
          (->> (sequence subsystem)
               (map function)
               (ComponentArray.))

          :else
          (function subsystem))))

(defn start-system
  "starts a system
   (->> (system {:models [[identity] [:files {:type :element :as :fs}]]
                 :files  [[identity]]}
                {:models [{:m 1} {:m 2}]
                 :files  [{:id 1} {:id 2}]})
        start-system
        (into {}))
   => (contains-in {:models [{:m 1,
                              :fs {:id 1}}
                            {:m 2,
                              :fs {:id 2}}],
                    :files [{:id 1} {:id 2}]})"
  {:added "3.0"}
  [system]
  (let [{:keys [build order] :as meta} (meta system)]
    (reduce (fn [out k]
              (let [{:keys [type import setup hooks] :as opts} (get build k)
                    {:keys [pre-start post-start]} hooks
                    component (get out k)
                    functions (or (get component :functions)
                                  (get opts :functions))
                    setup     (or setup identity)
                    result (cond-> (perform-hooks component functions pre-start)
                             (= type :build)
                             (system-import out import)

                             (= type :expose)
                             (system-expose out opts)

                             :finally
                             (-> protocol.component/-start setup (perform-hooks functions post-start)))]
                (assoc out k result)))
            system
            order)))

(defn system-deport
  "deports a component from the system"
  {:added "3.0"}
  [component import]
  (reduce-kv (fn [out k v]
               (let [{:keys [type as]} (get import k)]
                 (cond (array? out)
                       (->> (seq out)
                            (map #(dissoc % as))
                            (ComponentArray.))

                       :else
                       (dissoc out as))))
             component
             import))

(defn stop-system
  "stops a system
   (stop-system
    (start-system
     (system {:model   [identity]
              :ids     [[identity]]
              :traps   [[identity] [:model {:as :raw}] [:ids {:type :element :as :id}]]
              :entry   [identity :model :ids]
              :nums    [[{:expose :id}] :traps]
              :model-tag  [{:expose :tag
                            :setup identity}  :model]}
            {:model {:tag :barbie}
              :ids   [1 2 3 4 5]
              :traps [{} {} {} {} {}]
              :entry {}})))
   =>  {:model {:tag :barbie}, :ids [1 2 3 4 5], :traps [{} {} {} {} {}], :entry {}}"
  {:added "3.0"}
  [system]
  (let [{:keys [build order] :as meta} (meta system)]
    (reduce (fn [out k]
              (let [{:keys [type import teardown hooks] :as opts} (get build k)
                    {:keys [pre-stop post-stop]} hooks
                    component (get out k)
                    functions (or (get component :functions)
                                  (get opts :functions))
                    teardown  (or teardown identity)
                    component (-> component
                                  (perform-hooks functions pre-stop)
                                  (teardown))]
                (cond (= type :build)
                      (assoc out k (-> component
                                       (protocol.component/-stop)
                                       (system-deport import)
                                       (perform-hooks functions post-stop)))

                      (= type :expose)
                      (dissoc out k))))
            system
            (reverse order))))
