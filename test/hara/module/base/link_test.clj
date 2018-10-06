(ns hara.module.base.link-test
  (:use hara.test)
  (:require [hara.module.base.link :refer :all]))

^{:refer hara.module.base.link/+registry :added "3.0"}
(fact "registry for all links")

^{:refer hara.module.base.link/ns-metadata :added "3.0"}
(fact "provides source metadata support for links"

  (ns-metadata 'hara.core.base.check)
  => (contains '{ideref?  {:arglists ([obj])}
                 bigint?  {:arglists ([x])}
                 boolean? {:arglists ([x])}}))

^{:refer hara.module.base.link/Link :added "3.0"}
(fact "defines a Link type"^:hidden

  (declare -lnk-)
  (def -lnk- (->Link {:ns 'hara.core.base.check
                      :name 'bytes?}
                     #'-lnk-
                     identity
                     +registry)))

^{:refer hara.module.base.link/link :added "3.0"}
(fact "creates a link"
  (ns-unmap *ns* '-byte0?-)
  (declare -byte0?-)

  (link {:ns 'hara.core.base.check :name 'bytes?}
        #'-byte0?-)
  ;; #link{:source hara.core.base.check/bytes?, :bound false, :status :resolved, :synced false, :registered false}
  => link?)

^{:refer hara.module.base.link/link? :added "3.0"}
(fact "checks if object is a link"

  (link? -lnk-)
  => true)

^{:refer hara.module.base.link/register-link :added "3.0"}
(fact "adds link to global registry"

  (register-link -lnk-)
  => #'hara.module.base.link-test/-lnk-

  (registered-link? -lnk-)
  => true)

^{:refer hara.module.base.link/deregister-link :added "3.0"}
(fact "removes a link from global registry"

  (deregister-link -lnk-)
  => #'hara.module.base.link-test/-lnk-

  (registered-link? -lnk-)
  => false)

^{:refer hara.module.base.link/registered-link? :added "3.0"}
(fact "checks if a link is registered"

  (registered-link? -lnk-)
  => false)

^{:refer hara.module.base.link/registered-links :added "3.0"}
(fact "returns all registered links"

  (register-link -lnk-)
  => #'hara.module.base.link-test/-lnk-
  
  (registered-links)
  => (contains [(exactly #'-lnk-)]))

^{:refer hara.module.base.link/unresolved-links :added "3.0"}
(fact "returns all unresolved links"

  (unresolved-links))

^{:refer hara.module.base.link/resolve-links :added "3.0"}
(comment "resolves all unresolved links in a background thread"

  (resolve-links))

^{:refer hara.module.base.link/link-bound? :added "3.0"}
(fact "checks if the var of the link has been bound, should be true"

  (link-bound? -lnk-)
  => true
  
  (link-bound? (->Link {:ns 'hara.core.base.check :name 'bytes?}
                       nil
                       nil
                       nil))
  => false)

^{:refer hara.module.base.link/link-status :added "3.0"}
(fact "lists the current status of the link"

  (link-status (->Link {:ns 'hara.core.base.check :name 'bytes?}
                       nil
                       nil
                       nil))
  => :resolved^:hidden

  (link-status (->Link {:ns 'hara.core.base.check :name 'error}
                       nil
                       nil
                       nil))
  => :source-var-not-found

  (link-status (->Link {:ns 'error :name 'error}
                       nil
                       nil
                       nil))
  => :unresolved

  (link-status (->Link {:ns 'hara.module.base.link-test :name '-lnk-}
                       nil
                       nil
                       nil))
  => :linked)

^{:refer hara.module.base.link/find-source-var :added "3.0"}
(fact "finds the source var in the link"

  (find-source-var (->Link {:ns 'hara.core.base.check :name 'bytes?}
                           nil
                           nil
                           nil))
  => #'hara.core.base.check/bytes?)

^{:refer hara.module.base.link/link-synced? :added "3.0"}
(fact "checks if the source and sink have the same value"

  (def -bytes?- hara.core.base.check/bytes?)

  (link-synced? (->Link {:ns 'hara.core.base.check :name 'bytes?}
                        #'-bytes?-
                        nil
                        nil))
  => true)

^{:refer hara.module.base.link/link-selfied? :added "3.0"}
(fact "checks if the source and sink have the same value"

  (declare -selfied-)
  (def -selfied- (link {:name '-selfied-}
                       #'-selfied-
                       ))
  
  (link-selfied? -selfied-)
  => true)

^{:refer hara.module.base.link/link-display :added "3.0"}
(fact "displays the link"

  (link-display -selfied-)
  => {:source 'hara.module.base.link-test/-selfied-
      :bound true,
      :status :linked,
      :synced false,
      :registered false})

^{:refer hara.module.base.link/transform-metadata :added "3.0"}
(fact "helper function for adding metadata to vars")

^{:refer hara.module.base.link/bind-metadata :added "3.0"}
(fact "retrievess the metadata of a function from source code"

  (declare -metadata-)
  (def -metadata- (link {:ns 'hara.core.base.check :name 'bytes?}
                        #'-metadata-))
  
  (bind-metadata -metadata-)
  
  (-> (meta #'-metadata-)
      :arglists)
  => '([x]))

^{:refer hara.module.base.link/bind-source :added "3.0"}
(fact "retrieves the source var"

  (declare -source-)
  (def -source- (link {} #'-source-))
  
  (bind-source (.sink -source-) #'hara.core.base.check/bytes? (.transform -source-))
  => hara.core.base.check/bytes?)

^{:refer hara.module.base.link/bind-resolve :added "3.0"}
(fact "binds a link or a series of links"
  
  (deflink -byte0?- hara.core.base.check/byte?)
  
  (deflink -byte1?- -byte0?-)
  
  (deflink -byte2?- -byte1?-)
  
  (binding [*bind-root* true]
    (bind-resolve -byte2?-))
  (fn? -byte2?-) => true
  (fn? -byte1?-) => true
  (fn? -byte0?-) => true)

^{:refer hara.module.base.link/bind-preempt :added "3.0"}
(fact "retrieves the source var if available"

  (deflink -byte0?- hara.core.base.check/byte?)

  (binding [*bind-root* true]
    (bind-preempt -byte0?-))
  => hara.core.base.check/byte?)

^{:refer hara.module.base.link/bind-verify :added "3.0"}
(fact "retrieves the source var if available"

  (deflink -byte0?- hara.core.base.check/byte?)
  
  (binding [*bind-root* true]
    (bind-verify -byte0?-))
  => (exactly #'hara.module.base.link-test/-byte0?-))

^{:refer hara.module.base.link/bind-init :added "3.0"}
(fact "automatically loads the var if possible"

  (deflink -byte0?- hara.core.base.check/byte?)
  
  (binding [*bind-root* true]
    (bind-init -byte0?- :auto))
  => hara.core.base.check/byte?

  (deflink -to-element0- hara.object.element/to-element)

  (binding [*bind-root* true]
    (bind-init -to-element0- :resolve))
  => hara.object.element/to-element)

^{:refer hara.module.base.link/link-invoke :added "3.0"}
(fact "invokes a link"

  (deflink -byte0?- hara.core.base.check/byte?)
  
  (deflink -byte1?- -byte0?-)
  
  (deflink -byte2?- -byte1?-)
  
  (-byte2?- (byte 1)) => true)

^{:refer hara.module.base.link/intern-link :added "3.0"}
(fact "creates a registers a link"

  (intern-link '-byte0?- {:ns 'hara.core.base.check :name 'bytes?})
  ;;#link{:source hara.core.base.check/bytes?, :bound true, :status :resolved, :synced false, :registered true}
  => link?)

^{:refer hara.module.base.link/invoke-intern-link :added "3.0"}
(fact "creates a set of forms constructing a link"

  (invoke-intern-link :link '-link- {:ns 'hara.core.base.check :name 'bytes?} nil)^:hidden
  => '(do (def -link- (hara.module.base.link/intern-link
                       (quote hara.module.base.link-test)
                       (quote -link-)
                       {:ns (quote hara.core.base.check),
                        :name (quote bytes?)}
                       nil
                       (clojure.core/or nil hara.module.base.link/+registry)))
          (hara.module.base.link/bind-init -link- :auto)
          (clojure.core/doto (var -link-)
            (clojure.core/alter-meta! clojure.core/merge {}))))

^{:refer hara.module.base.link/deflink :added "3.0"}
(fact "defines a link"

  (deflink -byte3?- hara.core.base.check/byte?)
  @-byte3?-
  => hara.core.base.check/byte?
    
  (deflink -atom?- hara.core.base.check/atom?)
  @-atom?-
  => hara.core.base.check/atom?)
