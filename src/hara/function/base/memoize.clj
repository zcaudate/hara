(ns hara.function.base.memoize
  (:require [hara.function.base.macro :as macro]
            [hara.function.base.executive :refer [defexecutive]]
            [hara.function.base.invoke :as invoke :refer [definvoke]]
            [hara.protocol.function :as protocol.function]
            [hara.state.base.cache :as cache :refer [defcache]]
            [hara.state.base.common :as state])
  (:refer-clojure :exclude [memoize]))

(defcache +registry
  "global registry for memoize functions"
  {:added "3.0"}
  [:atom {:tag "memoize.registry"}])

(declare memoize-display
         memoize-invoke
         memoize-status
         memoize-enabled?
         memoize-disabled?)

(defexecutive Memoize
  "creates an object that holds its own cache
 
   (declare -mem-)
   (def -mem-
     (->Memoize + nil (atom {}) #'-mem- +registry (volatile! true)))"
  {:added "3.0"}
  [function memfunction cache var registry status]
  {:tag     "memoize"
   :display memoize-display
   :invoke  memoize-invoke})

(defn memoize
  "caches the result of a function
   (ns-unmap *ns* '+-inc-)
   (ns-unmap *ns* '-inc-)
   (def +-inc- (atom {}))
   (declare -inc-)
   (def -inc-  (memoize inc +-inc- #'-inc-))
   
   (-inc- 1) => 2
   (-inc- 2) => 3"
  {:added "3.0"}
  ([function cache var]
   (memoize function cache var +registry (volatile! :enabled)))
  ([function cache var registry status]
   (let [memfunction (fn [& args]
                       (if-let [e (find @cache args)]
                         (val e)
                         (let [ret (apply function args)]
                           (state/update cache assoc args ret)
                           ret)))]
     (Memoize. function memfunction cache var registry status))))

(defn register-memoize
  "registers the memoize function
   
   (register-memoize -inc-)"
  {:added "3.0"}
  ([^Memoize mem]
   (let [var (.var mem)
         registry (.registry mem)]
     (register-memoize mem var registry)))
  ([^Memoize mem var registry]
   (state/update registry assoc var mem)))

(defn deregister-memoize
  "deregisters the memoize function
 
   (deregister-memoize -inc-)"
  {:added "3.0"}
  ([^Memoize mem]
   (let [var (.var mem)
         registry (.registry mem)]
     (deregister-memoize mem var registry)))
  ([^Memoize mem var registry]
   (state/update registry dissoc var)))

(defn registered-memoizes
  "lists all registered memoizes
   
   (registered-memoizes)"
  {:added "3.0"}
  ([] (registered-memoizes nil))
  ([status] (registered-memoizes status +registry))
  ([status registry]
   (let [pred (case status
                :enabled  memoize-enabled?
                :disabled memoize-disabled?
                identity)]
     (cond->> @registry
       status
       (keep (fn [[var mem]]
               (if (memoize-disabled? mem)
                 var)))))))

(defn registered-memoize?
  "checks if a memoize function is registered
 
   (registered-memoize? -mem-)
   => false"
  {:added "3.0"}
  ([^Memoize mem]
   (let [var      (.var mem)
         registry (.registry mem)]
     (= mem (get @registry var)))))

(defn memoize-status
  "returns the status of the object
 
   (memoize-status -inc-)
   => :enabled"
  {:added "3.0"}
  [^Memoize mem]
  @(.status mem))

(defn memoize-display
  "formats the memoize object
 
   (def +-plus- (atom {}))
   (declare -plus-)
   (def -plus- (memoize + +-plus- #'-plus-))
   (memoize-display -plus-)
   => (contains {:status :enabled, :registered false, :items number?})
   ;; {:fn +, :cache #atom {(1 1) 2}}
   "
  {:added "3.0"}
  [^Memoize mem]
  {:status (memoize-status mem)
   :registered (registered-memoize? mem)
   :items (count @(.cache mem))})

(defn memoize-disable
  "disables the usage of the cache
 
   @(memoize-disable -inc-)
   => :disabled"
  {:added "3.0"}
  ([^Memoize mem]
   (state/set (.status mem) :disabled)))

(defn memoize-disabled?
  "checks if the memoized function is disabled
 
   (memoize-disabled? -inc-)
   => true"
  {:added "3.0"}
  ([^Memoize mem]
   (= @(.status mem) :disabled)))

(defn memoize-enable
  "enables the usage of the cache
   
   @(memoize-enable -inc-)
   => :enabled"
  {:added "3.0"}
  ([^Memoize mem]
   (state/set (.status mem) :enabled)))

(defn memoize-enabled?
  "checks if the memoized function is disabled
 
   (memoize-enabled? -inc-)
   => true"
  {:added "3.0"}
  ([^Memoize mem]
   (= @(.status mem) :enabled)))

(defn memoize-invoke
  "invokes the function with arguments
   
   (memoize-invoke -plus- 1 2 3)
   => 6"
  {:added "3.0"}
  [^Memoize mem & args]
  (if (memoize-enabled? mem)
    (apply (.memfunction mem) args)
    (apply (.function mem) args)))

(defn memoize-remove
  "removes a cached result
 
   (memoize-remove -inc- 1)
   => 2"
  {:added "3.0"}
  ([^Memoize mem & args]
   (let [cache (.cache mem)
         v (get @cache args)]
     (state/update cache dissoc args)
     v)))

(defn memoize-clear
  "clears all results
 
   (memoize-clear -inc-)
   => '{(2) 3}"
  {:added "3.0"}
  [^Memoize mem]
  (let [cache (.cache mem)
        v (state/get cache)]
    (state/empty cache {})
    v))

(definvoke invoke-intern-memoize
  "creates a memoize form template for `definvoke`
 
   (invoke-intern-memoize :memoize 'hello {} '([x] x))"
  {:added "3.0"}
  [:method {:multi protocol.function/-invoke-intern
            :val :memoize}]
  ([_ name {:keys [function cache] :as config} body]
   (let [arglists (if (seq body)
                    (invoke/form-arglists body)
                    (or (:arglists config) ()))
         
         [cache-name cache-form]
         (if (or (map? cache)
                 (nil? cache))
           (let [prefix (or (:prefix cache) "+")
                 cache-name (symbol (str prefix name))
                 cache-type (or (:type cache) :atom)]
             [cache-name [`(cache/defcache ~cache-name
                             ~(str "cache for " *ns* "/" name)
                             [~cache-type ~(dissoc cache :type)])]])
           [cache []])
         
         [function-name function-form]
         (if (or (map? function)
                 (nil? function))
           (let [suffix (or (:suffix function) "-raw")
                 function-name (symbol (str name suffix))]
             [function-name [`(defn ~function-name
                                ~(str "helper function for " *ns* "/" name)
                                ~@body)]])
           [function []])

         body `(memoize ~function-name ~cache-name (var ~name))]
     `(do (declare ~name)
          ~@cache-form
          ~@function-form
          (doto (def ~name (memoize ~function-name ~cache-name (var ~name)))
            (alter-meta! merge
                         {:arglists ~arglists}
                         ~(dissoc config :function :cache)))
          (doto ~name
            (register-memoize)
            (memoize-clear))
          (var ~name)))))

(defmacro defmemoize
  "defines a cached function
 
   (defmemoize -dec-
     \"decrements\"
     {:added \"1.0\"}
     ([x] (dec x)))
   
   (-dec- 1) => 0
   @+-dec- => '{(1) 0}"
  {:added "3.0"}
  [name doc? attrs? & body]
  (let [[doc attrs & body]
        (macro/create-args (concat [doc? attrs?] body))]
    (invoke-intern-memoize :memoize name (assoc attrs :doc doc) body)))

(comment
  (invoke/definvoke hello
    [:memoize {:cache {:type :atom :tag "hello"}}]
    ([x y]
     (+ x y)))

  (invoke/definvoke hello
    [:memoize {:stable true}]
    ([x y]
     (+ x y)))
  
  +hello
  (hello 1 2)
  (hello 3 4))
