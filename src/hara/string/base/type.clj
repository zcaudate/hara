(ns hara.string.base.type
  (:require [hara.string.base.common :as common]))

(defn re-sub
  "substitute a pattern by applying a function
 
   (re-sub \"aTa\" +hump-pattern+ (fn [_] \"$\"))
   => \"$a\""
  {:added "3.0"}
  [^String value pattern sub-func]
  (loop [matcher (re-matcher pattern value)
         result []
         last-end 0]
    (if (.find matcher)
      (recur matcher
             (conj result
                   (.substring value last-end (.start matcher))
                   (sub-func (re-groups matcher)))
             (.end matcher))
      (apply str (conj result (.substring value last-end))))))

(defonce +hump-pattern+ #"[a-z0-9][A-Z]")
(defonce +non-camel-pattern+ #"[_| |\-][A-Za-z]")
(defonce +non-snake-pattern+ #"[ |\-]")
(defonce +non-spear-pattern+ #"[ |\_]")

(defn separate-humps
  "separate words that are camel cased
   
   (separate-humps \"aTaTa\")
   => \"a Ta Ta\""
  {:added "3.0"}
  [^String value]
  (re-sub value +hump-pattern+ #(common/join (seq %) " ")))

(defn camel-type
  "converts a string-like object to camel case representation
 
   (camel-type \"hello-world\")
   => \"helloWorld\"
 
   (string/camel-type 'hello_world)
   => 'helloWorld"
  {:added "3.0"}
  [^String value]
  (re-sub value
          +non-camel-pattern+
          (fn [s] (common/upper-case (apply str (rest s))))))

(defn capital-type
  "converts a string-like object to captital case representation
 
   (capital-type \"hello world\")
   => \"Hello World\"
 
   (str (string/capital-type :hello-world))
   => \":Hello World\""
  {:added "3.0"}
  [^String value]
  (-> (separate-humps value)
      (common/split #"[ |\-|_]")
      (->> (map common/capital-case))
      (common/join " ")))

(defn lower-type
  "converts a string-like object to a lower case representation
 
   (lower-type \"helloWorld\")
   => \"hello world\"
 
   (string/lower-type 'hello-world)
   => (symbol \"hello world\")"
  {:added "3.0"}
  [^String value]
  (-> (separate-humps value)
      (common/split #"[ |\-|_]")
      (->> (map common/lower-case))
      (common/join " ")))

(defn pascal-type
  "converts a string-like object to a pascal case representation
 
   (pascal-type \"helloWorld\")
   => \"HelloWorld\" 
 
   (string/pascal-type :hello-world)
   => :HelloWorld"
  {:added "3.0"}
  [^String value]
  (let [s (camel-type value)]
    (str (.toUpperCase (subs s 0 1))
         (subs s 1))))

(defn phrase-type
  "converts a string-like object to snake case representation
 
   (phrase-type \"hello-world\")
   => \"Hello world\""
  {:added "3.0"}
  [^String value]
  (let [s (lower-type value)]
    (str (.toUpperCase (subs s 0 1))
         (subs s 1))))

(defn snake-type
  "converts a string-like object to snake case representation
 
   (snake-type \"hello-world\")
   => \"hello_world\"
 
   (string/snake-type 'helloWorld)
   => 'hello_world"
  {:added "3.0"}
  [value]
  (-> (separate-humps value)
      (common/lower-case)
      (common/replace +non-snake-pattern+ "_")))

(defn spear-type
  "converts a string-like object to spear case representation
 
   (spear-type \"hello_world\")
   => \"hello-world\"
 
   (string/spear-type 'helloWorld)
   => 'hello-world"
  {:added "3.0"}
  [value]
  (-> (separate-humps value)
      (common/lower-case)
      (common/replace +non-spear-pattern+ "-")))

(defn upper-type
  "converts a string-like object to upper case representation
 
   (upper-type \"hello world\")
   => \"HELLO WORLD\"
 
   (str (string/upper-type 'hello-world))
   => \"HELLO WORLD\""
  {:added "3.0"}
  [^String value]
  (-> (separate-humps value)
      (common/split #"[ |\-|_]")
      (->> (map common/upper-case))
      (common/join " ")))

(defn typeless=
  "compares two representations 
 
   (typeless= \"helloWorld\" \"hello_world\")
   => true
   
   (string/typeless= :a-b-c \"a b c\")
   => true
 
   (string/typeless= 'getMethod :get-method)
   => true"
  {:added "3.0"}
  [x y]
  (= (lower-type x)
     (lower-type y)))
