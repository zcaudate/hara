(ns hara.module.base.link
  (:require [hara.core.base.error :as error]
            [hara.module.base.source :as source]
            [hara.function.base.executive :refer [defexecutive]]
            [hara.function.base.invoke :as invoke :refer [definvoke]]
            [hara.function.base.macro :as macro]
            [hara.protocol.function :as protocol.function]
            [hara.state.base.common :as state]
            [hara.state.base.cache :refer [defcache]]))

(def ^:dynamic *bind-root* nil)

(defcache +registry
  "registry for all links"
  {:added "3.0"}
  [:atom {:tag "link.registry"}])

;; -----------------
;;      LINK
;; -----------------
;;
;; - structure is {:source {:ns <source-ns> :var <source-var>}
;;                 :sink   {:ns <sink-ns> :var <sink-var>}
;;
;; - on invocation, the link will:
;;       1. resolve its source function
;;       2. if function is present, rebind var-root, erroring otherwise
;;       3. invoke the function on given arguments
;;
;; - on initiation, the link is initiated with:
;;       1. :lazy, doing nothing until invoked
;;       2. :preempt, try to bind the var if source is loaded
;;       3. :eager, will load the source var and bind
;;       4. :metadata, will load arglist from source code
;;       5. :auto, will :preempt or load :metadata if source not loaded
;;
;; - the link will register itself on creation and register itself once resolved 

(definvoke ns-metadata
  "provides source metadata support for links
 
   (ns-metadata 'hara.core.base.check)
   => (contains '{ideref?  {:arglists ([obj])}
                  bigint?  {:arglists ([x])}
                  boolean? {:arglists ([x])}})"
  {:added "3.0"}
  [:memoize {:arglists '([ns])
             :function source/ns-metadata}])

(declare link-display link-invoke link-status bind-resolve)

(defexecutive Link
  "defines a Link type"
  {:added "3.0"}
  [source sink transform registry]
  {:tag "link"
   :invoke  link-invoke
   :display link-display}
  
  clojure.lang.IDeref
  (deref
   [link]
   (bind-resolve link)))

(defn link
  "creates a link
   (ns-unmap *ns* '-byte0?-)
   (declare -byte0?-)
 
   (link {:ns 'hara.core.base.check :name 'bytes?}
         #'-byte0?-)
   ;; #link{:source hara.core.base.check/bytes?, :bound false, :status :resolved, :synced false, :registered false}
   => link?"
  {:added "3.0"}
  ([source sink]
   (link source sink identity))
  ([source sink transform]
   (link source sink transform +registry))
  ([source sink transform registry]
   (let [{:keys [ns name]} source
         ns (or ns (.getName *ns*))]
     (Link. (assoc source :ns ns)
            sink
            transform
            registry))))

(defn link?
  "checks if object is a link
 
   (link? -lnk-)
   => true"
  {:added "3.0"}
  [obj]
  (instance? Link obj))

(defn register-link
  "adds link to global registry
 
   (register-link -lnk-)
   => #'hara.module.base.link-test/-lnk-
 
   (registered-link? -lnk-)
   => true"
  {:added "3.0"}
  ([^Link link]
   (let [sink (.sink link)
         registry (.registry link)]
     (register-link link sink registry)))
  ([^Link link sink registry]
   (state/update registry assoc sink link)
   sink))

(defn deregister-link
  "removes a link from global registry
 
   (deregister-link -lnk-)
   => #'hara.module.base.link-test/-lnk-
 
   (registered-link? -lnk-)
   => false"
  {:added "3.0"}
  ([^Link link]
   (let [sink (.sink link)
         registry (.registry link)]
     (deregister-link link sink registry)))
  ([^Link link sink registry]
   (state/update registry dissoc sink)
   sink))

(defn registered-link?
  "checks if a link is registered
 
   (registered-link? -lnk-)
   => false"
  {:added "3.0"}
  ([^Link link]
   (let [sink     (.sink link)
         registry (.registry link)]
     (= link (get @registry sink)))))

(defn registered-links
  "returns all registered links
 
   (register-link -lnk-)
   => #'hara.module.base.link-test/-lnk-
   
   (registered-links)
   => (contains [(exactly #'-lnk-)])"
  {:added "3.0"}
  ([]
   (registered-links +registry))
  ([registry]
   (keys @registry)))

(defn unresolved-links
  "returns all unresolved links
 
   (unresolved-links)"
  {:added "3.0"}
  ([]
   (unresolved-links +registry))
  ([registry]
   (keep (fn [[k link]]
           (= :unresolved (link-status link))
           k)
         @registry)))

(defn resolve-links
  "resolves all unresolved links in a background thread
 
   (resolve-links)"
  {:added "3.0"}
  ([]
   (resolve-links +registry))
  ([registry]
   (future
     (mapv (fn [v]
             (try
               (bind-resolve @v)
               (catch Throwable t
                 (println "LINK RESOLVE ERROR:" @v))))
           (unresolved-links registry)))))

(defn link-bound?
  "checks if the var of the link has been bound, should be true
 
   (link-bound? -lnk-)
   => true
   
   (link-bound? (->Link {:ns 'hara.core.base.check :name 'bytes?}
                        nil
                        nil
                        nil))
   => false"
  {:added "3.0"}
  ([^Link link]
   (= link
      (and (.sink link) @(.sink link)))))

(defn link-status
  "lists the current status of the link
 
   (link-status (->Link {:ns 'hara.core.base.check :name 'bytes?}
                        nil
                        nil
                        nil))
   => :resolved"
  {:added "3.0"}
  ([^Link link]
   (let [{:keys [ns name]} (.source link)
         source-ns  (find-ns ns)
         source-var (error/suppress (ns-resolve ns name))]
     (cond (nil? source-ns)
           :unresolved

           (nil? source-var)
           :source-var-not-found

           (link? @source-var)
           :linked

           :else
           :resolved))))

(defn find-source-var
  "finds the source var in the link
 
   (find-source-var (->Link {:ns 'hara.core.base.check :name 'bytes?}
                            nil
                            nil
                            nil))
   => #'hara.core.base.check/bytes?"
  {:added "3.0"}
  ([^Link link]
   (let [{:keys [ns name]} (.source link)]
     (if (find-ns ns) (ns-resolve ns name)))))

(defn link-synced?
  "checks if the source and sink have the same value
 
   (def -bytes?- hara.core.base.check/bytes?)
 
   (link-synced? (->Link {:ns 'hara.core.base.check :name 'bytes?}
                         #'-bytes?-
                         nil
                         nil))
   => true"
  {:added "3.0"}
  ([^Link link]
   (and (= :resolved (link-status link))
        (= @(find-source-var link)
           (and (.sink link) @(.sink link))))))

(defn link-selfied?
  "checks if the source and sink have the same value
 
   (declare -selfied-)
   (def -selfied- (link {:name '-selfied-}
                        #'-selfied-
                        ))
   
   (link-selfied? -selfied-)
   => true"
  {:added "3.0"}
  ([^Link link]
   (and (link-bound? link)
        (= :linked (link-status link))
        (= (.sink link) (find-source-var link)))))

(defn link-display
  "displays the link
 
   (link-display -selfied-)
   => {:source 'hara.module.base.link-test/-selfied-
       :bound true,
       :status :linked,
       :synced false,
       :registered false}"
  {:added "3.0"}
  ([^Link link]
   (let [{:keys [ns name]} (.source link)]
     {:source  (symbol (str ns) (str name))
      :bound   (link-bound? link)
      :status  (link-status link)
      :synced  (link-synced? link)
      :registered (registered-link? link)})))

(defn transform-metadata
  "helper function for adding metadata to vars"
  {:added "3.0"}
  [sink transform metadata]
  (let [meta-fn (:meta transform)
        metadata (if meta-fn
                   (meta-fn metadata)
                   metadata)]
    (doto sink
      (alter-meta! merge metadata))))

(defn bind-metadata
  "retrievess the metadata of a function from source code
 
   (declare -metadata-)
   (def -metadata- (link {:ns 'hara.core.base.check :name 'bytes?}
                         #'-metadata-))
   
   (bind-metadata -metadata-)
   
   (-> (meta #'-metadata-)
       :arglists)
   => '([x])"
  {:added "3.0"}
  ([^Link link]
   (let [{:keys [ns name]} (.source link)]
     (doto (.sink link)
       (transform-metadata (.transform link)
                           (get (ns-metadata ns) name))))))

(declare bind-resolve)

(defn bind-source
  "retrieves the source var
 
   (declare -source-)
   (def -source- (link {} #'-source-))
   
   (bind-source (.sink -source-) #'hara.core.base.check/bytes? (.transform -source-))
   => hara.core.base.check/bytes?"
  {:added "3.0"}
  [sink source-var transform]
  (let [source-obj @source-var
        source-obj (if (link? source-obj)
                     (bind-resolve source-obj)
                     source-obj)
        source-obj (if-let [value-fn (:value transform)]
                     (value-fn source-obj)
                     source-obj)]
    (if *bind-root*
      (doto sink
        (.bindRoot source-obj)
        (transform-metadata transform (meta source-var))))
    source-obj))

(defn bind-resolve
  "binds a link or a series of links
   
   (deflink -byte0?- hara.core.base.check/byte?)
   
   (deflink -byte1?- -byte0?-)
   
   (deflink -byte2?- -byte1?-)
   
   (binding [*bind-root* true]
     (bind-resolve -byte2?-))
   (fn? -byte2?-) => true
   (fn? -byte1?-) => true
   (fn? -byte0?-) => true"
  {:added "3.0"}
  ([^Link link]
   (cond (link-selfied? link)
         (throw (ex-info "Link selfied." {:link link}))
         
         :else
         (let [{:keys [ns name]} (.source link)
               status (error/suppress (require ns) :source-ns-not-found)
               source-var (if (not= status :source-ns-not-found)
                            (ns-resolve ns name))
               source-obj (if source-var
                            (bind-source (.sink link) source-var (.transform link))
                            (throw (ex-info "Link not found." {:source (.source link)
                                                               :status (or status
                                                                           :source-var-not-found)})))]
           (deregister-link link)
           source-obj))))

(defn bind-preempt
  "retrieves the source var if available
 
   (deflink -byte0?- hara.core.base.check/byte?)
 
   (binding [*bind-root* true]
     (bind-preempt -byte0?-))
   => hara.core.base.check/byte?"
  {:added "3.0"}
  ([^Link link]
   (if *bind-root*
     (let [source-var (find-source-var link)]
       (if (and source-var (not (link? @source-var)))
         (bind-source (.sink link) source-var (.transform link)))))))

(defn bind-verify
  "retrieves the source var if available
 
   (deflink -byte0?- hara.core.base.check/byte?)
   
   (binding [*bind-root* true]
     (bind-verify -byte0?-))
   => (exactly #'hara.module.base.link-test/-byte0?-)"
  {:added "3.0"}
  ([^Link link]
   (let [{:keys [ns name]} (.source link)]
     (if (get (ns-metadata ns) name)
       (.sink link)
       (throw (ex-info "Source var not found." {:ns ns :name name}))))))

(defn bind-init
  "automatically loads the var if possible
 
   (deflink -byte0?- hara.core.base.check/byte?)
   
   (binding [*bind-root* true]
     (bind-init -byte0?- :auto))
   => hara.core.base.check/byte?
 
   (deflink -import0- hara.code/import)
 
   (binding [*bind-root* true]
     (bind-init -import0- :resolve))
   => hara.code/import"
  {:added "3.0"}
  ([^Link link key]
   (case key
     :lazy      nil
     :preempt   (bind-preempt link)
     :metadata  (bind-metadata link)
     :verify    (bind-verify link)
     :resolve   (bind-resolve link)
     :auto      (or (bind-preempt link)
                    (bind-metadata link))
     nil)))

(defn link-invoke
  "invokes a link
 
   (deflink -byte0?- hara.core.base.check/byte?)
   
   (deflink -byte1?- -byte0?-)
   
   (deflink -byte2?- -byte1?-)
   
   (-byte2?- (byte 1)) => true"
  {:added "3.0"}
  ([^Link link & args]
   (let [func (bind-resolve link)]
     (apply func args))))

(defn intern-link
  "creates a registers a link
 
   (intern-link '-byte0?- {:ns 'hara.core.base.check :name 'bytes?})
   ;;#link{:source hara.core.base.check/bytes?, :bound true, :status :resolved, :synced false, :registered true}
   => link?"
  {:added "3.0"}
  ([name source]
   (intern-link *ns* name source))
  ([ns name source]
   (intern-link ns name source identity))
  ([ns name source transform]
   (intern-link ns name source transform +registry))
  ([ns name source transform registry]
   (let [ns   (the-ns ns)
         sink (clojure.lang.Var/intern ns name)
         lk   (link source sink transform registry)
         _    (doto sink
                (.setMeta {})
                (.bindRoot lk))]
     (register-link lk sink registry)
     lk)))

(definvoke invoke-intern-link
  "creates a set of forms constructing a link
 
   (invoke-intern-link :link '-link- {:ns 'hara.core.base.check :name 'bytes?} nil)"
  {:added "3.0"}
  [:method {:multi protocol.function/-invoke-intern
            :val :link}]
  ([_ lname config _]
   (let [sink-ns (.getName *ns*) 
         {:keys [resolve ns name transform registry]
          :or {resolve :auto
               transform nil
               ns   sink-ns
               name lname}}  config
         body `(intern-link (quote ~sink-ns)
                            (quote ~lname)
                            {:ns   (quote ~ns)
                             :name (quote ~name)}
                            ~transform
                            (or ~registry +registry))]
    `(do (def ~lname ~body)
         (bind-init ~lname ~resolve)
         (doto (var ~lname)
           (alter-meta! merge ~(dissoc config :ns :name)))))))

(defmacro deflink
  "defines a link
 
   (deflink -byte3?- hara.core.base.check/byte?)
   @-byte3?-
   => hara.core.base.check/byte?
     
   (deflink -atom?- hara.core.base.check/atom?)
   @-atom?-
   => hara.core.base.check/atom?"
  {:added "3.0"}
  [lname doc? & [attrs? sym]]
  (let [[doc attrs sym]
        (macro/create-args [doc? attrs? sym])
        
        config (if (symbol? sym)
                 {:ns   (if-let [ns (namespace sym)] (symbol ns))
                  :name (symbol (clojure.core/name sym))}
                 (throw (ex-info "symbols only as input" {:form sym})))]
    (invoke-intern-link :link lname config nil)))

(comment
  (definvoke bytes?
    [:link {:resolve :lazy :ns hara.core.base.check :var bytes?}])
  
  (deflink bytes? hara.core.base.check/bytes?)
  
  (definvoke bytes?
    [:link {:ns hara.core.base.check
            :name bytes?
            :transform {:meta  (fn [{:keys [arglists] :as m}]
                                 (assoc m :arglists '([a b c d])))
                        :value (fn [f]
                                 (fn [x]
                                   [x x]))}}])
  (./ns:reset '[hara])
  (./code:incomplete '[hara.module])
  (./code:incomplete '[hara.function])
  (binding [*bind-root* true]
    (bytes? 1))
  (unresolved-links)
  (resolve-links)
  (bytes? 1)
  
  (@bytes? 1)
  
  (bytes? 1)
  
  (@bytes? "a"))
