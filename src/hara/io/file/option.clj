(ns hara.io.file.option
  (:require [hara.core.base.enum :as enum]
            [hara.string :as string])
  (:import (java.nio.file AccessMode FileVisitOption FileVisitResult LinkOption StandardCopyOption StandardOpenOption)
           (java.nio.file.attribute PosixFilePermission PosixFilePermissions)))

(defonce +file-permissions+ (enum/enum-map PosixFilePermission))

(defonce +access-modes+ (enum/enum-map AccessMode))

(defonce +copy-options+ (enum/enum-map StandardCopyOption))

(defonce +file-visit-options+ (enum/enum-map FileVisitOption))

(defonce +file-visit-results+ (enum/enum-map FileVisitResult))

(defonce +link-options+ (enum/enum-map LinkOption))

(defonce +open-options+ (enum/enum-map StandardOpenOption))

(defonce +all-options+
  (merge +copy-options+
         +file-visit-options+
         +file-visit-results+
         +link-options+
         +open-options+))

(defn to-mode-string
  "transforms mode numbers to mode strings
 
   (to-mode-string \"455\")
   => \"r--r-xr-x\"
 
   (to-mode-string \"777\")
   => \"rwxrwxrwx\""
  {:added "3.0"}
  [s]
  (->> s
       (map (fn [ch]
              (case ch
                \0 "---"
                \1 "--x"
                \2 "-w-"
                \3 "-wx"
                \4 "r--"
                \5 "r-x"
                \6 "rw-"
                \7 "rwx")))
       (apply str)))

(defn to-mode-number
  "transforms mode numbers to mode strings
 
   (to-mode-number \"r--r-xr-x\")
   => \"455\"
 
   (to-mode-number \"rwxrwxrwx\")
   => \"777\""
  {:added "3.0"}
  [s]
  (->> (partition 3 s)
       (map #(apply str %))
       (map (fn [mode]
              (let [hist (frequencies mode)]
                (reduce-kv (fn [out k v]
                             (+ out (or (if (= 1 v)
                                          (case k
                                            \r 4
                                            \w 2
                                            \x 1
                                            nil))
                                        0)))
                           0
                           hist))))
       (apply str)))

(defn to-permissions
  "transforms mode to permissions
 
   (to-permissions \"455\")  
   => (contains [:owner-read
                 :group-read
                 :group-execute
                 :others-read
                 :others-execute] :in-any-order)"
  {:added "3.0"}
  [s]
  (->> (to-mode-string s)
       (PosixFilePermissions/fromString)
       (map (comp keyword string/spear-type str))))

(defn from-permissions
  "transforms permissions to mode
 
   (from-permissions [:owner-read
                      :group-read
                      :group-execute
                      :others-read
                      :others-execute])
   => \"455\""
  {:added "3.0"}
  [modes]
  (->> (map +file-permissions+ modes)
       set
       (PosixFilePermissions/toString)
       (to-mode-number)))

(defn option
  "shows all options for file operations
 
   (option)
   => (contains [:atomic-move :create-new
                 :skip-siblings :read :continue
                 :create :terminate :copy-attributes
                 :append :truncate-existing :sync
                 :follow-links :delete-on-close :write
                 :dsync :replace-existing :sparse
                 :nofollow-links :skip-subtree])
 
   (option :read)
   => java.nio.file.StandardOpenOption/READ"
  {:added "3.0"}
  ([] (keys +all-options+))
  ([k]
   (+all-options+ k)))
