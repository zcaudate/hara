(ns hara.core.environment
  (:require [clojure.walk :as walk]
            [hara.core.version :as version]
            [hara.string :as string])
  (:refer-clojure :exclude [require clojure-version load]))

(defrecord Properties [])

(defmethod print-method Properties
  [v ^java.io.Writer w]
  (.write w (str "#props" (vec (keys v)))))

(defn properties
  "returns jvm properties in a nested map for easy access
   (->> (env/properties)
        :os)
   => (contains {:arch anything
                 :name anything
                 :version anything})"
  {:added "3.0"}
  []
  (->> (System/getProperties)
       (map (fn [[k v]] [(string/path-split (keyword k) ".") v]))
       (sort)
       (reverse)
       (reduce (fn [out [k v]]
                 (if (get-in out k)
                   (assoc-in out (conj k :name) v)
                   (assoc-in out k v)))
               (Properties.))))

(defn load
  "returns jvm properties in a nested map for easy access
   (->> (java.io.StringReader. (str {:version [:property \"os.version\"]}))
        (env/load))
   => (contains {:version string?})"
  {:added "3.0"}
  [f]
  (->> (slurp f)
       (read-string)
       (walk/postwalk (fn [x]
                        (if (and (vector? x)
                                 (= :property (first x)))
                          (System/getProperty (second x))
                          x)))))

(defn clojure-version
  "returns the current clojure version
   (env/clojure-version)
   => (contains
       {:major anything,
        :minor anything,
        :incremental anything
        :qualifier anything})"
  {:added "3.0"}
  []
  *clojure-version*)

(defn java-version
  "returns the current java version
   (env/java-version)
   => (contains
       {:major anything,
        :minor anything,
        :incremental anything
        :qualifier anything})"
  {:added "3.0"}
  []
  (version/version (System/getProperty "java.version")))

(defn version
  "alternate way of getting clojure and java version
   (env/version :clojure)
   => (env/clojure-version)
 
   (env/version :java)
   => (env/java-version)"
  {:added "3.0"}
  ([] [:clojure :java])
  ([tag]
   (case tag
     :clojure (clojure-version)
     :java    (java-version))))

(defn satisfied
  "checks to see if the current version satisfies the given constraints
   (env/satisfied [:java    :newer {:major 1 :minor 7}]
                  {:major 1  :minor 8})
   => true
 
   (env/satisfied [:java  :older {:major 1 :minor 7}]
                  {:major 1  :minor 7})
   => false
 
   (env/satisfied [:java  :not-newer  {:major 12 :minor 0}])
   => true"
  {:added "3.0"}
  ([[type compare constraint :as entry]]
   (let [current (version type)]
     (satisfied entry current)))
  ([[type compare constraint] current]
   (if-let [pred (get version/*lookup* compare)]
     (pred current constraint)
     (throw (ex-info "input not valid" {:input compare
                                        :options (keys version/*lookup*)})))))

(defmacro init
  "only attempts to load the files when the minimum versions have been met
 
   (env/init [[:java    :newer {:major 1 :minor 8}]
              [:clojure :newer {:major 1 :minor 6}]]
            (:require [hara.time.data.zone
                        java-time-zoneid]
                       [hara.time.data.instant
                        java-time-instant]
                       [hara.time.data.format
                        java-time-format-datetimeformatter])
             (:import java.time.Instant))"
  {:added "3.0"}
  [constraints & statements]
  (if (->> constraints
           (map satisfied)
           (every? true?))
    (let [trans-fn (fn [[k & rest]]
                     (let [sym (symbol (str "clojure.core/" (name k)))]
                       (cons sym (map (fn [w]
                                        (if (keyword? w)
                                          w
                                          (list 'quote w)))
                                      rest))))]
      (cons 'do (map trans-fn statements)))))

(defmacro run
  "only runs the following code is the minimum versions have been met
   (env/run [[:java    :newer {:major 1 :minor 8}]
             [:clojure :newer {:major 1 :minor 6}]]
           (Instant/ofEpochMilli 0))"
  {:added "3.0"}
  [constraints & body]
  (if (->> constraints
           (map satisfied)
           (every? true?))
    (cons 'do body)))
