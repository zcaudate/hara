(ns hara.io.file.walk
  (:require [hara.core.base.util :as primitive]
            [hara.io.file.filter :as filter]
            [hara.io.file.option :as option]
            [hara.io.file.path :as path])
  (:import (java.nio.file FileVisitor Files)))

(defn match-single
  "matches according to the defined filter
 
   (match-single {:root (path/path \".\")
                  :path (path/path \"src/hara/test.clj\")}
                 {:tag :pattern
                  :pattern #\"src\"})
   => true
 
   (match-single {:root (path/path \"src\")
                  :path (path/path \"src/hara/test.clj\")}
                 {:tag :pattern
                  :pattern #\"src\"})
   => false
 
   (match-single {:path (path/path \"src/hara/test.clj\")}
                 {:tag :fn
                  :fn (fn [m]
                        (re-find #\"hara\" (str m)))})
   => true"
  {:added "3.0"}
  [{:keys [root path attrs] :as m} {:keys [tag] :as single}]
  (boolean (case tag
             :fn      (let [f (:fn single)]
                        (f path))
             :pattern (let [pat (:pattern single)]
                        (if-not (= (str root) (str path))
                          (->> (str root)
                               (count)
                               (inc)
                               (subs (str path))
                               (re-find pat))))
             :mode    (do (:mode single)
                          (throw (Exception. "TODO"))))))

(defn match-filter
  "matches according to many filters
 
   (match-filter {})
   => true
 
   (match-filter {:root (path/path \"\")
                  :path (path/path \"src/hara/test.clj\")
                  :include [{:tag :pattern
                             :pattern #\"test\"}]})
   => true
 
   (match-filter {:root (path/path \"\")
                  :path (path/path \"src/hara/test.clj\")
                  :exclude [{:tag :pattern
                             :pattern #\"test\"}]})
   => false"
  {:added "3.0"}
  [{:keys [path attrs root include exclude with] :as m}]
  (or (and (get with :root) (= (str root) (str path)))
      (let [include (if (empty? include)
                      [{:tag :fn :fn primitive/T}]
                      include)
            exclude (if (empty? exclude)
                      [{:tag :fn :fn primitive/F}]
                      exclude)]
        (and (some #(match-single m %) include)
             (not (some #(match-single m %) exclude))))))

(defn visit-directory-pre
  "helper function, triggers before visiting a directory"
  {:added "3.0"}
  [{:keys [root path attrs accumulate] :as m}]
  (let [f      (-> m :directory :pre)
        run?   (match-filter m)
        result (try
                 (when run?
                   (if (accumulate :directories)
                     (swap! (:accumulator m) conj path))
                   (if f (f m)))
                 :continue
                 (catch clojure.lang.ExceptionInfo e
                   (or (:command (ex-data e))
                       (throw e))))]
    (option/option result)))

(defn visit-directory-post
  "helper function, triggers after visiting a directory"
  {:added "3.0"}
  [m]
  (let [f (get-in m [:directory :post])
        run? (match-filter m)]
    (when (and f run?)
      (f m)))
  (option/option :continue))

(defn visit-file
  "helper function, triggers on visiting a file"
  {:added "3.0"}
  [{:keys [path attrs accumulate] :as m}]
  (let [f      (:file m)
        run?   (match-filter m)
        result (try
                 (when run?
                   (if (and (accumulate :files)
                            (->> (into-array [(option/option :nofollow-links)])
                                 (Files/isRegularFile path)))
                     (swap! (:accumulator m) conj path))
                   (if f (f m)))
                 :continue
                 (catch clojure.lang.ExceptionInfo e
                   (or (:command (ex-data e))
                       (throw e))))]
    (option/option result)))

(defn visit-file-failed
  "helper function, triggers on after a file cannot be visited"
  {:added "3.0"}
  [m]
  (option/option
   (or (if-let [f (-> m :failed)]
         (f m))
       :continue)))

(defn visitor
  "contructs the clojure wrapper for `java.nio.file.FileVisitor`"
  {:added "3.0"}
  [m]
  (reify FileVisitor
    (preVisitDirectory  [_ path attrs]
      (visit-directory-pre  (assoc m :path path :attrs attrs)))
    (postVisitDirectory [_ path error]
      (visit-directory-post (assoc m :path path :error error)))
    (visitFile          [_ path attrs]
      (visit-file           (assoc m :path path :attrs attrs)))
    (visitFileFailed    [_ path error]
      (visit-file-failed    (assoc m :path path :error error)))))

(defn walk
  "visits files based on a directory
 
   (walk \"src\" {:accumulate #{:directories}})
   => vector?
 
   (walk \"src\" {:accumulator (atom {})
                :accumulate  #{}
                :file (fn [{:keys [path attrs accumulator]}]
                        (swap! accumulator
                               assoc
                               (str path)
                               (.toMillis (.lastAccessTime attrs))))})
   => map?"
  {:added "3.0"}
  [root {:keys [directory file include
                exclude recursive depth options
                accumulator accumulate with]
         :as m}]
  (let [directory (cond (nil? directory)
                        {:pre primitive/F}

                        (fn? directory)
                        {:pre directory}

                        :else directory)
        file      (cond (nil? file) primitive/F

                        :else directory)
        options   (-> (map option/+file-visit-options+ options) (set) (disj nil))
        depth     (or (cond (false? recursive) 1
                            (true? recursive) Integer/MAX_VALUE)
                      depth
                      Integer/MAX_VALUE)
        root        (path/path root)
        accumulate  (or accumulate #{:files :directories})
        accumulator (or accumulator (atom []))
        include   (map filter/characterise-filter include)
        exclude   (map filter/characterise-filter exclude)
        with      (or with #{})
        state     (merge m {:root root
                            :directory directory
                            :depth depth
                            :include include
                            :exclude exclude
                            :options options
                            :with with
                            :accumulate accumulate
                            :accumulator accumulator})
        visitor    (visitor state)]
    (Files/walkFileTree (:root state)
                        (:options state)
                        (:depth state)
                        visitor)
    @accumulator))
