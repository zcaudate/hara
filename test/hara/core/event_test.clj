(ns hara.core.event-test
  (:use hara.test)
  (:require [hara.core.event :refer :all]))

^{:refer hara.core.event/clear-listeners :added "3.0"}
(comment "empties all event listeners"

  (clear-listeners)
  ;; all defined listeners will be cleared 
)

^{:refer hara.core.event/list-listeners :added "3.0"}
(fact "shows all event listeners"

  (deflistener -hello-listener- :msg
    [msg]
    (str "recieved " msg))

  (list-listeners)
  => (contains-in [{:id 'hara.core.event-test/-hello-listener-,
                    :checker :msg}]))

^{:refer hara.core.event/install-listener :added "3.0"}
(fact "adds an event listener, `deflistener` can also be used"

  (install-listener '-hello-
                    :msg
                    (fn [{:keys [msg]}]
                      (str "recieved " msg)))

  (list-listeners)
  => (contains-in [{:id '-hello-
                    :checker :msg}]))

^{:refer hara.core.event/uninstall-listener :added "3.0"}
(fact "uninstalls a global signal listener"

  (uninstall-listener 'hara.core.event-test/-hello-))

^{:refer hara.core.event/deflistener :added "3.0"}
(fact "installs a global signal listener"

  (def -counts- (atom {}))

  (deflistener -count-listener- :log
    [msg]
    (swap! -counts- update-in [:counts] (fnil #(conj % (count msg)) [])))

  (signal [:log {:msg "Hello World"}])

  (signal [:log {:msg "How are you?"}])

  @-counts-
  => {:counts [11 12]})

^{:refer hara.core.event/signal :added "3.0"}
(fact "signals an event that is sent to, it does not do anything by itself"

  (signal :anything) => ()
  
  (deflistener -hello- _
    e
    e)
  
  (signal :anything)
  => '({:id hara.core.event-test/-hello- :result {:anything true}}))

^{:refer hara.core.event/continue :added "3.0"}
(fact "used within a manage form to continue on with a particular value"

  (manage [1 2 (raise :error)]
          (on :error
              _
              (continue 3)))
  => [1 2 3])

^{:refer hara.core.event/default :added "3.0"}
(fact "used within either a raise or escalate form to specify the default option to take if no other options arise. "

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
  => 5)

^{:refer hara.core.event/choose :added "3.0"}
(fact "used within a manage form to definitively fail the system"

  (manage (raise :error
                 (option :specify [a] a))
          (on :error
              _
              (choose :specify 42)))
  => 42)

^{:refer hara.core.event/fail :added "3.0"}
(fact "used within a manage form to definitively fail the system"

  (manage (raise :error)
          (on :error
              _
              (fail :failed)))
  => (throws-info {:error true}))

^{:refer hara.core.event/escalate :added "3.0"}
(fact "used within a manage form to add further data on an issue"

  (manage [1 2 (raise :error)]
          (on :error
              _
              (escalate :escalated)))
  => (throws-info {:error true
                   :escalated true}))

^{:refer hara.core.event/raise :added "3.0"}
(fact "raise an issue, like throw but can be conditionally managed as well as automatically resolved:"

  (raise  [:error {:msg "A problem."}])
  => (throws-info {:error true
                   :msg "A problem."})

  (raise [:error {:msg "A resolvable problem"}]
         (option :something [] 42)
         (default :something))
  => 42)

^{:refer hara.core.event/manage :added "3.0"}
(fact "manages a raised issue, like try but is continuable:"

  (manage [1 2 (raise :error)]
          (on :error
              _
              3))
  => 3)

^{:refer hara.core.event/with-temp-listener :added "3.0"}
(fact "used for isolating and testing signaling"

  (with-temp-listener [{:id string?}
                       (fn [e] "world")]
    (signal {:id "hello"}))
  => '({:result "world", :id :temp}))
