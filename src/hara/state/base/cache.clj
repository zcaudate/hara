(ns hara.state.base.cache
  (:require [hara.protocol.state :as protocol.state]
            [hara.state.base.common :as common]
            [hara.state.base.impl :deps true]
            [hara.function.base.macro :as macro]
            [hara.function.base.executive :refer [defexecutive]]))

(defn cache-string
  "string represetation of a cache
 
   (str (cache {:a 1 :b 2} {:tag \"stuff\"
                            :display keys}))
   \"#stuff:atom(:a :b)\""
  {:added "3.0"}
  [cache]
  (let [{:keys [tag display print type]
         :or {tag "cache"
              display identity}} (.metadata cache)]
    (if print
      (print cache)
      (str tag type (display @(.state cache))))))

(defn cache-invoke
  "helper function for invoking the cache"
  {:added "3.0"}
  [cache & args]
  cache)

(defexecutive Cache
  "returns a `hara.state.base.cache.Cache` instance
 
   (->Cache (atom {}) {})"
  {:added "3.0"}
  [state metadata]
  {:this cache
   :display cache-string
   :invoke cache-invoke
   :args-number 0}
  
  clojure.lang.IRef
  (deref [_] (.deref state))
  
  protocol.state/IStateGet
  (-get-state [obj opts]
              (common/get state opts))
  
  protocol.state/IStateSet
  (-update-state [obj f args opts]
                 (common/update-apply state f args opts))
  (-set-state [obj v opts]
              (common/set state v opts))
  (-empty-state [obj opts]
                (common/empty state opts))
  (-clone-state [obj opts]
                (common/clone state opts))
  
  clojure.lang.IMeta
  (meta [_] metadata)

  clojure.lang.IObj
  (withMeta [this m]
            (Cache. state m)))

(defmethod protocol.state/-create-state Cache
  [_ data metadata]
  (Cache. (atom data) metadata))

(defmethod protocol.state/-container-state :cache
  [_]
  Cache)

(defn cache
  "creates a cache with the following properties
   
   (-> (cache {} {:tag \"stuff\"
                  :type :ref})
       (.state))
   => clojure.lang.Ref
   
   (str (cache {:a 1 :b 2} {:type :agent}))
   => \"#cache:agent{:a 1, :b 2}\""
  {:added "3.0"}
  ([data]
   (cache data {}))
  ([data {:keys [tag display type] :as metadata
          :or {type :atom}}]
   (try
     (let [cls   (common/container type)
           state (common/create cls data nil)]
       (Cache. state (assoc metadata :type type)))
     (catch java.lang.IllegalArgumentException e
       (throw (ex-info "Invalid cache type:"
                       {:type type
                        :options (set (keys (.getMethodTable protocol.state/-container-state)))}))))))

(defmacro defcache
  "defines a cache
 
   (defcache -a-)
   (.state -a-)
   => clojure.lang.Atom
   
   (defcache -b-
     [:volatile {:tag \"hello\"}])
   (.state -b-)
   => volatile?"
  {:added "3.0"}
  [name & [doc? attrs? [type metadata :as opts?]]]
  (let [[doc attrs {:keys [refresh] :as opts}]
        (macro/create-args [doc? attrs? opts?])

        [type metadata]
        (cond (nil? opts)
              [:atom {}]
              
              (vector? opts) opts
              
              :else
              (throw (ex-info "Cannot recognise option." {:opts opts})))
        body `(cache {} (assoc ~metadata :type ~type))]
    (if (or refresh
            (not (resolve name)))
      (macro/create-def-form name doc attrs () body))))
