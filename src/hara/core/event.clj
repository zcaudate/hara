(ns hara.core.event
  (:require [hara.core.event.handler :as handler]
            [hara.core.event.condition.data :as data]
            [hara.core.event.condition.manage :as manage]
            [hara.core.event.condition.raise :as raise]
            [hara.core.event.util :as util])
  (:import (clojure.lang Namespace Symbol)))

(defonce ^:dynamic *signal-manager* (atom (handler/manager)))

(defonce ^:dynamic *issue-managers* [])

(defonce ^:dynamic *issue-optmap* {})

(defn clear-listeners
  "empties all event listeners
 
   (clear-listeners)
   ;; all defined listeners will be cleared 
 "
  {:added "3.0"}
  []
  (reset! *signal-manager* (handler/manager)))

(defn list-listeners
  "shows all event listeners
 
   (deflistener -hello-listener- :msg
     [msg]
     (str \"recieved \" msg))
 
   (list-listeners)
   => (contains-in [{:id 'hara.core.event-test/-hello-listener-,
                     :checker :msg}])"
  {:added "3.0"}
  ([]
   (handler/list-handlers @*signal-manager*))
  ([checker]
   (handler/list-handlers @*signal-manager* checker)))

(defn install-listener
  "adds an event listener, `deflistener` can also be used
 
   (install-listener '-hello-
                     :msg
                     (fn [{:keys [msg]}]
                       (str \"recieved \" msg)))
 
   (list-listeners)
   => (contains-in [{:id '-hello-
                     :checker :msg}])"
  {:added "3.0"}
  [id checker handler]
  (swap! *signal-manager*
         handler/add-handler checker {:id id
                                     :fn handler}))

(defn uninstall-listener
  "uninstalls a global signal listener
 
   (uninstall-listener 'hara.core.event-test/-hello-)"
  {:added "3.0"}
  [id]
  (do (swap! *signal-manager* handler/remove-handler id)
      (if-let [nsp (and (symbol? id)
                        (.getNamespace ^Symbol id)
                        (Namespace/find (symbol (.getNamespace ^Symbol id))))]
        (do (.unmap ^Namespace nsp (symbol (.getName ^Symbol id)))
            nsp)
        id)))

(defmacro deflistener
  "installs a global signal listener
 
   (def -counts- (atom {}))
 
   (deflistener -count-listener- :log
     [msg]
     (swap! -counts- update-in [:counts] (fnil #(conj % (count msg)) [])))
 
   (signal [:log {:msg \"Hello World\"}])
 
   (signal [:log {:msg \"How are you?\"}])
 
   @-counts-
   => {:counts [11 12]}"
  {:added "3.0"}
  [name checker bindings & more]
  (let [sym    (str  (.getName *ns*) "/" name)
        cform  (util/checker-form checker)
        hform  (util/handler-form bindings more)]
    `(do (install-listener (symbol ~sym) ~cform ~hform)
         (def ~(symbol name) (symbol ~sym)))))

(defn signal
  "signals an event that is sent to, it does not do anything by itself
 
   (signal :anything) => ()
   
   (deflistener -hello- _
     e
     e)
   
   (signal :anything)
   => '({:id hara.core.event-test/-hello- :result {:anything true}})"
  {:added "3.0"}
  ([data]
   (signal data @*signal-manager*))
  ([data manager]
   (let [ndata   (handler/expand-data data)]
     (doall (for [handler (handler/match-handlers manager ndata)]
              {:id (:id handler)
               :result ((:fn handler) ndata)})))))

(defn continue
  "used within a manage form to continue on with a particular value
 
   (manage [1 2 (raise :error)]
           (on :error
               _
               (continue 3)))
   => [1 2 3]"
  {:added "3.0"}
  [body]
  {:type :continue :value body})

(defn default
  "used within either a raise or escalate form to specify the default option to take if no other options arise. 
 
   (raise :error
          (option :specify [a] a)
          (default :specify 3))
   => 3
 
   (manage
    (raise :error
           (option :specify [a] a)
           (default :specify 3))
    (on :error []
        (escalate :error
                  (default :specify 5))))
   => 5"
  {:added "3.0"}
  [& args]
  {:type :default :args args})

(defn choose
  "used within a manage form to definitively fail the system
 
   (manage (raise :error
                  (option :specify [a] a))
           (on :error
               _
               (choose :specify 42)))
   => 42"
  {:added "3.0"}
  [label & args]
  {:type :choose :label label :args args})

(defn fail
  "used within a manage form to definitively fail the system
 
   (manage (raise :error)
           (on :error
               _
               (fail :failed)))
   => (throws-info {:error true})"
  {:added "3.0"}
  ([] {:type :fail :data {}})
  ([data]
   {:type :fail :data data}))

(defmacro escalate
  "used within a manage form to add further data on an issue
 
   (manage [1 2 (raise :error)]
           (on :error
               _
               (escalate :escalated)))
   => (throws-info {:error true
                    :escalated true})"
  {:added "3.0"}
  [data & forms]
  (let [[data forms]
        (if (util/is-special-form :raise data)
          [nil (cons data forms)]
          [data forms])]
    `{:type :escalate
      :data ~data
      :options  ~(util/parse-option-forms forms)
      :default  ~(util/parse-default-form forms)}))

(defmacro raise
  "raise an issue, like throw but can be conditionally managed as well as automatically resolved:
 
   (raise  [:error {:msg \"A problem.\"}])
   => (throws-info {:error true
                    :msg \"A problem.\"})
 
   (raise [:error {:msg \"A resolvable problem\"}]
          (option :something [] 42)
          (default :something))
   => 42"
  {:added "3.0"}
  [content & [msg & forms]]
  (let [[msg forms] (if (util/is-special-form :raise msg)
                      ["" (cons msg forms)]
                      [msg forms])
        options (util/parse-option-forms forms)
        default (util/parse-default-form forms)]
    `(let [issue# (data/issue ~content ~msg ~options ~default)]
       (signal (assoc (:data issue#) :issue (:msg issue#)))
       (raise/raise-loop issue# *issue-managers*
                         (merge (:optmap issue#) *issue-optmap*)))))

(defmacro manage
  "manages a raised issue, like try but is continuable:
 
   (manage [1 2 (raise :error)]
           (on :error
               _
               3))
   => 3"
  {:added "3.0"}
  [& forms]
  (let [sp-fn           (fn [form] (util/is-special-form :manage form #{'finally 'catch}))
        body-forms      (vec (filter (complement sp-fn) forms))
        sp-forms        (filter sp-fn forms)
        id              (handler/new-id)
        options         (util/parse-option-forms sp-forms)
        on-handlers     (util/parse-on-handler-forms sp-forms)
        on-any-handlers (util/parse-on-any-handler-forms sp-forms)
        try-forms       (util/parse-try-forms sp-forms)
        optmap          (zipmap (keys options) (repeat id))]
    `(let [manager# (handler/manager ~id
                                    ~(vec (concat on-handlers on-any-handlers))
                                    ~options)]
       (binding [*issue-managers* (cons manager# *issue-managers*)
                 *issue-optmap*   (merge ~optmap *issue-optmap*)]
         (try
           (try
             ~@body-forms
             (catch clojure.lang.ExceptionInfo ~'ex
               (manage/manage-condition manager# ~'ex)))
           ~@try-forms)))))

(defmacro with-temp-listener
  "used for isolating and testing signaling
 
   (with-temp-listener [{:id string?}
                        (fn [e] \"world\")]
     (signal {:id \"hello\"}))
   => '({:result \"world\", :id :temp})"
  {:added "3.0"}
  [[checker handler] & body]
  `(binding [*signal-manager* (atom (handler/manager))]
     (install-listener :temp ~checker ~handler)
     ~@body))
