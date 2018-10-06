(ns hara.core.base.watch
  (:require [hara.core.base.shorthand :as hand]
            [hara.function :as fn]
            [hara.protocol.watch :as protocol.watch])
  (:refer-clojure :exclude [list remove set]))

(defn wrap-select
  "enables operating on a given key
 
   ((watch/wrap-select (fn [_ _ p n]
                         (+ p n))
                       :a)
    nil nil {:a 2} {:a 1})
   => 3"
  {:added "3.0"}
  [f sel]
  (fn [& args]
    (let [[n p & more] (reverse args)
          pv    (hand/get-> p sel)
          nv    (hand/get-> n sel)]
      (apply f (->> more (cons pv) (cons nv) reverse)))))

(defn wrap-diff
  "only functions when the inputs are different
 
   ((watch/wrap-diff (fn [_ _ p n]
                       (+ p n)))
    nil nil 2 2)
   => 2
 
   ((watch/wrap-diff (fn [_ _ p n]
                       (+ p n)))
    nil nil 2 3)
   => 5"
  {:added "3.0"}
  [f]
  (fn [& args]
    (let [[nv pv & more] (reverse args)]
      (cond (and (nil? pv) (nil? nv))
            nil

            (or (nil? pv) (nil? nv)
                (not (= pv nv)))
            (apply f args)

            :else pv))))

(defn wrap-mode
  "changes how the function is run, :sync (same thread) and :async (new thread)"
  {:added "3.0"}
  [f mode]
  (fn [& args]
    (case mode
      :sync  (apply f args)
      :async (future (apply f args)))))

(defn wrap-suppress
  "runs the function but if errors, does not throw exception"
  {:added "3.0"}
  [f]
  (fn [& args]
    (try
      (apply f args)
      (catch Throwable t))))

(defn process-options
  "helper function for building a watch function
 
   (watch/process-options {:supress true
                           :diff true
                          :mode :async
                           :args 2}
                          (fn [_ _] 1))"
  {:added "3.0"}
  [opts f]
  (let [_ (if (:args opts)
            (fn/arg-check f (:args opts)))
        f (if (:diff opts)
            (wrap-diff f)
            f)
        f (if-let [sel (:select opts)]
            (wrap-select f sel)
            f)
        f (if (:suppress opts)
            (wrap-suppress f)
            f)
        f (wrap-mode f (or (:mode opts) :sync))]
    f))

(defn add
  "Adds a watch function through the IWatch protocol
 
   (def subject (atom nil))
   (def observer (atom nil))
 
   (watch/add subject :follow
              (fn [_ _ _ n]
                (reset! observer n)))
   (reset! subject 1)
   @observer => 1
 
   ;; options can be given to either transform
   ;; the current input as well as to only execute
   ;; the callback if there is a difference.
 
   (def subject  (atom {:a 1 :b 2}))
   (def observer (atom nil))
 
   (watch/add subject :clone
              (fn [_ _ p n] (reset! observer n))
              {:select :b
               :diff true})
 
   (swap! subject assoc :a 0) ;; change in :a does not
   @observer => nil           ;; affect watch
 
 
   (swap! subject assoc :b 1) ;; change in :b does
   @observer => 1"
  {:added "3.0"}
  ([obj f] (add obj nil f nil))
  ([obj k f] (add obj k f nil))
  ([obj k f opts]
   (protocol.watch/-add-watch obj k f opts)))

(defn list
  "Lists watch functions through the IWatch protocol
 
   (def subject   (atom nil))
   (do (watch/add subject :a (fn [_ _ _ n]))
       (watch/add subject :b (fn [_ _ _ n]))
       (watch/list subject))
   => (contains {:a fn? :b fn?})"
  {:added "3.0"}
  ([obj] (list obj nil))
  ([obj opts] (protocol.watch/-list-watch obj opts)))

(defn remove
  "Removes watch function through the IWatch protocol
 
   (def subject   (atom nil))
   (do (watch/add subject :a (fn [_ _ _ n]))
       (watch/add subject :b (fn [_ _ _ n]))
       (watch/remove subject :b)
       (watch/list subject))
   => (contains {:a fn?})"
  {:added "3.0"}
  ([obj]   (remove obj nil nil))
  ([obj k] (remove obj k nil))
  ([obj k opts] (protocol.watch/-remove-watch obj k opts)))

(defn clear
  "Clears all watches form the object
 
   (def subject   (atom nil))
   (do (watch/add subject :a (fn [_ _ _ n]))
       (watch/add subject :b (fn [_ _ _ n]))
       (watch/clear subject)
       (watch/list subject))
   => {}"
  {:added "3.0"}
  ([obj] (clear obj nil))
  ([obj opts]
   (let [watches (list obj opts)]
     (doseq [k (keys watches)]
       (remove obj k opts)))))

(defn set
  "Sets a watch in the form of a map
   (def obj (atom nil))
   (do (watch/set obj {:a (fn [_ _ _ n])
                       :b (fn [_ _ _ n])})
       (watch/list obj))
   => (contains {:a fn? :b fn?})"
  {:added "3.0"}
  ([obj watches] (set obj watches nil))
  ([obj watches opts]
   (doseq [[k f] watches]
     (add obj k f opts))
   (list obj opts)))

(defn copy
  "Copies watches from one object to another
   (def obj-a   (atom nil))
   (def obj-b   (atom nil))
   (do (watch/set obj-a {:a (fn [_ _ _ n])
                         :b (fn [_ _ _ n])})
       (watch/copy obj-b obj-a)
       (watch/list obj-b))
   => (contains {:a fn? :b fn?})"
  {:added "3.0"}
  ([to from] (copy to from nil))
  ([to from opts]
   (let [watches (list from opts)]
     (set to watches opts))))

(extend-protocol protocol.watch/IWatch
  clojure.lang.IRef
  (-add-watch [obj k f opts]
    (add-watch obj k
               (process-options opts f))
    (.getWatches obj))

  (-list-watch [obj _]
    (.getWatches obj))

  (-remove-watch [obj k _]
    (remove-watch obj k)))
