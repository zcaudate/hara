(ns hara.function.base.hook
  (:require [hara.state.base.cache :refer [defcache]]
            [hara.state.base.common :as state]
            [hara.state.base.impl :deps true]))

(defcache +original
  "cache for storing original functions which have been patched"
  {:added "3.0"}
  [:atom {:tag "hooks"
          :display (comp vec keys)}])

(defn patch
  "patches the existing function with a given one
 
   (patch #'hara.core.base.check/double?
          (fn [x]
            (instance? Float x)))
   
   (hara.core.base.check/double? (float 1.0))
   => true"
  {:added "3.0"}
  [var f]
  (when-not (get @+original var)
    (state/update +original assoc var @var))
  (doto var
    (alter-var-root (constantly f))))

(defn patched?
  "checks if an existing function has been patched
 
   (patched? #'hara.core.base.check/double?)
   => true"
  {:added "3.0"}
  [var]
  (boolean (get @+original var)))

(defn unpatch
  "removes the patch creates for the var
 
   (unpatch #'hara.core.base.check/double?)
 
   (hara.core.base.check/double? (float 1.0))
   => false"
  {:added "3.0"}
  [var]
  (when-let [f (get @+original var)]
    (alter-var-root var (constantly f))
    (state/update +original dissoc var)
    f))

(defn list-patched
  "returns all functions that have been patched
 
   (patch #'hara.core.base.check/double?
          hara.core.base.check/double?)
 
   (-> (list-patched)
       (get #'hara.core.base.check/double?)
       boolean)
   => true"
  {:added "3.0"}
  []
  (set (keys @+original)))
