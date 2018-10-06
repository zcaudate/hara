(ns hara.lib.jgit.api.difference
  (:require [clojure.java.io :as io]
            [hara.object :as object]
            [hara.lib.jgit.api.repository :as repository])
  (:import (org.eclipse.jgit.api Git)
           (org.eclipse.jgit.diff DiffEntry DiffEntry$ChangeType Edit EditList HistogramDiff RawText RawTextComparator)
           (org.eclipse.jgit.lib AbbreviatedObjectId Constants Repository)))

(defonce null-id "0000000000000000000000000000000000000000")

(defn enum->keyword
  "converts an enum to a keyword
 
   (enum->keyword Constants/ATTR_FILTER)
   => :filter
 
   (enum->keyword Constants/CHARSET)
   => :utf-8"
  {:added "3.0"}
  [enum]
  (keyword (.toLowerCase (str enum))))

(defrecord Change [])

(defmethod print-method Change
  [v ^java.io.Writer w]
  (let [shorthand  (fn [v] [(-> v :lines :start) (-> v :lines :end)])]
    (.write w (str "#" (name (:type v)) " "
                   (-> (into {} v)
                       (dissoc :type)
                       (update-in [:old] shorthand)
                       (update-in [:new] shorthand))))))

(defn retrieve-text
  "retrieve texts from given lines of a blob 
 
   (-> (retrieve-text (repo/repository)
                      (AbbreviatedObjectId/fromString \"80973f8e246b58f84a94ee9c1b91336ad117924e\")
                      0
                      1) ;; => (\"(defproject zcaudate/lucid \\\"1.4.5\\\"\\\n\")
       first
       (subs 1)
       read-string)
   => 'defproject"
  {:added "3.0"}
  [^Repository repo ^AbbreviatedObjectId id start end]
  (cond (= (.name id) null-id) []

        (= start end) []

        :else
        (let [lines (-> repo
                        (.open (.toObjectId id) Constants/OBJ_BLOB)
                        (.openStream)
                        (io/reader)
                        (line-seq)
                        vec)
              cnt (count lines)]
          (->> lines
               (map-indexed (fn [i line]
                              [(inc i) line]))
               (keep (fn [[i line]]
                       (if (< start i (inc end))
                         (if (= cnt i)
                           line
                           (str line "\\n")))))))))

(defn edit->change
  "helper function for `format-change`"
  {:added "3.0"}
  [^Edit edit]
  (map->Change {:type (enum->keyword (.getType edit))
                :old {:lines {:start  (.getBeginA edit)
                              :end    (.getEndA edit)
                              :length (.getLengthA edit)}}
                :new {:lines {:start  (.getBeginB edit)
                              :end    (.getEndB edit)
                              :length (.getLengthB edit)}}}))

(defn format-change
  "helper function for `format-changes`"
  {:added "3.0"}
  [^Repository repo ^Edit edit old-id new-id]
  (let [retrieve (fn [{:keys [lines] :as entry} id]
                   (assoc entry :text (retrieve-text repo id (:start lines) (:end lines))))]
    (-> (edit->change edit)
        (update-in [:old] retrieve old-id)
        (update-in [:new] retrieve new-id))))

(defn format-changes
  "helper function for `format-entry`"
  {:added "3.0"}
  [^Repository repo entry old-id new-id]
  (let [get-text (fn [^AbbreviatedObjectId id]
                   (if (and id (not= (.name id) null-id))
                     (-> repo
                         (.open (.toObjectId id) Constants/OBJ_BLOB)
                         (.getCachedBytes)
                         (RawText.))
                     RawText/EMPTY_TEXT))
        old-text (get-text old-id)
        new-text (get-text new-id)
        changes  (-> (HistogramDiff.)
                     (.diff RawTextComparator/DEFAULT old-text new-text))]
    (mapv #(format-change repo % old-id new-id) changes)))

(defrecord Entry [])

(defmethod print-method Entry
  [v ^java.io.Writer w]
  (.write w (str "#" (name (:type v)) " " (into {} (dissoc v :type)))))

(defn format-entry
  "formats the entry returned by `git-diff`
   (->> (git-diff (repo/repository)
                  {:commit \"HEAD~3\"}
                  {})
        first
        (format-entry (repo/repository)))
   ;; => #modify {:new-id \"c81c8f4e14c252adecd1d983ce161055d253c62e\",
   ;;             :old-id \"356a16f27cd0e66d5d1950252bd6ae57851881a0\",
   ;;             :new-path \"project.clj\",
   ;;             :old-path \"project.clj\",
   ;;             :changes [#replace {:old [50 51],
   ;;                                 :new [50 51]}
   ;;                       #delete {:old [121 122],
   ;;                                :new [121 121]}
   ;;                       #replace {:old [127 128],
   ;;                                 :new [126 127]}]}
 "
  {:added "3.0"}
  [^Repository repo ^DiffEntry entry]
  (let [type    (enum->keyword (.getChangeType entry))
        changes (format-changes repo entry (.getOldId entry) (.getNewId entry))
        entry   (-> (object/to-data entry)
                    (assoc :type type :changes changes)
                    (dissoc :change-type :tree-filter-marks :score :new-mode :old-mode)
                    (map->Entry))]
    (case type
      :add    (dissoc entry :old-id :old-path)
      :delete (dissoc entry :new-id :new-path)
      entry)))

(defn git-diff
  "returns the raw diff object
   (-> (git-diff (repo/repository)
                 {:commit \"HEAD~3\"}
                 {})
       first)
   ;; => #entry{:change-type \"MODIFY\",
   ;;           :new-id \"c81c8f4e14c252adecd1d983ce161055d253c62e\",
   ;;           :new-mode \"100644\",
   ;;           :new-path \"project.clj\",
   ;;           :old-id \"356a16f27cd0e66d5d1950252bd6ae57851881a0\",
   ;;           :old-mode \"100644\",
   ;;           :old-path \"project.clj\",
   ;;           :score 0,
   ;;           :tree-filter-marks 0}
 "
  {:added "3.0"}
  [^Repository repo old new]
  (-> (Git. repo)
      (.diff)
      (.setOldTree (repository/tree-parser repo old))
      (.setNewTree (repository/tree-parser repo new))
      (.call)))

(defn list-difference
  "returns a list of modifications from one version to another
   (-> (list-difference (repo/repository)
                        {:commit \"HEAD~3\"})
       first)
   ;; => #modify {:new-id \"c81c8f4e14c252adecd1d983ce161055d253c62e\",
   ;;             :old-id \"356a16f27cd0e66d5d1950252bd6ae57851881a0\",
   ;;             :new-path \"project.clj\",
   ;;             :old-path \"project.clj\",
   ;;             :changes [#replace {:old [50 51], :new [50 51]}
   ;;                       #delete {:old [121 122], :new [121 121]}
   ;;                       #replace {:old [127 128], :new [126 127]}]}
 "
  {:added "3.0"}
  ([repo old]
   (list-difference repo old {}))
  ([^Repository repo old new]
   (->> (git-diff repo old new)
        (map (partial format-entry repo)))))

(defn list-file-changes
  "lists file changes between the previous and current repositories
 
   (list-file-changes (repo/repository)
                      {:commit \"HEAD~3\"})
   ;; => ({:path \"project.clj\", :type :modify}
   ;;     {:path \"test/hara.deploy/manifest/graph/external_test.clj\", :type :modify}
   ;;     {:path \"test/lucid/system/jvm_test.clj\", :type :modify})
   "
  {:added "3.0"}
  ([repo old]
   (list-file-changes repo old {}))
  ([^Repository repo old new]
   (->> (git-diff repo old new)
        (map (fn [^DiffEntry entry]
               {:path (.getNewPath entry)
                :type (enum->keyword (.getChangeType entry))}))
        (filter (fn [{:keys [type]}]
                  (#{:add :modify :copy} type))))))
