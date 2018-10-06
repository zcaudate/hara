(ns hara.function.base.hook-test
  (:use hara.test)
  (:require [hara.function.base.hook :refer :all]
            [hara.core.base.check :as check]))

^{:refer hara.function.base.hook/+original :added "3.0"}
(fact "cache for storing original functions which have been patched")

^{:refer hara.function.base.hook/patch :added "3.0"}
(fact "patches the existing function with a given one"

  (patch #'hara.core.base.check/double?
         (fn [x]
           (instance? Float x)))
  
  (hara.core.base.check/double? (float 1.0))
  => true)

^{:refer hara.function.base.hook/patched? :added "3.0"}
(fact "checks if an existing function has been patched"

  (patched? #'hara.core.base.check/double?)
  => true)

^{:refer hara.function.base.hook/unpatch :added "3.0"}
(fact "removes the patch creates for the var"

  (unpatch #'hara.core.base.check/double?)

  (hara.core.base.check/double? (float 1.0))
  => false)

^{:refer hara.function.base.hook/list-patched :added "3.0"}
(fact "returns all functions that have been patched"

  (patch #'hara.core.base.check/double?
         hara.core.base.check/double?)

  (-> (list-patched)
      (get #'hara.core.base.check/double?)
      boolean)
  => true)
