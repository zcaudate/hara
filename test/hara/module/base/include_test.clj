(ns hara.module.base.include-test
  (:use hara.test)
  (:require [hara.module.base.include :refer :all]
            [hara.module.base.link :as link]))

^{:refer hara.module.base.include/include :added "3.0"}
(fact "Imports all or a selection of vars from one namespace to the current one."

  (include (hara.core.base.check atom? long?))
  (eval '(long? 1))  => true
  (eval '(atom? 1)) => false

  (include
   {:fn (fn [ns sym var]
          (intern ns sym (fn [x] (@var (bigint x)))))}
   (hara.core.base.check bigint?))
  (eval '(bigint? 1)) => true

  (include
   {:ns 'clojure.core}
   (hara.core.base.check bigint?))
  => [#'clojure.core/bigint?]

  ^:hidden
  (doseq [k (keys (ns-interns *ns*))]
    (ns-unmap *ns* k))
  
  (ns-unmap 'clojure.core 'bigint?))

^{:refer hara.module.base.include/link-sources :added "3.0"}
(fact "helper function for `link`")

^{:refer hara.module.base.include/link :added "3.0"}
(fact "creates links to vars that can be resolved in a controllable manner"
  
  (link
   {:resolve :lazy}
   (hara.core.base.check atom?))
  
  (meta #'atom?) => {:name 'atom?, :ns *ns*}^:hidden
  
  (link
   {:resolve :metadata}
   (hara.core.base.check atom?))
  
  (meta #'atom?) => {:name 'atom? :ns *ns* :arglists '([obj])}

  (binding [link/*bind-root* true]
    (link
     {:resolve :resolve}
     (hara.core.base.check atom?)))
  atom? => hara.core.base.check/atom?

  (link
   {:resolve :verify}
   (hara.core.base.check error))
  => throws)
