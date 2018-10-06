(ns hara.test.form.match
  (:require [clojure.set :as set]))

(defn match-base
  "determines whether a term matches with a filter
   (match-base {:tags #{:web}}
               {:tags #{:web}}
               false)
   => [true false false]
   (match-base {:refer 'user/foo
                :namespace 'user}
               {:refers '[user/other]
                :namespaces '[foo bar]}
               true)
   => [true false false]"
  {:added "3.0"}
  [fmeta {:keys [tags refers namespaces] :as filter} default]
  [(if-not (empty? tags)
     (->> (:tags fmeta)
          (set/intersection tags)
          (empty?)
          (not))
     default)
   (if-not (empty? refers)
     (let [refer (:refer fmeta)
           refers (set refers)
           ns    (if (symbol? refer)
                   (symbol (namespace refer)))]
       (boolean (or (if ns (refers ns))
                    (refers refer))))
     default)
   (if-not (empty? namespaces)
     (or (->> namespaces
              (map (fn [namespace]
                     (cond (symbol? namespace)
                           (= (str (:ns fmeta))
                              (str namespace))

                           (instance? java.util.regex.Pattern namespace)
                           (boolean (re-find namespace (str (:ns fmeta)))))))
              (some true?))
         false)
     default)])

(defn match-include
  "determines whether inclusion is a match
   (match-include {:tags #{:web}}
                  {:tags #{:web}})
   => true
 
   (match-include {:refer 'user/foo
                   :namespace 'user}
                  {})
   => true"
  {:added "3.0"}
  [fmeta filter]
  (not (some false? (match-base fmeta filter true))))

(defn match-exclude
  "determines whether exclusion is a match
   (match-exclude {:tags #{:web}}
                  {:tags #{:web}})
   => true
   (match-exclude {:refer 'user/foo
                   :namespace 'user}
                  {})
   => false"
  {:added "3.0"}
  [fmeta filter]
  (or (some true? (match-base fmeta filter false))
      false))

(defn match-options
  "determines whether a set of options can match
   (match-options {:tags #{:web}
                   :refer 'user/foo}
                  {:include [{:tags #{:web}}]
                   :exclude []})
   => true
 
   (match-options {:tags #{:web}
                   :refer 'user/foo}
                  {:include [{:tags #{:web}}]
                   :exclude [{:refers '[user/foo]}]})
   => false
 
   (match-options {:tags #{:web}
                   :ns 'user
                   :refer 'user/foo}
                  {:include [{:namespaces [#\"us\"]}]})
   => true"
  {:added "3.0"}
  [{:keys [refer tags] :as fmeta} {:keys [include exclude] :as settings}]
  (cond (and tags (empty? include))
        false

        :else
        (and (if (empty? include)
               true
               (->> include
                    (map #(match-include fmeta %))
                    (some true?)))
             (->> exclude
                  (map #(match-exclude fmeta %))
                  (every? false?)))))
