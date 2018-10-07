(ns hara.io.file.watch
  (:require [clojure.java.io :as io]
            [hara.string :as string]
            [hara.data.base.map :as map]
            [hara.protocol.watch :as protocol.watch])
  (:import (java.nio.file FileSystems Paths StandardWatchEventKinds WatchService)
           (java.util.concurrent TimeUnit)))

(def ^:dynamic *defaults* {:recursive true
                           :types :all
                           :mode :sync
                           :exclude [".*"]})

(defonce ^:dynamic *filewatchers* (atom {}))

(def event-kinds  [StandardWatchEventKinds/ENTRY_CREATE
                   StandardWatchEventKinds/ENTRY_DELETE
                   StandardWatchEventKinds/ENTRY_MODIFY])
(def event-types  [:create :delete :modify])
(def event-lookup (zipmap event-types event-kinds))
(def kind-lookup  (zipmap event-kinds event-types))

(defn pattern
  "creates a regex pattern from the string representation
 
   (pattern \".*\") => #\"\\Q.\\E.+\"
 
   (pattern \"*.jar\") => #\".+\\Q.\\Ejar\""
  {:added "3.0"}
  [s]
  (-> s
      (string/replace #"\." "\\\\\\Q.\\\\\\E")
      (string/replace #"\*" ".+")
      (re-pattern)))

(defn register-entry
  "adds a path to the watch service
 
   (-> (.newWatchService (FileSystems/getDefault))
       (register-entry \"src\"))"
  {:added "3.0"}
  [service path]
  (let [entry (Paths/get path (make-array String 0))]
    (.register entry service (into-array event-kinds))))

(defn register-sub-directory
  "registers a directory to an existing watcher
 
   (-> (watcher [\"src\"] {} {})
       (assoc :service (.newWatchService (FileSystems/getDefault)))
      (register-sub-directory \"test\"))"
  {:added "3.0"}
  [watcher dir-path]
  (let [{:keys [root seen options service excludes includes]} watcher]
    (when (and (or (= dir-path root)
                   (empty? includes)
                   (some #(re-find % (subs dir-path (-> root count inc))) includes))
               (not (or (and seen (get @seen dir-path))
                        (some #(re-find % (last (string/split dir-path #"/"))) excludes))))
      (register-entry service dir-path)
      (if seen (swap! seen conj dir-path))
      (if (:recursive options)
        (doseq [^java.io.File f (.listFiles (io/file dir-path))]
          (when (. f isDirectory)
            (register-sub-directory watcher (.getCanonicalPath f))))))
    watcher))

(defn register-path
  "registers either a file or a path to the watcher
   (-> (watcher [] {} {})
       (assoc :service (.newWatchService (FileSystems/getDefault)))
      (register-path \"test\"))"
  {:added "3.0"}
  [{:keys [service] :as watcher} path]
  (let [f (io/file path)]
    (cond (.isDirectory f)
          (register-sub-directory watcher path)

          (.exists f)
          (register-entry service path))))

(defn process-event
  "helper function to process event"
  {:added "3.0"}
  [watcher kind ^java.io.File file]
  (let [{:keys [options callback excludes filters kinds]} watcher
        filepath (.getPath file)
        filename (.getName file)]
    (if (and (get kinds kind)
             (or  (empty? filters)
                  (some #(re-find % filename) filters)))
      (case (:mode options)
        :async (future (callback (kind-lookup kind) file))
        :sync  (callback (kind-lookup kind) file)))))

(defn run-watcher
  "initiates the watcher with the given callbacks"
  {:added "3.0"}
  [watcher]
  (let [^java.nio.file.WatchKey wkey
        (.take ^java.nio.file.WatchService (:service watcher))]
    (doseq [^java.nio.file.WatchEvent event (.pollEvents wkey)
            :when (not= (.kind event)
                        StandardWatchEventKinds/OVERFLOW)]
      (let [kind (.kind event)
            ^java.nio.file.Path path (.watchable wkey)
            ^java.nio.file.Path context (.context event)
            ^java.nio.file.Path res-path (.resolve path context)
            ^java.io.File file (.toFile res-path)]
        (if (and (= kind StandardWatchEventKinds/ENTRY_CREATE)
                 (.isDirectory file)
                 (-> watcher :options :recursive))
          (register-sub-directory watcher (.getPath file)))
        (if-not (.isDirectory file)
          (process-event watcher kind file))))
    (.reset wkey)
    (recur watcher)))

(defn start-watcher
  "starts the watcher"
  {:added "3.0"}
  [watcher]
  (let [{:keys [types filter exclude include]} (:options watcher)
        ^java.nio.file.WatchService service (.newWatchService (FileSystems/getDefault))
        seen    (atom #{})
        kinds   (if (= types :all)
                  (set event-kinds)
                  (->> types (map event-lookup) set))
        filters  (->> filter  (map pattern))
        excludes (->> exclude (map pattern))
        includes (->> include (map pattern))
        watcher  (->> (assoc watcher
                             :root (first (:paths watcher))
                             :service service
                             :seen seen
                             :filters filters
                             :excludes excludes
                             :includes includes
                             :kinds kinds))
        watcher  (reduce register-path watcher (:paths watcher))]
    (assoc watcher :running (future (run-watcher watcher)))))

(defn stop-watcher
  "stops the watcher"
  {:added "3.0"}
  [watcher]
  (.close ^java.nio.file.WatchService (:service watcher))
  (future-cancel (:running watcher))
  (dissoc watcher :running :service :seen))

(defrecord Watcher [paths callback options]
  Object
  (toString [this]
    (str "#watcher" (assoc options :paths paths :running (-> this :running not not)))))

(defmethod print-method Watcher
  [v ^java.io.Writer w]
  (.write w (str v)))

(defn watcher
  "the watch interface provided for java.io.File
 
   (def ^:dynamic *happy* (promise))
 
   (watch/add (io/file \".\") :save
              (fn [f k _ [cmd file]]
                (watch/remove f k)
                (.delete file)
                (deliver *happy* [cmd (.getName file)]))
              {:types #{:create :modify}
               :recursive false
               :filter  [\".hara\"]
               :exclude [\".git\" \"target\"]})
 
   (watch/list (io/file \".\"))
   => (contains {:save fn?})
 
   (spit \"happy.hara\" \"hello\")
 
   @*happy*
   => [:create \"happy.hara\"]
 
   (watch/list (io/file \".\"))
   => {}"
  {:added "3.0"}
  [paths callback options]
  (let [paths   (if (coll? paths) paths [paths])]
    (Watcher. paths callback
              (map/merge-nil options *defaults*))))

(defn watch-callback
  "helper function to create watch callback"
  {:added "3.0"}
  [f root k]
  (fn [type file]
    (f root k nil [type file])))

(defn add-io-watch
  "registers the watch to a global list of *filewatchers*"
  {:added "3.0"}
  [obj k f opts]
  (let [path (.getCanonicalPath ^java.io.File obj)
        _    (if-let [wt (get-in @*filewatchers* [path k :watcher])]
               (protocol.watch/-remove-watch obj k nil))
        cb   (watch-callback f obj k)
        wt   (start-watcher (watcher path cb opts))]
    (swap! *filewatchers* assoc-in [path k] {:watcher wt :function f})
    obj))

(defn list-io-watch
  "list all *filewatchers"
  {:added "3.0"}
  [obj _]
  (let [path (.getCanonicalPath ^java.io.File  obj)]
    (->> (get @*filewatchers* path)
         (map/map-vals :function))))

(defn remove-io-watch
  "removes the watcher with the given key"
  {:added "3.0"}
  [obj k _]
  (let [path (.getCanonicalPath ^java.io.File obj)
        wt   (get-in @*filewatchers* [path k :watcher])]
    (if-not (nil? wt)
      (if (stop-watcher wt)
        (swap! *filewatchers* dissoc path)
        true)
      false)))

(extend-protocol protocol.watch/IWatch
  java.io.File
  (-add-watch [obj k f opts]
    (add-io-watch obj k f opts))

  (-list-watch [obj _]
    (list-io-watch obj nil))

  (-remove-watch [obj k _]
    (remove-io-watch obj k nil)))
