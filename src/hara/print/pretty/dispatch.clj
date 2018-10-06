(ns hara.print.pretty.dispatch
  "Dispatch functions take a `Class` argument and return the looked-up value.
  This provides similar functionality to Clojure's protocols, but operates over
  locally-constructed logic rather than using a global dispatch table.

  A simple example is a map from classes to values, which can be used directly
  as a lookup function."
  (:require [hara.string :as string]
            [hara.core.base.inheritance :as inheritance]))

;; ## Logical Dispatch

(defn chained-lookup
  "chains two or more lookups together
 
   (chained-lookup
    (inheritance-lookup printer/clojure-handlers)
   (inheritance-lookup printer/java-handlers))"
  {:added "3.0"}
  ([dispatchers]
   {:pre [(sequential? dispatchers)]}
   (let [candidates (remove nil? dispatchers)]
     (when (empty? candidates)
       (throw (IllegalArgumentException.
                "chained-lookup must be provided at least one dispatch function to try.")))
     (if (= 1 (count candidates))
       (first candidates)
       (fn lookup
         [t]
         (some #(% t) candidates)))))
  ([a b & more]
   (chained-lookup (list* a b more))))

(defn inheritance-lookup
  "checks if items inherit from the handlers
 
   ((inheritance-lookup printer/clojure-handlers)
    clojure.lang.Atom)
   => fn?
 
   ((inheritance-lookup printer/clojure-handlers)
    String)
   => nil"
  {:added "3.0"}
  [dispatch]
  (fn lookup
    [t]
    (or
      ; Look up base class and ancestors up to the base class.
      (some dispatch (inheritance/ancestor-list t))

      ; Look up interfaces and collect candidates.
      (let [candidates (remove (comp nil? first)
                               (map (juxt dispatch identity)
                                    (inheritance/all-ancestors t)))]
        (case (count candidates)
          0 nil
          1 (ffirst candidates)
          (throw (RuntimeException.
                   (format "%d candidates found for interfaces on dispatch type %s: %s"
                           (count candidates) t (string/joinr ", " (map second candidates)))))))

      ; Look up Object base class.
      (dispatch Object))))
