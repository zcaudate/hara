(ns hara.module.classloader.common
  (:require [hara.core.base.check :as check])
  (:import (java.net URL)))

(defn to-url
  "constructs a `java.net.URL` object from a string
 
   (str (to-url \"/dev/null\"))
   => \"file:/dev/null/\""
  {:added "3.0"}
  [path]
  (cond (check/url? path)
        path

        (string? path)
        (let [path (if-not (or (.endsWith path ".jar")
                               (.endsWith path "/"))
                     (str path "/")
                     path)
              path (if-not (.startsWith path "file:")
                     (str "file:" path)
                     path)]
          (URL. path))

        :else (to-url (str path))))
