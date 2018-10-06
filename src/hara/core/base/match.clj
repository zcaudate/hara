(ns hara.core.base.match
  (:require [hara.protocol.match :as protocol.match]
            [hara.core.base.check :as check]))

(defrecord ActualPattern [expression]
  protocol.match/ITemplate
  (-match [template obj]
    (= expression obj)))

(defn actual-pattern
  "constructs a pattern used for direct comparison
 
   (actual-pattern '_)
 
   (actual-pattern #{1 2 3})"
  {:added "3.0"}
  [expression]
  (ActualPattern. expression))

(defn actual-pattern?
  "checks if input is an actual pattern
 
   (actual-pattern? '_) => false
 
   (-> (actual-pattern '_)
       actual-pattern?)
   => true"
  {:added "3.0"}
  ([pattern]
   (instance? ActualPattern pattern))
  ([pattern expression]
   (and (actual-pattern? pattern)
        (= expression (:expression pattern)))))

(defrecord EvaluationPattern [expression]
  protocol.match/ITemplate
  (-match [template obj]
    (protocol.match/-match (eval expression) obj)))

(defn eval-pattern
  "constructs a pattern that is evaluated before comparison
 
   (eval-pattern '(keyword \"a\"))
   
   (eval-pattern 'symbol?)"
  {:added "3.0"}
  [expression]
  (EvaluationPattern. expression))

(defn eval-pattern?
  "checks if input is an eval pattern
 
   (-> (eval-pattern 'symbol?)
       eval-pattern?)
   => true"
  {:added "3.0"}
  ([pattern]
   (instance? EvaluationPattern pattern))
  ([pattern expression]
   (and (eval-pattern? pattern)
        (= expression (:expression pattern)))))

(extend-protocol protocol.match/ITemplate
  Object
  (-match [template obj]
    (= template obj)))

(extend-protocol protocol.match/ITemplate
  clojure.lang.Symbol
  (-match [template obj]
    (cond (= template '_)
          true

          :else
          (= template obj))))

(defn match-inner
  "matches the inner contents of a array
 
   (match-inner [number? {:a {:b #'symbol?}} '& '_]
                [1 {:a {:b 'o}} 5 67 89 100])
   => true"
  {:added "3.0"}
  [template arr]
  (loop [[t & tmore :as tall] template
         [x & more :as all] arr]
    (cond (and (empty? tall) (empty? all))
          true

          (empty? tall) false

          (= t '&)
          (protocol.match/-match (first tmore) (cons x more))

          (protocol.match/-match t x)
          (recur tmore more)

          :else false)))

(extend-protocol protocol.match/ITemplate
  clojure.lang.IPersistentList
  (-match [template obj]
    (cond (or (list? obj)
              (check/lazy-seq? obj))
          (match-inner template obj)

          :else false)))

(extend-protocol protocol.match/ITemplate
  clojure.lang.IPersistentVector
  (-match [template obj]
    (cond (vector? obj)
          (match-inner template obj)

          :else false)))

(extend-protocol protocol.match/ITemplate
  clojure.lang.IPersistentMap
  (-match [template obj]
    (cond (map? obj)
          (and (= (keys template) (keys obj))
               (->> (map protocol.match/-match
                         (vals template)
                         (vals obj))
                    (every? true?)))

          :else false)))

(extend-protocol protocol.match/ITemplate
  clojure.lang.Fn
  (-match [template obj]
    (cond (= template obj)
          true

          :else
          (try (boolean (template obj))
               (catch Throwable t
                 false)))))

(extend-protocol protocol.match/ITemplate
  clojure.lang.Var
  (-match [template obj]
    (protocol.match/-match @template obj)))

(extend-protocol protocol.match/ITemplate
  clojure.lang.APersistentSet
  (-match [template obj]
    (->> template
         (map (fn [t]
                (protocol.match/-match t obj)))
         (filter true?)
         (first))))

(extend-protocol protocol.match/ITemplate
  java.util.regex.Pattern
  (-match [template obj]
    (cond (string? obj)
          (re-find template obj)

          (instance? java.util.regex.Pattern obj)
          (= (.pattern template)
             (.pattern obj)))))
