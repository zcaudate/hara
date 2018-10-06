(ns hara.deploy.analyser.java
  (:require [hara.string :as string]
            [hara.deploy.common :as common]))

(defn get-class
  "grabs the symbol of the class in the java file
   (get-class
    (io/file \"java/hara/string/mustache/Mustache.java\"))
   => 'hara.string.mustache.Mustache"
  {:added "3.0"}
  [file]
  (let [pkg (-> (->> (slurp file)
                     (string/split-lines)
                     (filter #(.startsWith % "package"))
                     (first))
                (string/split #"[ ;]")
                (second))
        nm  (let [nm (.getName file)]
              (subs nm 0 (- (count nm) 5)))]
    (symbol (str pkg "." nm))))

(defn get-imports
  "grabs the symbol of the class in the java file
   (get-imports
    (io/file \"java/hara/string/mustache/Context.java\"))
   => '(java.util.List java.util.Map clojure.lang.Keyword)"
  {:added "3.0"}
  [file]
  (->> (slurp file)
       (string/split-lines)
       (filter #(.startsWith % "import"))
       (map #(string/split % #"[ ;]"))
       (map second)
       (map symbol)))

(defmethod common/-file-info :java [file]
  {:file file
   :exports #{[:class (get-class file)]}
   :imports (set (map (fn [jv] [:class jv]) (get-imports file)))})
