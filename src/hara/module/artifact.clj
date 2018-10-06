(ns hara.module.artifact
  (:require [hara.string :as string]
            [hara.function :refer [definvoke]]
            [hara.protocol.loader :as protocol.loader]
            [hara.module.artifact.common :as base]))

(defn rep->coord
  "encodes the rep to a coordinate
 
   (-> {:group \"hara\" :artifact \"hara\" :version \"2.4.0\"}
       (map->Rep)
       (rep->coord))
   => '[hara/hara \"2.4.0\"]"
  {:added "3.0"}
  [{:keys [group artifact version exclusions scope]}]
  (filterv identity (concat [(symbol group artifact) version]
                            (if exclusions [:exclusions exclusions])
                            (if scope [:scope scope]))))

(defn rep->path
  "encodes the rep to a path
 
   (-> {:group \"hara\" :artifact \"hara\" :version \"2.4.0\"}
       (map->Rep)
       (rep->path))
   => \"<.m2>/hara/hara/2.4.0/hara-2.4.0.jar\""
  {:added "3.0"}
  [{:keys [group artifact version extension]}]
  (string/joinr base/*sep*
               [base/*local-repo* (.replaceAll group "\\." base/*sep*)
                artifact version (str artifact "-" version "." (or extension "jar"))]))

(defn rep->string
  "encodes the rep to a string
 
   (-> {:group \"hara\" :artifact \"hara\" :version \"2.4.0\"}
       (map->Rep)
       (rep->string))
   => \"hara:hara:2.4.0\""
  {:added "3.0"}
  [{:keys [group artifact extension version]}]
  (string/joinr ":" [group
                    artifact
                    (if extension
                      (str extension ":" version)
                      version)]))

(defrecord Rep [group artifact extension classifier version properties file scope exclusions]
  Object
  (toString [rep] (rep->string rep)))

(defmethod print-method Rep
  [v w]
  (.write w (str "'" v)))

(defn rep?
  "checks if an object is of type `hara.module.artifact.Rep`
 
   (rep? (rep \"hara:hara:2.4.0\"))
   => true"
  {:added "3.0"}
  [obj]
  (instance? Rep obj))

(defn coord->rep
  "converts a coord to a rep instance
 
   (coord->rep '[hara/hara \"2.4.0\"])
   => (contains {:group \"hara\"
                 :artifact \"hara\"
                 :version \"2.4.0\"})"
  {:added "3.0"}
  [[name version & {:keys [scope exclusions]}]]
  (let [[group artifact] (string/split (str name) #"/")
        artifact (or artifact
                     group)]
    (Rep. group artifact "jar" nil version {} nil scope exclusions)))

(defn path->rep
  "converts a path to a rep instance
 
   (path->rep (str base/*local-repo* \"/hara/hara/2.4.0/hara-2.4.0.jar\"))
   => (contains {:group \"hara\"
                 :artifact \"hara\"
                 :version \"2.4.0\"})"
  {:added "3.0"}
  [x]
  (let [arr (->> (re-pattern base/*sep*)
                 (string/split (.replaceAll x base/*local-repo* ""))
                 (remove empty?))
        extension (-> (last arr)
                      (string/split #"\.")
                      last)
        version   (last (butlast arr))
        artifact  (last (butlast (butlast arr)))
        group     (string/joinr "." (butlast (butlast (butlast arr))))]
    (Rep. group artifact extension nil version {} x nil nil nil nil)))

(defn string->rep
  "converts a string to a rep instance
 
   (string->rep \"hara:hara:2.4.0\")
   => (contains {:group \"hara\"
                 :artifact \"hara\"
                 :version \"2.4.0\"})"
  {:added "3.0"}
  [s]
  (let [[group artifact extension? classifer? version :as array] (string/split s #":")]
    (case (count array)
      1 (path->rep s)
      2 (Rep. group artifact "jar" nil nil {} nil nil nil)
      3 (Rep. group artifact "jar" nil extension? {} nil nil nil)
      4 (Rep. group artifact extension? nil classifer? {} nil nil nil)
      5 (Rep. group artifact extension? classifer? version {} nil nil nil))))

(defn coord?
  "checks for a valid coordinate
 
   (coord? '[org.clojure/clojure \"1.8.0\"]) => true
 
   (coord? '[1 2 3]) => false"
  {:added "3.0"}
  [obj]
  (and (vector? obj)
       (symbol? (first obj))
       (string? (second obj))))

(defn rep
  "converts various formats to a rep
 
   (str (rep '[hara/hara \"2.4.0\"]))
   => \"hara:hara:jar:2.4.0\"
 
   (str (rep \"hara:hara:2.4.0\"))
   => \"hara:hara:jar:2.4.0\""
  {:added "3.0"}
  [obj]
  (protocol.loader/-rep obj))

(definvoke rep-default
  "creates the default representation of a artifact
 
   (into {} (rep-default \"hara:hara:2.4.0\"))
   => {:properties {},
       :group \"hara\",
       :classifier nil,
       :file nil,
       :exclusions nil,
       :scope nil,
       :extension \"jar\",
      :artifact \"hara\",
       :version \"2.4.0\"}"
  {:added "3.0"}
  [:method {:multi protocol.loader/-rep
            :val :default}]
  ([x]
   (cond (instance? Rep x) x

         (map? x) (map->Rep x)

         (coord? x) (coord->rep x)

         (string? x)
         (if (.startsWith x base/*local-repo*)
           (path->rep x)
           (string->rep x))

         (symbol? x)
         (coord->rep [x])

         :else
         (throw (Exception. (str "Invalid form: (" (type x)  ") " x))))))

(defn artifact
  "converts various artifact formats
 
   (artifact :string '[hara/hara \"2.4.0\"])
   => \"hara:hara:jar:2.4.0\"
 
   (artifact :path \"hara:hara:2.4.0\")
   => (str base/*local-repo*
           \"/hara/hara/2.4.0/hara-2.4.0.jar\")"
  {:added "3.0"}
  ([x]
   (artifact :default x))
  ([type x]
   (protocol.loader/-artifact type x)))

(definvoke artifact-default
  "converts an artifact in any format to the default representation
 
   (artifact-default '[hara/hara \"2.4.0\"])
   => rep?"
  {:added "3.0"}
  [:method {:multi protocol.loader/-artifact
            :val   :default}]
  ([x]
   (artifact-default nil x))
  ([_ x]
   (rep x)))

(definvoke artifact-string
  "converts an artifact in any format to the string representation
 
   (artifact-string '[hara/hara \"2.4.0\"])
   => \"hara:hara:jar:2.4.0\""
  {:added "3.0"}
  [:method {:multi protocol.loader/-artifact
            :val   :string}]
  ([x]
   (artifact-string nil x))
  ([_ x]
   (-> (rep x) rep->string)))

(definvoke artifact-symbol
  "converts an artifact in any format to the symbol representation
 
   (artifact-symbol '[hara/hara \"2.4.0\"])
   => 'hara/hara"
  {:added "3.0"}
  [:method {:multi protocol.loader/-artifact
            :val   :symbol}]
  ([x]
   (artifact-symbol nil x))
  ([_ x]
   (-> (rep x) rep->coord first)))

(definvoke artifact-path
  "converts an artifact in any format to the path representation
 
   (artifact-path '[hara/hara \"2.4.0\"])
   => \"<.m2>/hara/hara/2.4.0/hara-2.4.0.jar\""
  {:added "3.0"}
  [:method {:multi protocol.loader/-artifact
            :val   :path}]
  ([x]
   (artifact-path nil x))
  ([_ x]
   (if (and (string? x)
            (.endsWith x "jar"))
     x
     (-> (rep x)
         rep->path))))

(definvoke artifact-coord
  "converts an artifact in any format to the coord representation
 
   (artifact-coord \"hara:hara:jar:2.4.0\")
   => '[hara/hara \"2.4.0\"]"
  {:added "3.0"}
  [:method {:multi protocol.loader/-artifact
            :val   :coord}]
  ([x]
   (artifact-coord nil x))
  ([_ x]
   (-> (rep x) rep->coord)))
