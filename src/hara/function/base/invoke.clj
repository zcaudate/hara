(ns hara.function.base.invoke
  (:require [hara.function.base.arg :as arg]
            [hara.function.base.macro :as macro]
            [hara.function.base.multi :as multi]
            [hara.protocol.function :as protocol.function]
            [clojure.core :as clojure])
  (:refer-clojure :exclude [fn]))

(def +default-packages+
  '{:fn        {:ns clojure.core}                                  
    :multi     {:ns clojure.core}
    :method    {:ns clojure.core}
    :dynamic   {:ns clojure.core}
    :protocol  {:ns clojure.core}
    :compose   {:ns hara.function.base.macro}
    :lookup    {:ns hara.function.base.macro}
    :memoize   {:ns hara.function.base.memoize}

    :link      {:ns hara.module.base.link}
    :element   {:ns hara.object.query}
    :procedure {:ns hara.function.procedure}
    :task      {:ns hara.function.task}
    :scala     {:ns hara.lang.scala}
    :opencl    {:ns hara.lib.opencl}})

(defn form-arglists
  "returns the arglists of a form
 
   (form-arglists '([x] x))
   => '(quote ([x]))
 
   (form-arglists '(([x] x) ([x y] (+ x y))))
   => '(quote ([x] [x y]))"
  {:added "3.0"}
  [body]
  (cond (list? (first body))
        `(quote ~(map first body))
        
        (vector? (first body))
        `(quote ~(list (first body)))
        
        :else
        (throw (ex-info "Cannot find arglists." {:body body}))))

(defn invoke-intern-method
  "creates a `:method` form, similar to `defmethod`
 
   (defmulti -hello-multi- identity)
   (invoke-intern-method '-hello-method-
                         {:multi '-hello-multi-
                          :val :apple}
                         '([x] x))"
  {:added "3.0"}
  [name {:keys [multi val] :as config} body]
  (let [arglists (form-arglists body)
        [mm-name method-name] (cond (nil? multi)
                                    (if (resolve name)
                                      [name name]
                                      (throw (ex-info "Cannot resolve multimethod." {:name name})))

                                    :else
                                    (if (resolve multi)
                                      [multi name]
                                      (throw (ex-info "Cannot resolve multimethod." {:name multi}))))]
    (if-not (= mm-name method-name)
      `(let [~'v (doto (def ~method-name (clojure/fn ~method-name ~@body))
                   (alter-meta! merge ~config {:arglists ~arglists}))]
         [(multi/multi-add ~mm-name ~val ~method-name)
          ~'v])
      `[(multi/multi-add ~mm-name ~val (clojure/fn ~@body))
        nil])))

(defmethod protocol.function/-invoke-intern :method
  [_ name {:keys [multi val] :as config} body]
  (invoke-intern-method name config body))

(defn resolve-method
  "resolves a package related to a label
 
   (resolve-method protocol.function/-invoke-intern
                   protocol.function/-invoke-package
                   :fn
                   +default-packages+)
   => nil"
  {:added "3.0"}
  [mmethod pkgmethod label lookup]
  (let [exists? (multi/multi-get mmethod label)]
    (when (not exists?)
      (let [{:keys [ns]} (or (get lookup label)
                             (if (multi/multi-get pkgmethod label)
                               (pkgmethod label)))]
        (if ns
          (require ns)
          (throw (ex-info "resolve package does not exist" {:label label})))))))

(defn invoke-intern
  "main function to call for `definvoke`
 
   (invoke-intern :method
                  '-hello-method-
                 {:multi '-hello-multi-
                   :val :apple}
                  '([x] x))"
  {:added "3.0"}
  [label name config body]
  (let [_ (resolve-method protocol.function/-invoke-intern
                          protocol.function/-invoke-package
                          label
                          +default-packages+)]
    (protocol.function/-invoke-intern label name config body)))

(defmacro definvoke
  "customisable invocation forms
 
   (definvoke -another-
     [:compose {:val (partial + 10)
               :arglists '([& more])}])"
  {:added "3.0"}
  [name doc? & [attrs? & [params & body :as more]]]
  (let [[doc attrs [label {:keys [refresh stable] :as config}] & body]
        (macro/create-args (concat [doc? attrs?] more))]
    (if (or refresh
            (not (true? stable))
            (not (resolve name)))
      (invoke-intern label name (assoc (merge config attrs) :doc doc) body))))

;; Using definvoke 

(definvoke invoke-intern-fn
  "method body for `:fn` invoke
 
   (invoke-intern-fn :fn '-fn-form- {} '([x] x))"
  {:added "3.0"}
  [:method {:multi protocol.function/-invoke-intern
            :val :fn}]
  ([_ name config body]
   (let [arglists (form-arglists body)]
     `(do (def ~name (clojure/fn ~(symbol (str name)) ~@body))
          (doto (var ~name)
            (alter-meta! merge ~config {:arglists ~arglists}))))))

(definvoke invoke-intern-dynamic
  {:added "3.0"}
  [:method {:multi protocol.function/-invoke-intern
            :val :dynamic}]
  ([_ name config _]
   (let [earmuff  (symbol (str "*" name "*"))]
     `(do
        (intern (quote ~(.getName *ns*))
                (quote ~earmuff)
                ~(:val config))
        (defn ~name
          ([] ~earmuff)
          ([~'v]
           (alter-var-root (var ~earmuff) (fn [~'_] ~'v))))
        [(doto (var ~earmuff)
           (alter-meta! assoc :dynamic true))
         (doto (var ~name)
           (alter-meta! merge ~config))]))))

(definvoke invoke-intern-multi
  "method body for `:multi` form
 
   (invoke-intern-multi :multi '-multi-form- {} '([x] x))"
  {:added "3.0"}
  [:method {:multi protocol.function/-invoke-intern
            :val :multi}]
  ([_ name {:keys [refresh default hierarchy] :as config} body]
   (let [[dispatch arglists]
         (if (seq body)
           [`(clojure/fn ~(symbol (str name)) ~@body)
            (form-arglists body)]
           [(:dispatch config)
            (or (:arglists config) ())])
         options   (select-keys config [:default :hierarchy])
         default   (or default :default)
         hierarchy (or hierarchy '(var clojure.core/global-hierarchy))]
     `(do (@#'clojure.core/check-valid-options ~options :default :hierarchy)
          (let [~'v (def ~name)]
            (if (or (not (and (.hasRoot ~'v)
                              (instance? clojure.lang.MultiFn (deref ~'v))))
                    ~refresh)
              (doto (def ~name
                      (new clojure.lang.MultiFn ~(.getName name) ~dispatch ~default ~hierarchy))
                (alter-meta! merge ~config {:arglists ~arglists}))))))))

(definvoke invoke-intern-lookup
  "method body for `:lookup` form
 
   (invoke-intern-lookup :lookup
                         '-lookup-form-
                        {:table {:a 1 :b 2}} nil)"
  {:added "3.0"}
  [:method {:multi protocol.function/-invoke-intern
            :val :lookup}]
  ([_ name {:keys [table in out not-found arglists] :as config} _]
   (let [function? (or in out not-found)
         arglists (or arglists ''([k]))]
     `(doto (def ~name ~(if function?
                          `(macro/lookup ~table ~(select-keys config [:in :out :not-found]))
                          table))
        (alter-meta! merge ~(-> config
                               (dissoc :table :in :out :not-found))
                     {:arglists ~arglists})))))

(definvoke invoke-intern-compose
  "method body for `:compose` form
 
   (invoke-intern-compose :compose
                          '-compose-form-
                         {:val '(partial + 1 2)
                           :arglists ''([& more])} nil)"
  {:added "3.0"}
  [:method {:multi protocol.function/-invoke-intern
            :val :compose}]
  ([_ name {:keys [val arglists] :as config} _]
   `(doto (def ~name ~val)
      (alter-meta! merge ~(dissoc config :val)))))

(definvoke invoke-intern-macro
  "method body for `:macro` form
 
   (defn -macro-fn- [] '[(fn [x] x)
                         {:arglists ([x])}])
   
   (invoke-intern-macro :macro
                        '-macro-form-
                        {:fn '-macro-fn-
                         :args []}
                        nil)
   => '(clojure.core/doto
           (def -macro-form- (fn [x] x))
         (clojure.core/alter-meta! clojure.core/merge {} {:arglists ([x])}))"
  {:added "3.0"}
  [:method {:multi protocol.function/-invoke-intern
            :val :macro}]
  ([_ name {:keys [args fn] :as config} _]
   (let [func (resolve fn)
         [val meta] (apply func args)]
     `(doto (def ~name ~val)
        (alter-meta! merge ~(dissoc config :fn :args) ~meta)))))

(def +default-fn+
  '{:function   {:ns hara.function.base.native}
    :predicate  {:ns hara.function.base.native}
    :scala      {:ns hara.lang.scala.function}})

(defn fn-body
  "creates the anonymous function body
 
   (fn-body :function '([x] x))
   => '(clojure.core/reify java.util.function.Function
         (toString [_] \"([x] x)\")
         (apply [_ x] x))"
  {:added "3.0"}
  [label body]
  (let [_ (resolve-method protocol.function/-fn-body
                          protocol.function/-fn-package
                          label
                          +default-fn+)]
    (protocol.function/-fn-body label body)))

(definvoke fn-body-clojure
  "creates the anonymous function body for a clojure fn
   
   (fn-body-clojure '([x] x))
   => '(clojure.core/fn [x] x)"
  {:added "3.0"}
  [:method {:multi protocol.function/-fn-body
            :val :clojure}]
  ([body] (fn-body-clojure nil body))
  ([_ body] `(clojure/fn ~@body)))

(defmacro fn
  "macro for an extensible `fn` form
 
   (fn [x] x)
   => fn?
 
   ^{:type :function}
   (fn [x] x)
   => java.util.function.Function
 
   ^{:type :predicate}
   (fn [x] true)
   => java.util.function.Predicate"
  {:added "3.0"}
  [& body]
  (let [type (or (:type (meta &form))
                 :clojure)]
    (protocol.function/-fn-body type body)))
