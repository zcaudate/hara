(ns hara.function.base.memoize-test
  (:use hara.test)
  (:require [hara.function.base.memoize :refer :all]
            [hara.core.base.check :as check])
  (:refer-clojure :exclude [memoize]))

^{:refer hara.function.base.memoize/+registry :added "3.0"}
(fact "global registry for memoize functions")

^{:refer hara.function.base.memoize/Memoize :added "3.0"}
(fact "creates an object that holds its own cache"

  (declare -mem-)
  (def -mem-
    (->Memoize + nil (atom {}) #'-mem- +registry (volatile! true))))

^{:refer hara.function.base.memoize/memoize :added "3.0"}
(fact "caches the result of a function"
  (ns-unmap *ns* '+-inc-)
  (ns-unmap *ns* '-inc-)
  (def +-inc- (atom {}))
  (declare -inc-)
  (def -inc-  (memoize inc +-inc- #'-inc-))
  
  (-inc- 1) => 2
  (-inc- 2) => 3)

^{:refer hara.function.base.memoize/register-memoize :added "3.0"}
(fact "registers the memoize function"
  
  (register-memoize -inc-))

^{:refer hara.function.base.memoize/deregister-memoize :added "3.0"}
(fact "deregisters the memoize function"

  (deregister-memoize -inc-))

^{:refer hara.function.base.memoize/registered-memoizes :added "3.0"}
(fact "lists all registered memoizes"
  
  (registered-memoizes))

^{:refer hara.function.base.memoize/registered-memoize? :added "3.0"}
(fact "checks if a memoize function is registered"

  (registered-memoize? -mem-)
  => false)

^{:refer hara.function.base.memoize/memoize-status :added "3.0"}
(fact "returns the status of the object"

  (memoize-status -inc-)
  => :enabled)

^{:refer hara.function.base.memoize/memoize-display :added "3.0"}
(fact "formats the memoize object"

  (def +-plus- (atom {}))
  (declare -plus-)
  (def -plus- (memoize + +-plus- #'-plus-))
  (memoize-display -plus-)
  => (contains {:status :enabled, :registered false, :items number?})
  ;; {:fn +, :cache #atom {(1 1) 2}}
  )

^{:refer hara.function.base.memoize/memoize-disable :added "3.0"}
(fact "disables the usage of the cache"

  @(memoize-disable -inc-)
  => :disabled)

^{:refer hara.function.base.memoize/memoize-disabled? :added "3.0"}
(fact "checks if the memoized function is disabled"

  (memoize-disabled? -inc-)
  => true)

^{:refer hara.function.base.memoize/memoize-enable :added "3.0"}
(fact "enables the usage of the cache"
  
  @(memoize-enable -inc-)
  => :enabled)

^{:refer hara.function.base.memoize/memoize-enabled? :added "3.0"}
(fact "checks if the memoized function is disabled"

  (memoize-enabled? -inc-)
  => true)

^{:refer hara.function.base.memoize/memoize-invoke :added "3.0"}
(fact "invokes the function with arguments"
  
  (memoize-invoke -plus- 1 2 3)
  => 6)

^{:refer hara.function.base.memoize/memoize-remove :added "3.0"}
(fact "removes a cached result"

  (memoize-remove -inc- 1)
  => 2)

^{:refer hara.function.base.memoize/memoize-clear :added "3.0"}
(fact "clears all results"

  (memoize-clear -inc-)
  => '{(2) 3})


^{:refer hara.function.base.memoize/invoke-intern-memoize :added "3.0"}
(fact "creates a memoize form template for `definvoke`"

  (invoke-intern-memoize :memoize 'hello {} '([x] x))^:hidden
  => '(do (clojure.core/declare hello)

          (hara.state.base.cache/defcache +hello "cache for hara.function.base.memoize-test/hello"
            [:atom nil])
          
          (clojure.core/defn hello-raw "helper function for hara.function.base.memoize-test/hello" [x] x)
          (clojure.core/doto
              (def hello (hara.function.base.memoize/memoize hello-raw +hello (var hello)))
            (clojure.core/alter-meta! clojure.core/merge {:arglists (quote ([x]))} {}))
          (clojure.core/doto hello
            (hara.function.base.memoize/register-memoize)
            (hara.function.base.memoize/memoize-clear))
          (var hello)))

^{:refer hara.function.base.memoize/defmemoize :added "3.0"}
(fact "defines a cached function"

  (defmemoize -dec-
    "decrements"
    {:added "1.0"}
    ([x] (dec x)))
  
  (-dec- 1) => 0
  @+-dec- => '{(1) 0})

(comment
  (hara.code/incomplete)
  (./code:scaffold)
  (hara.code/arrange)
  (hara.code/import))

(comment
  (defmemoize -inc-
    [s]
    +)
  
  (-inc- 1 2 3 4)
  (-inc- 1 2 3)
  (-inc- 1 2)
  +-inc-
  (memoize-deregister -inc-)
  (memoize-disable -inc-)
  (+registry)
  
  (def a (var oeuoe))
  (hara.code/import))
