(ns hara.state.base.cache-test
  (:use hara.test)
  (:require [hara.state.base.cache :refer :all]))

^{:refer hara.state.base.cache/cache-string :added "3.0"}
(fact "string represetation of a cache"

  (str (cache {:a 1 :b 2} {:tag "stuff"
                           :display keys}))
  "#stuff:atom(:a :b)")

^{:refer hara.state.base.cache/cache-invoke :added "3.0"}
(fact "helper function for invoking the cache")

^{:refer hara.state.base.cache/Cache :added "3.0"}
(fact "returns a `hara.state.base.cache.Cache` instance"

  (->Cache (atom {}) {}))

^{:refer hara.state.base.cache/cache :added "3.0"}
(fact "creates a cache with the following properties"
  
  (-> (cache {} {:tag "stuff"
                 :type :ref})
      (.state))
  => clojure.lang.Ref
  
  (str (cache {:a 1 :b 2} {:type :agent}))
  => "#cache:agent{:a 1, :b 2}")

^{:refer hara.state.base.cache/defcache :added "3.0"}
(fact "defines a cache"

  (defcache -a-)
  (.state -a-)
  => clojure.lang.Atom
  
  (defcache -b-
    [:volatile {:tag "hello"}])
  (.state -b-)
  => volatile?)
